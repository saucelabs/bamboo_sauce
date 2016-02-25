package com.saucelabs.bamboo.sod.singletons;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;

/**
 * Created by gavinmogan on 2016-02-24.
 */
public class SauceConnectFourManagerSingleton {

    private static SauceConnectFourManager sauceConnectFourTunnelManager;

    public static SauceConnectFourManager getSauceConnectFourTunnelManager() {
        if (SauceConnectFourManagerSingleton.sauceConnectFourTunnelManager == null) {
            setSauceConnectFourTunnelManager(new SauceConnectFourManager());
        }
        return SauceConnectFourManagerSingleton.sauceConnectFourTunnelManager;
    }

    public static void setSauceConnectFourTunnelManager(SauceConnectFourManager sauceConnectFourTunnelManager) {
        SauceConnectFourManagerSingleton.sauceConnectFourTunnelManager = sauceConnectFourTunnelManager;
    }
}
