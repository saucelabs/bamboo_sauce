package com.saucelabs.bamboo.sod.selenium;

import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.util.SauceConnectOneManager;
import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.bamboo.sod.util.SauceTunnelManager;
import com.saucelabs.rest.Credential;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
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
         // start the Jetty locally and have it respond our secret code.
        Server server = startWebServer();

        try {
            // start a tunnel
            System.out.println("Starting a tunnel");
            Credential c = new Credential();            
            SauceTunnelManager sauceTunnelManager = new SauceConnectOneManager();

            SauceTunnel tunnel = (SauceTunnel) sauceTunnelManager.openConnection(c.getUsername(), c.getKey(), "localhost", 80, 8080, Collections.singletonList("testin.org"));
            sauceTunnelManager.addTunnelToMap("TEST", tunnel);

            assertTrue("tunnel id=" + tunnel.getId() + " isn't coming online", tunnel.isRunning());
            System.out.println("tunnel established");

            String originalUrl = System.getenv("SELENIUM_STARTING_URL");
            try {
                String driver = System.getenv("SELENIUM_DRIVER");
                if (driver == null || driver.equals("")) {
                    System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
                }

                System.setProperty("SELENIUM_STARTING_URL", "http://testing.org/");
                Selenium selenium = SeleniumFactory.create();
                selenium.start();
                selenium.open("/");
                // if the server really hit our Jetty, we should see the same title that includes the secret code.
                assertEquals("test" + code, selenium.getTitle());
                selenium.stop();
            } finally {
                sauceTunnelManager.closeTunnelsForPlan("TEST");
                if (originalUrl != null && !originalUrl.equals("")) {
                     System.setProperty("SELENIUM_STARTING_URL", originalUrl);
                }
            }
        } finally {
            server.stop();
        }
    }
}
