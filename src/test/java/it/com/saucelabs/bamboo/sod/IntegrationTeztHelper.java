package it.com.saucelabs.bamboo.sod;


import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.ci.sauceconnect.SauceConnectTwoManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.rest.Credential;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.TimeUnit;


/**
 * @author Ross Rowe
 */
public class IntegrationTeztHelper {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=60&os=windows 2008&browser=firefox&browser-version=4.";
    private static final String DUMMY_PLAN_KEY = "test";

    protected WebDriver selenium;
    private SauceTunnelManager sauceTunnelManager;

    @Before
    public void setUp() throws Exception {
        File sauceSettings = new File(new File(System.getProperty("user.home")), ".sauce-ondemand");
        if (!sauceSettings.exists()) {
            String userName = System.getProperty("sauce.user");
            String accessKey = System.getProperty("access.key");
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(accessKey)) {
                Credential credential = new Credential(userName, accessKey);
                credential.saveTo(sauceSettings);
            }
        }
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

        System.setProperty("SELENIUM_STARTING_URL", "http://localhost:" + AbstractTestHelper.PORT + "/bamboo/allPlans.action");
        selenium = SeleniumFactory.createWebDriver();
        selenium.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }


    @After
    public void tearDown() throws Exception {
        sauceTunnelManager.closeTunnelsForPlan(DUMMY_PLAN_KEY);
        selenium.close();

    }

    @Test
	public void empty() throws Exception {}
    

}
