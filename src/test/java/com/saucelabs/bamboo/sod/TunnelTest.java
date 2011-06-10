package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.rest.Credential;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.Test;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

import java.io.IOException;
import java.util.Random;

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
        this.code = new Random().nextInt();
        // start the Jetty locally and have it respond our secret code.
        Server server = new Server();
        HttpListener listener = server.addListener("8080");
        ((SocketListener) listener).setMaxIdleTimeMs(0);
        ServletHttpContext context = (ServletHttpContext) server.getContext("/");
        context.addServlet("/", SauceConnect2Test.class.getName());
        server.start();
        System.out.println("Started Jetty at 8080");

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
                tunnel.connect(80,"localhost",8080);
            }

            assertTrue("tunnel id=" + tunnel.getId() + " isn't coming online", tunnel.isRunning());
            System.out.println("tunnel established");

            String originalUrl = System.getenv("SELENIUM_STARTING_URL");
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
                if (originalUrl != null && !originalUrl.equals("")) {
                     System.setProperty("SELENIUM_STARTING_URL", originalUrl);
                } 
            }
        } finally {
            server.stop();
        }
    }
}
