package it.com.saucelabs.bamboo.sod;


import org.junit.Test;
import org.openqa.selenium.By;


public class AdministrationTest extends IntegrationTeztHelper {


    @Test
    public void testAdmin() throws Exception {
        selenium.get("http://localhost:5000/bamboo/admin/sauceondemand/configureSauceOnDemand.action?os_username=admin&os_password=admin");
//        selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).clear();
//        selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).sendKeys("admin");
//        selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).clear();
//        selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).sendKeys("admin");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumHost")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumHost")).sendKeys("saucelabs.com");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumPort")).clear();
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumPort")).sendKeys("4444");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_save")).click();
//        verifyTrue(selenium.isTextPresent("Configuration Updated"));

    }

}
