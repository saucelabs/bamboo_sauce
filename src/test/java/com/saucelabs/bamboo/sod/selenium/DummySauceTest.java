package com.saucelabs.bamboo.sod.selenium;

import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class DummySauceTest extends AbstractTestHelper {
    private Selenium selenium;

    private static final String DUMMY_BUILD_DATA ="sauce:job-build=BSAD-TRUNK-14";

    @Before
    public void setUp() throws Exception {
        this.selenium = new DefaultSelenium(
                "ondemand.saucelabs.com",
                80,
                "{\"username\": \"" + System.getProperty("sauce.username") + "\"," +
                "\"access-key\": \"" + System.getProperty("sauce.accessKey") + "\"," +
                "\"os\": \"Windows 2003\"," +
                "\"browser\": \"firefox\"," +
                "\"browser-version\": \"3.6.\"," +
                "\"name\": \"This is an example test\"}",
                "http://example.saucelabs.com/");

        String bambooData = System.getenv(SODKeys.SAUCE_CUSTOM_DATA_ENV);
        if (bambooData == null || bambooData.equals("")) {
            bambooData = DUMMY_BUILD_DATA;
        }
        selenium.start();
        this.selenium.setContext(bambooData);

    }

    @Test
    @Ignore
    public void sauce() throws Exception {
        this.selenium.open("/");
        assertEquals("Cross browser testing with Selenium - Sauce Labs",
                     this.selenium.getTitle());
    }

    @After
    public void tearDown() throws Exception {
        this.selenium.stop();
    }
}