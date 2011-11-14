package com.saucelabs.bamboo.sod;

import com.saucelabs.ci.SauceLibraryManager;
import org.apache.commons.io.IOUtils;

import static org.junit.Assert.assertEquals;

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
