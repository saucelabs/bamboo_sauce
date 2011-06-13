package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.config.SODKeys;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.saucelabs.sauce_ondemand.driver.SauceOnDemandSelenium;
import org.junit.*;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ross Rowe
 */
public class SeleniumFactoryTest extends AbstractTestHelper {
    private Selenium selenium;
    private static final String DUMMY_BUILD_DATA = "BSAD-TRUNK-14";
    private String originalUrl;

    @Before
    public void setUp() throws Exception {
        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }

        this.originalUrl = System.getenv("SELENIUM_STARTING_URL");
        System.setProperty("SELENIUM_STARTING_URL", "http://www.google.com");

        this.selenium = SeleniumFactory.create();

        assertTrue("Selenium instance is not SauceSeleniumFactory", selenium instanceof SauceOnDemandSelenium);        
        String bambooData = System.getenv(SODKeys.BAMBOO_BUILD_NUMBER_ENV);
        if (bambooData == null || bambooData.equals("")) {
            bambooData = DUMMY_BUILD_DATA;
        }
        selenium.start();
        SauceOnDemandSelenium sauce = (SauceOnDemandSelenium) selenium;
        sauce.setBuildNumber(bambooData);

    }

    @Test
    public void sauce() throws Exception {
        this.selenium.open("/");
        assertEquals("Google",
                this.selenium.getTitle());
        SauceOnDemandSelenium sauce = (SauceOnDemandSelenium) selenium;
        sauce.jobPassed();
    }

    @After
    public void tearDown() throws Exception {
        this.selenium.stop();
        if (originalUrl != null && !originalUrl.equals("")) {
            System.setProperty("SELENIUM_STARTING_URL", originalUrl);
        }
    }
}
