package it;

import com.thoughtworks.selenium.*;
import java.io.InputStream;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.regex.Pattern;
import com.atlassian.selenium.SeleniumClient;
import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

public class admin  {

//    private DefaultSelenium selenium;
    private SeleniumClient selenium;

    @Before
    public void setUp() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test.properties");
        Properties properties = new Properties();
        properties.load(stream);
        selenium = seleniumClient();
//        selenium = new DefaultSelenium("ondemand.saucelabs.com",
//                80, generateStartCommand(properties), "http://localhost:8085/");
//        selenium.start();
    }

    @Test
    @Ignore
    public void testAdmin() throws Exception {
        selenium.open("/allPlans.action");
        selenium.click("admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("sauceOnDemandConfig");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("sauceOnDemandConfigurationForm_username"));
        assertTrue(selenium.isElementPresent("sauceOnDemandConfigurationForm_accesskey"));
        assertTrue(selenium.isElementPresent("sauceOnDemandConfigurationForm_seleniumHost"));
        assertTrue(selenium.isElementPresent("sauceOnDemandConfigurationForm_seleniumPort"));
        selenium.type("sauceOnDemandConfigurationForm_username", "admin");
        selenium.type("sauceOnDemandConfigurationForm_accesskey", "admin");
        selenium.type("sauceOnDemandConfigurationForm_seleniumHost", "saucelabs.com");
        selenium.type("sauceOnDemandConfigurationForm_seleniumPort", "4445");
        selenium.click("sauceOnDemandConfigurationForm_save");
        selenium.waitForPageToLoad("30000");
//        verifyEquals("admin", selenium.getValue("sauceOnDemandConfigurationForm_username"));
//        verifyEquals("saucelabs.com", selenium.getValue("sauceOnDemandConfigurationForm_seleniumHost"));
//        verifyEquals("4445", selenium.getValue("sauceOnDemandConfigurationForm_seleniumPort"));
        selenium.click("sub-tree-item-26-link");
        selenium.click("sub-tree-item-75-unread-count");
        selenium.click("//div[@id='entries']/div[1]/div/div[1]/div/div[1]/div[5]/div/div/div");
        selenium.click("//span[@id='sub-tree-item-81-name']/span[1]");
        selenium.click("sub-tree-item-51-link");
        selenium.click("//div[@id='entries']/div[1]/div/div[1]/div/div[1]/div[5]/div/div/div");
        selenium.click("//span[@id='sub-tree-item-81-name']/span[1]");
        selenium.click("//span[@id='sub-tree-item-2-name']/span[1]");
        selenium.click("//span[@id='sub-tree-item-81-name']/span[1]");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
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
