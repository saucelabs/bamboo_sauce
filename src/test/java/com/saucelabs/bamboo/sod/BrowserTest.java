package com.saucelabs.bamboo.sod;

import org.junit.Test;
import org.openqa.selenium.Platform;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class BrowserTest {
	@Test
	public void osNames() throws Exception {
        Browser browser = new Browser(null, "Windows 2008", null, null, null);
        assertEquals("Platform is not Windows", browser.getPlatform(), Platform.VISTA);
        browser = new Browser(null, "Windows 2003", null, null, null);
        assertEquals("Platform is not Windows", browser.getPlatform(), Platform.XP);
        browser = new Browser(null, "Linux", null, null, null);
        assertEquals("Platform is not Linux", browser.getPlatform(), Platform.LINUX);

	}
}
