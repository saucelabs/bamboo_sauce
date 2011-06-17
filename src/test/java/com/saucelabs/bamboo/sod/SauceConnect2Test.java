package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.util.SauceConnectTwoManager;
import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.bamboo.sod.util.SauceTunnelManager;
import com.saucelabs.rest.Credential;
import com.saucelabs.sauceconnect.SauceConnect;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SauceConnect2Test extends AbstractTestHelper {

    /**
     * Start a web server locally, set up an SSH tunnel, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {
        this.code = new Random().nextInt();

        // start the Jetty locally and have it respond our secret code.
         org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(this), "/*");

        server.start();
        System.out.println("Started Jetty at 8080");

        try {
            // start a tunnel
            System.out.println("Starting a tunnel");
            final Credential c = new Credential();
            Authenticator.setDefault(
                    new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    c.getUsername(), c.getKey().toCharArray());
                        }
                    }
            );            
            SauceTunnelManager sauceTunnelManager = new SauceConnectTwoManager();
//            sauceAPIFactory.setupProxy("proxy.immi.local", "80", "exitr6", "abc125");
            SauceConnect sauceConnect = (SauceConnect) sauceTunnelManager.openConnection(c.getUsername(), c.getKey(), "testing.org", -1, -1, null);
            sauceTunnelManager.addTunnelToMap("TEST", sauceConnect);
            System.out.println("tunnel established");
            String driver = System.getenv("SELENIUM_DRIVER");
            if (driver == null || driver.equals("")) {
                System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
            }

            String originalUrl = System.getenv("SELENIUM_STARTING_URL");
            System.setProperty("SELENIUM_STARTING_URL", "http://testing.org:8080/");
            Selenium selenium = SeleniumFactory.create();            
            try {
                selenium.start();
                selenium.open("/");
                // if the server really hit our Jetty, we should see the same title that includes the secret code.
                assertEquals("test" + code, selenium.getTitle());
                selenium.stop();
            } finally {
                sauceTunnelManager.closeTunnelsForPlan("TEST");
                selenium.stop();
                if (originalUrl != null && !originalUrl.equals("")) {
                     System.setProperty("SELENIUM_STARTING_URL", originalUrl);
                }
            }
        } finally {
            server.stop();
        }
    }
}
