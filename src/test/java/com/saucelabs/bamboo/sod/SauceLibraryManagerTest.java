package com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.util.SauceLibraryManager;
import org.apache.commons.io.IOUtils;

/**
 * @author Ross Rowe
 */
public class SauceLibraryManagerTest {
    
    private SauceLibraryManager sauceLibraryManager;
    
    public void versionsFromFile() throws Exception {
        String sampleJson = IOUtils.toString(getClass().getResourceAsStream("/browsers.js"));
        int version = sauceLibraryManager.extractVersionFromResponse(sampleJson);
        assertEquals("Version not equal to 17", version, 17);
    }
}
