package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.rest.Credential;
import com.saucelabs.sauceconnect.SauceConnect;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.Test;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

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
            final Credential c = new Credential();
            Authenticator.setDefault(
                    new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    c.getUsername(), c.getKey().toCharArray());
                        }
                    }
            );
            SauceFactory sauceAPIFactory = new SauceFactory();
//            sauceAPIFactory.setupProxy("proxy.immi.local", "80", "exitr6", "abc125");
            SauceConnect sauceConnect = sauceAPIFactory.createSauceConnect(c.getUsername(), c.getKey(), "testing.org");
            //assertTrue("tunnel id=" + tunnel.getId() + " isn't coming online", tunnel.isRunning());
            System.out.println("tunnel established");
            String driver = System.getenv("SELENIUM_DRIVER");
            if (driver == null || driver.equals("")) {
                System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
            }

            String url = System.getenv("SELENIUM_STARTING_URL");
            if (url == null || url.equals("")) {
                System.setProperty("SELENIUM_STARTING_URL", "http://testing.org:8080/");
            }

            Selenium selenium = SeleniumFactory.create();
            try {


                selenium.start();
                selenium.open("/");
                // if the server really hit our Jetty, we should see the same title that includes the secret code.
                assertEquals("test" + code, selenium.getTitle());
                selenium.stop();
            } finally {
                sauceAPIFactory.closeSauceConnect(sauceConnect);
                selenium.stop();
            }
        } finally {
            server.stop();
        }
    }
}
