package it.com.saucelabs.bamboo.sod;


import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class AdministrationTest extends IntegrationTestHelper {

    
    @Test
    public void testAdmin() throws Exception {
        selenium.open("/allPlans.action");
        selenium.waitForPageToLoad("30000");
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

}
