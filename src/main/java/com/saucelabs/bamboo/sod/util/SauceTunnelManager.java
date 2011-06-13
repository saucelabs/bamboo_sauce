package com.saucelabs.bamboo.sod.util;

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
public interface SauceTunnelManager
{

    /**
     * Timeout is hard-coded to 180 seconds.
     */
    public static final int SSH_TIMEOUT = 180 * 1000;

    /**
     *
     * @param planKey
     */
    public void closeTunnelsForPlan(String planKey);

    /**
     *
     * @param planKey
     * @param tunnel
     */
    public void addTunnelToMap(String planKey, Object tunnel);


    Object openConnection(String username, String apiKey, String localHost, int intLocalPort, int intRemotePort, List<String> domainList) throws IOException;

    Map getTunnelMap();
}
