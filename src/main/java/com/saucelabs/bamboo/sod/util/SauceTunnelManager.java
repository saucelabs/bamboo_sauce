package com.saucelabs.bamboo.sod.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface which defines the behaviour for Sauce Connect Tunnel implementations.
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

    public void closeTunnelsForPlan(String planKey);

    public void addTunnelToMap(String planKey, Object tunnel);

    Object openConnection(String username, String apiKey, String localHost, int intLocalPort, int intRemotePort, List<String> domainList) throws IOException;

    Map getTunnelMap();
}
