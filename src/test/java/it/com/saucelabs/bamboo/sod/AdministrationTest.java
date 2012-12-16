package it.com.saucelabs.bamboo.sod;


import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.rest.Credential;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class AdministrationTest extends IntegrationTeztHelper {


    @Test
    public void sauceFields() throws Exception {
	    Credential credential = new Credential();
	    
        selenium.get("http://localhost:" + AbstractTestHelper.PORT + "/bamboo/admin/sauceondemand/configureSauceOnDemand.action?os_username=admin&os_password=admin");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).sendKeys(credential.getUsername());
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).sendKeys(credential.getKey());
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumHost")).sendKeys("saucelabs.com");
        selenium.findElement(By.id("sauceOnDemandConfigurationForm_seleniumPort")).sendKeys("4444");
        //TODO verify no error messages
        //selenium.findElement(By.id("sauceOnDemandConfigurationForm_save")).click();
        //assertEquals("username incorrect", selenium.findElement(By.id("sauceOnDemandConfigurationForm_username")).getText(), credential.getUsername());
        //assertEquals("username incorrect", selenium.findElement(By.id("sauceOnDemandConfigurationForm_accessKey")).getText(), credential.getKey());
//        verifyTrue(selenium.isTextPresent("Configuration Updated"));


    }

    @Test
    @Ignore("Updating of Sauce Connect jar file is currently disabled")
    public void updateSauceConnect() throws Exception {
		Credential credential = new Credential();
        selenium.get("http://localhost:" + AbstractTestHelper.PORT + "/bamboo/admin/sauceondemand/configureSauceOnDemand.action?os_username=admin&os_password=admin");
	    WebElement element = selenium.findElement(By.linkText("Check for updates to Sauce Connect"));
	    element.click();
	    //TODO verify that message states that no update required
    }  

}
