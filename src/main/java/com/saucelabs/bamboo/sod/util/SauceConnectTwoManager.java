package com.saucelabs.bamboo.sod.util;

import com.saucelabs.sauceconnect.SauceConnect;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ross Rowe
 */
public class SauceConnectTwoManager implements SauceTunnelManager {

    private static final Logger logger = Logger.getLogger(SauceConnectTwoManager.class);
    private Thread sauceConnectThread;
    private Map<String, List<SauceConnect>> tunnelMap;

    public SauceConnectTwoManager() {
        this.tunnelMap = new HashMap<String,List<SauceConnect>>();
    }

    public void closeTunnelsForPlan(String planKey) {
        if (tunnelMap.containsKey(planKey)) {
            List<SauceConnect> tunnelList = tunnelMap.get(planKey);
            for (SauceConnect sauceConnect : tunnelList) {
                sauceConnect.removeHandler();
                sauceConnect.closeTunnel();
                sauceConnectThread.interrupt();
            }
            tunnelMap.remove(planKey);
        }
    }

    public void addTunnelToMap(String planKey, Object tunnel) {
        if (!tunnelMap.containsKey(planKey)) {
            tunnelMap.put(planKey, new ArrayList<SauceConnect>());
        }

        tunnelMap.get(planKey).add((SauceConnect) tunnel);
    }

    public Object openConnection(String username, String apiKey, String localHost, int intLocalPort, int intRemotePort, List<String> domainList) throws IOException {
        final SauceConnect sauceConnect = new SauceConnect(new String[]{username, apiKey, "-d", "--proxy-host", localHost});
        this.sauceConnectThread = new Thread("SauceConnectThread") {
            @Override
            public void run() {
                sauceConnect.openConnection();
            }
        };
        sauceConnectThread.start();

        try {
            Thread.sleep(1000 * 60 * 2); //2 minutes
        } catch (InterruptedException e) {
            //continue;
        }
        return sauceConnect;
    }

    public Map getTunnelMap() {
        return tunnelMap;
    }
}
