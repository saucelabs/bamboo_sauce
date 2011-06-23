package com.saucelabs.bamboo.sod.util;

import com.saucelabs.sauceconnect.SauceConnect;
import org.apache.log4j.Logger;
import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

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
    private Map<String, List<SauceConnect>> tunnelMap;
    private static SauceTunnelManager instance;

    public SauceConnectTwoManager() {
        this.tunnelMap = new HashMap<String, List<SauceConnect>>();
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

        PySystemState.initialize();
        PythonInterpreter interpreter = new PythonInterpreter(null, new PySystemState());
        PySystemState sys = Py.getSystemState();
        try {
            File jarFile = new File
                    (SauceConnect.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            sys.path.append(new PyString(jarFile.getPath()));
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        interpreter.exec("import sauce_connect");
        SauceConnect.setInterpreterIfNull(interpreter);
        //SauceConnect.getInterpreter();
        final SauceConnect sauceConnect = new SauceConnect(new String[]{username, apiKey, "-d", "--proxy-host", localHost});
        sauceConnect.setStandaloneMode(false);
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
