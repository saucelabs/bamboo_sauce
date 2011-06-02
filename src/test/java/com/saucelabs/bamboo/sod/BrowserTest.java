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

        Platform platform = Platform.extractFromSysProperty("Windows 2008");
        assertEquals("Platform is not Windows", platform, Platform.WINDOWS);
        platform = Platform.extractFromSysProperty("Windows 2003");
        assertEquals("Platform is not Windows", platform, Platform.WINDOWS);
        platform = Platform.extractFromSysProperty("Linux");
        assertEquals("Platform is not Linux", platform, Platform.LINUX);

	}
}
