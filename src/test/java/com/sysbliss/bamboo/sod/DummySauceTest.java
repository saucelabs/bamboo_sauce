package com.sysbliss.bamboo.sod;

import com.sysbliss.bamboo.sod.config.SODKeys;
import com.thoughtworks.selenium.DefaultSelenium;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ross Rowe
 */
public class DummySauceTest {
    private DefaultSelenium selenium;
    private static final String DUMMY_CUSTOM_DATA = "sauce:job-info={\"custom-data\": {\"bamboo-buildKey\": \"TEST_PLAN\", \"bamboo-buildNumber\": \"1\", \"bamboo-buildResultKey\": \"TST\"}";

    @Before
    public void setUp() throws Exception {
        DefaultSelenium selenium = new DefaultSelenium(
                "ondemand.saucelabs.com",
                80,
                "{\"username\": \"rossco_9_9\"," +
                "\"access-key\": \"BLAH\"," +
                "\"os\": \"Windows 2003\"," +
                "\"browser\": \"firefox\"," +
                "\"browser-version\": \"3.6.\"," +
                "\"name\": \"This is an example test\"}",
                "http://example.saucelabs.com/");
        System.setProperty(SODKeys.SAUCE_CUSTOM_DATA, DUMMY_CUSTOM_DATA);
        String bambooData = System.getProperty(SODKeys.SAUCE_CUSTOM_DATA);
        this.selenium.setContext(bambooData);
        selenium.start();
        this.selenium = selenium;
    }

    @Test
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