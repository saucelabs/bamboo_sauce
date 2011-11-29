package it.com.saucelabs.bamboo.sod;


import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.action.BuildConfigurator;

import com.saucelabs.bamboo.sod.util.BambooSauceFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.saucelabs.ci.sauceconnect.SauceConnectTwoManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.rest.Credential;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;

import org.junit.BeforeClass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Ross Rowe
 */
public class IntegrationTeztHelper {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=60&os=windows 2008&browser=firefox&browser-version=4.";
    private static final String DUMMY_PLAN_KEY = "test";

    protected Selenium selenium;
    private SauceTunnelManager sauceTunnelManager;

    @Before
    public void setUp() throws Exception {
        final Credential c = new Credential();
        Authenticator.setDefault(
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                c.getUsername(), c.getKey().toCharArray());
                    }
                }
        );
        sauceTunnelManager = new SauceConnectTwoManager();
        Process sauceConnect = (Process) sauceTunnelManager.openConnection(c.getUsername(), c.getKey());
        sauceTunnelManager.addTunnelToMap(DUMMY_PLAN_KEY, sauceConnect);
        System.out.println("tunnel established");
        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }

        String originalUrl = System.getenv("SELENIUM_STARTING_URL");
        System.setProperty("SELENIUM_STARTING_URL", "http://localhost:8080/");


        selenium = SeleniumFactory.create();

        //selenium = new DefaultSelenium("ondemand.saucelabs.com",
        //        80, generateStartCommand(properties), "http://localhost:8085/");
        //selenium.start();
    }


    @After
    public void tearDown() throws Exception {
        selenium.stop();
        sauceTunnelManager.closeTunnelsForPlan(DUMMY_PLAN_KEY);
    }

    private String generateStartCommand(Properties properties) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"username\": \"").append(properties.get("sauce.username"));
        builder.append(",\"access-key\": \"").append(properties.get("sauce.accessKey"));
        builder.append(",\"os\": \"Windows 2003\"");
        builder.append("\"browser\": \"firefox\"," + "\"browser-version\": \"3.6.\"," + "\"name\": \"This is an example test\"}");
        return builder.toString();
    }
}
