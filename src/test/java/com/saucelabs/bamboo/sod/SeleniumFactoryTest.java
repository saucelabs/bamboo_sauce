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
public class SeleniumFactoryTest {
    private Selenium selenium;
    private static final String DUMMY_BUILD_DATA ="BSAD-TRUNK-14";
    private static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=30&os=Linux&browser=firefox&browser-version=3.";

    @Before
    public void setUp() throws Exception {
        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }

        String url = System.getenv("SELENIUM_STARTING_URL");
        if (url == null || url.equals("")) {
            System.setProperty("SELENIUM_STARTING_URL", "http://www.google.com");
        }
        this.selenium = SeleniumFactory.create();

        assertTrue("Selenium instance is not SauceSeleniumFactory", selenium instanceof SauceOnDemandSelenium);

        Map<String, String> envVars = System.getenv();
//        for (Map.Entry envVar : envVars.entrySet()) {
//            System.out.println(envVar.getKey() + " : " + envVar.getValue());
//        }
        String bambooData = System.getenv(SODKeys.SAUCE_CUSTOM_DATA_ENV);
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
    }
}
