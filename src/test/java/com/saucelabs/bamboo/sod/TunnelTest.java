package com.saucelabs.bamboo.sod;

import com.saucelabs.rest.Credential;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.sauce_ondemand.driver.SauceOnDemandSelenium;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.bio.SocketConnector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ross Rowe
 */
public class TunnelTest extends AbstractTestHelper {

    /**
     * Start a web server locally, set up an SSH tunnel, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {
        final int code = new Random().nextInt();

        // start the Jetty locally and have it respond our secret code.
        Server server = new Server();
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("text/html");
                resp.getWriter().println("<html><head><title>test" + code + "</title></head><body>it works</body></html>");
            }
        }), "/");
        server.setHandler(handler);

        SocketConnector connector = new SocketConnector();
        server.addConnector(connector);
        server.start();
        System.out.println("Started Jetty at " + connector.getLocalPort());

        try {
            // start a tunnel
            System.out.println("Starting a tunnel");
            Credential c = new Credential();
            SauceFactory sauceAPIFactory = new SauceFactory();
            SauceTunnelFactory tunnelFactory = sauceAPIFactory.createSauceTunnelFactory(c.getUsername(), c.getKey());
            SauceTunnel tunnel = tunnelFactory.create("test"+code+".org");

            if (tunnel != null) {
                try {
                    tunnel.waitUntilRunning(90000);
                    if (!tunnel.isRunning()) {
                        throw new IOException("Sauce OnDemand Tunnel didn't come online. Aborting.");
                    }
                } catch (InterruptedException e) {
                    throw new IOException("Sauce OnDemand Tunnel Aborted.");
                }
                tunnel.connect(80,"localhost",connector.getLocalPort());
            }

            assertTrue("tunnel id=" + tunnel.getId() + " isn't coming online", tunnel.isRunning());           
            System.out.println("tunnel established");

            try {
                String driver = System.getenv("SELENIUM_DRIVER");
                if (driver == null || driver.equals("")) {
                    System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
                }

                System.setProperty("SELENIUM_STARTING_URL", "http://test" + code + ".org/");
                Selenium selenium = SeleniumFactory.create();
                selenium.start();
                selenium.open("/");
                // if the server really hit our Jetty, we should see the same title that includes the secret code.
                assertEquals("test" + code, selenium.getTitle());
                selenium.stop();
            } finally {
                tunnel.disconnectAll();
                tunnel.destroy();
            }
        } finally {
            server.stop();
        }
    }
}
