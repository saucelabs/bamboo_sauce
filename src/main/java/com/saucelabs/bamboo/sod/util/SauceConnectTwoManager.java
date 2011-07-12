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
 * Handles opening a SSH Tunnel using the Sauce Connect 2 logic. The class  maintains a cache of {@link Process } instances mapped against
 * the corresponding plan key.  This class can be considered a singleton, and is instantiated via the 'component' element of the atlassian-plugin.xml
 * file (ie. using Spring).
 *
 * @author Ross Rowe
 */
public class SauceConnectTwoManager implements SauceTunnelManager {

    private static final Logger logger = Logger.getLogger(SauceConnectTwoManager.class);
    private Map<String, List<Process>> tunnelMap;
    private static SauceTunnelManager instance;

    public SauceConnectTwoManager() {
        this.tunnelMap = new HashMap<String, List<Process>>();
    }

    public void closeTunnelsForPlan(String planKey) {
        if (tunnelMap.containsKey(planKey)) {
            List<Process> tunnelList = tunnelMap.get(planKey);
            for (Process sauceConnect : tunnelList) {
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
     * Creates a new Java process to run the Sauce Connect 2 library.  We have to launch a separate process
     * because of a version conflict with Jython (Bamboo includes v2.2 but Sauce Connect requires v 2.5).
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
//        try {
//            File jarFile = new File
//                    (SauceConnect.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File jarFile = new File("/Developer/workspace/bamboo_sauce/target/bamboo-sauceondemand-plugin-1.3.1.jar");
            String path = System.getProperty("java.home")
                    + separator + "bin" + separator + "java";
            ProcessBuilder processBuilder =
                    new ProcessBuilder(path, "-cp",
                            jarFile.getPath(),
                            SauceConnect.class.getName(),
                            username,
                            apiKey,
                            "-p",
                            domainList.get(0)
                    );
            final Process process = processBuilder.start();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getInputStream(), System.out);
                    } catch (IOException e) {
                        logger.error("Exception trapped in copying output from Sauce Connect, attempting to continue", e);
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getErrorStream(), System.err);
                    } catch (IOException e) {
                        logger.error("Exception trapped in copying error output from Sauce Connect, attempting to continue", e);
                    }
                }
            }).start();
            try {
                //TODO check for the Tunnel started message in the Sauce Connect output
                Thread.sleep(1000 * 60 * 1); //2 minutes
            } catch (InterruptedException e) {
                //continue;
            }
            return process;

//        } catch (URISyntaxException e) {
//            //shouldn't happen
//            logger.error("Exception occured during retrieval of bamboo-sauce.jar URL", e);
//        }
//
//        return null;
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
