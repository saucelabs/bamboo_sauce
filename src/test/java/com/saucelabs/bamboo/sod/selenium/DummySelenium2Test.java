package com.saucelabs.bamboo.sod.selenium;

import com.saucelabs.bamboo.sod.AbstractTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * @author Ross Rowe
 */
public class DummySelenium2Test extends AbstractTestHelper {
    private WebDriver driver;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabillities = new DesiredCapabilities(
                "firefox", "3.6.", Platform.WINDOWS);
        capabillities.setCapability("name", "Selenium 2 Test");
        driver = new RemoteWebDriver(
                new URL("http://" + System.getProperty("sauce.username") + ":" +  System.getProperty("sauce.accessKey") + "@ondemand.saucelabs.com:80/wd/hub"),
                capabillities);
    }

    @Test
    public void invokeGoogle() throws Exception {
        driver.get("http://www.google.com");
        WebElement search = driver.findElement(By.name("q"));
        search.sendKeys("Hello, WebDriver");
        search.submit();
        System.out.println(driver.getTitle());
    }

    public void tearDown() throws Exception {
        driver.quit();
    }
}
