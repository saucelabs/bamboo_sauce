package it.com.saucelabs.bamboo.sod;


import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;


public class AdministrationTest extends IntegrationTeztHelper {


    @Test
    public void testAdmin() throws Exception {
        selenium.get("start.action");
        selenium.findElement(By.linkText("log in")).click();
        selenium.findElement(By.id("loginForm_os_username")).clear();
        selenium.findElement(By.id("loginForm_os_username")).sendKeys("admin");
        selenium.findElement(By.id("loginForm_save")).click();
        selenium.findElement(By.id("admin")).click();
        selenium.findElement(By.id("sauceOnDemandConfig")).click();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).sendKeys("admin");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).sendKeys("admin");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumHost")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumHost")).sendKeys("saucelabs.com");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumPort")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumPort")).sendKeys("4444");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_save")).click();
//        verifyTrue(selenium.isTextPresent("Configuration Updated"));

//        verifyEquals("admin", selenium.getValue("sauceOnDemandConfigurationForm_username"));
//        verifyEquals("saucelabs.com", selenium.getValue("sauceOnDemandConfigurationForm_seleniumHost"));
//        verifyEquals("4445", selenium.getValue("sauceOnDemandConfigurationForm_seleniumPort"));        
    }

}
