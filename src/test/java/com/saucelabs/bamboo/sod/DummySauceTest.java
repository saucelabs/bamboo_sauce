package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.config.SODKeys;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class DummySauceTest {
    private Selenium selenium;
    private static final String DUMMY_CUSTOM_DATA = "sauce:job-info={\"custom-data\": {\"bamboo-buildKey\": \"TEST_PLAN\", \"bamboo-buildNumber\": \"1\", \"bamboo-buildResultKey\": \"TST\"}";

    private static final String DUMMY_TAG_DATA ="sauce:job-tags=BSAD-TRUNK-14";

    private static final String DUMMY_BUILD_DATA ="sauce:job-build=BSAD-TRUNK-14";


    @Before
    public void setUp() throws Exception {
        this.selenium = new DefaultSelenium(
                "ondemand.saucelabs.com",
                80,
                "{\"username\": \"rossco_9_9\"," +
                "\"access-key\": \"44f0744c-1689-4418-af63-560303cbb37b\"," +
                "\"os\": \"Windows 2003\"," +
                "\"browser\": \"firefox\"," +
                "\"browser-version\": \"3.6.\"," +
                "\"name\": \"This is an example test\"}",
                "http://example.saucelabs.com/");

        Map<String, String> envVars = System.getenv();
        for (Map.Entry envVar : envVars.entrySet()) {
            System.out.println(envVar.getKey() + " : " + envVar.getValue());
        }
        String bambooData = System.getenv(SODKeys.SAUCE_CUSTOM_DATA_ENV);
        if (bambooData == null || bambooData.equals("")) {
            bambooData = DUMMY_BUILD_DATA;
        }
        selenium.start();
        this.selenium.setContext(bambooData);

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