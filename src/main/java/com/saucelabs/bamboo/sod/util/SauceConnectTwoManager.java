package com.saucelabs.bamboo.sod.util;

import com.saucelabs.sauceconnect.SauceConnect;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles opening a SSH Tunnel using the Sauce Connect 2 logic. The class  maintains a cache of {@link com.saucelabs.rest.SauceTunnel} instances mapped against
 * the corresponding plan key.  This class can be considered a singleton, and is instantiated via the 'component' element of the atlassian-plugin.xml
 * file (ie. using Spring).
 *
 * @author Ross Rowe
 */
public class SauceConnectTwoManager implements SauceTunnelManager {

    private static final Logger logger = Logger.getLogger(SauceConnectTwoManager.class);
    private Thread sauceConnectThread;
    private Map<String, List<Process>> tunnelMap;
    private static SauceTunnelManager instance;

    public SauceConnectTwoManager() {
        this.tunnelMap = new HashMap<String, List<Process>>();
    }

    public void closeTunnelsForPlan(String planKey) {
        if (tunnelMap.containsKey(planKey)) {
            List<Process> tunnelList = tunnelMap.get(planKey);
            for (Process sauceConnect : tunnelList) {
//                sauceConnect.removeHandler();
//                sauceConnect.closeTunnel();
//                sauceConnectThread.interrupt();
                sauceConnect.destroy();
            }

            tunnelMap.remove(planKey);
        }
    }

    public void addTunnelToMap(String planKey, Object tunnel) {
        if (!tunnelMap.containsKey(planKey)) {
            tunnelMap.put(planKey, new ArrayList<Process>());
        }

        tunnelMap.get(planKey).add((Process) tunnel);
    }

    /**
     * Creates a new Thread which creates a SSH Tunnel.
     *
     * @param username
     * @param apiKey
     * @param localHost
     * @param intLocalPort
     * @param intRemotePort
     * @param domainList
     * @return
     * @throws IOException
     */
    public Object openConnection(String username, String apiKey, String localHost, int intLocalPort, int intRemotePort, List<String> domainList) throws IOException {

        String separator = System.getProperty("file.separator");
        try {
            File jarFile = new File
                    (SauceConnect.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String path = System.getProperty("java.home")
                    + separator + "bin" + separator + "java";
            ProcessBuilder processBuilder =
                    new ProcessBuilder(path, "-cp",
                            jarFile.getPath(),
                            SauceConnect.class.getName(),
                            username,
                            apiKey,
                            "-d",
                            "--proxy-host",
                            localHost);
            final Process process = processBuilder.start();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getInputStream(), System.out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getErrorStream(), System.err);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            try {
                Thread.sleep(1000 * 60 * 2); //2 minutes
            } catch (InterruptedException e) {
                //continue;
            }
            return process;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //SauceConnect.getInterpreter();
//        final SauceConnect sauceConnect = new SauceConnect(new String[]{username, apiKey, "-d", "--proxy-host", localHost});
//        sauceConnect.setStandaloneMode(false);
//        this.sauceConnectThread = new Thread("SauceConnectThread") {
//            @Override
//            public void run() {
//                sauceConnect.openConnection();
//            }
//        };
//        sauceConnectThread.start();


//        return sauceConnect;
        return null;
    }

    public Map getTunnelMap() {
        return tunnelMap;
    }

    /**
     * Returns a singleton instance of SauceConnectTwoManager.  This is required because
     * remote agents don't have the Bamboo component plugin available, so the Spring
     * auto-wiring doesn't work.
     *
     * @return
     */
    public static SauceTunnelManager getInstance() {
        if (instance == null) {
            instance = new SauceConnectTwoManager();
        }
        return instance;
    }
}
