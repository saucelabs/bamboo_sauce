package com.saucelabs.bamboo.sod;

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
public class DummySelenium2Test {
    private WebDriver driver;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabillities = new DesiredCapabilities(
                "firefox", "3.6.", Platform.WINDOWS);
        capabillities.setCapability("name", "Hello, Sauce OnDemand!");
        driver = new RemoteWebDriver(
                new URL("http://rossco_9_9:44f0744c-1689-4418-af63-560303cbb37b@ondemand.saucelabs.com:80/wd/hub"),
                capabillities);
    }

    @Test
    public void run() throws Exception {
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
