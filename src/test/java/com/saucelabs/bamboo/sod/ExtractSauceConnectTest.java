package com.saucelabs.bamboo.sod;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * @author Ross Rowe
 */
@Ignore
public class ExtractSauceConnectTest {

    private SauceConnectFourManager manager = new SauceConnectFourManager();

    @Before
    public void setup() throws Exception {

    }

    @Test
    public void linux() throws Exception {
        File workingDirectory = new File(System.getProperty("java.io.tmpdir"));
        manager.extractZipFile(workingDirectory, SauceConnectFourManager.OperatingSystem.LINUX);
    }

    @Test
    public void windows() throws Exception {
        manager.extractZipFile(new File(System.getProperty("java.io.tmpdir")), SauceConnectFourManager.OperatingSystem.WINDOWS);
    }
}
