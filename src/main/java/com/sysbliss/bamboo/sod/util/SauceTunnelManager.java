package com.sysbliss.bamboo.sod.util;

import com.saucelabs.rest.SauceTunnel;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be considered a singleton, and is instantiated via the 'component' element of the atlassian-plugin.xml
 * file (ie. using Spring).  It maintains a cache of {@link com.saucelabs.rest.SauceTunnel} instances mapped against
 * the corresponding plan key.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class SauceTunnelManager
{
    private static final Logger logger = Logger.getLogger(SauceTunnelManager.class);

    private Map<String, List<SauceTunnel>> tunnelMap;

    public SauceTunnelManager()
    {
        this.tunnelMap = new HashMap<String, List<SauceTunnel>>();
    }


    /**
     *
     * @param planKey
     */
    public void closeTunnelsForPlan(String planKey)
    {
        if(tunnelMap.containsKey(planKey)) {
            List<SauceTunnel> tunnelList = tunnelMap.get(planKey);
            for(SauceTunnel tunnel : tunnelList) {
                try {
                    tunnel.disconnectAll();
                    tunnel.destroy();
                } catch (IOException e) {
                    logger.error("Failed to close a Sauce OnDemand Tunnel");
                    //continue processing
                }
            }

            tunnelMap.remove(planKey);
        }

    }

    /**
     *
     * @param planKey
     * @param tunnel
     */
    public  void addTunnelToMap(String planKey, SauceTunnel tunnel)
    {
        if(!tunnelMap.containsKey(planKey)) {
            tunnelMap.put(planKey, new ArrayList<SauceTunnel>());
        }

        tunnelMap.get(planKey).add(tunnel);
    }

    public Map<String, List<SauceTunnel>> getTunnelMap() {
        return tunnelMap;
    }


}
