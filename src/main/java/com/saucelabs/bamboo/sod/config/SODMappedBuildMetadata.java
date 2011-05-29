package com.sysbliss.bamboo.sod.config;

import java.util.Map;
import static com.sysbliss.bamboo.sod.config.SODKeys.*;

/**
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class SODMappedBuildMetadata
{
    private Map<String,String> map;

    public SODMappedBuildMetadata(Map<String, String> map)
    {
        this.map = map;
    }

    public String getSshHost()
    {
        return map.get(SSH_LOCAL_HOST_KEY);
    }

    public void setSshHost(String host)
    {
        map.put(SSH_LOCAL_HOST_KEY,host);
    }

    public String getSshPorts()
    {
        return map.get(SSH_LOCAL_PORTS_KEY);
    }

    public void setSshPorts(String ports)
    {
        map.put(SSH_LOCAL_PORTS_KEY,ports);
    }

    public String getSshTunnelPorts()
    {
        return map.get(SSH_REMOTE_PORTS_KEY);
    }

    public void setSshTunnelPorts(String ports)
    {
        map.put(SSH_REMOTE_PORTS_KEY,ports);
    }

    public String getSshDomains()
    {
        return map.get(SSH_DOMAINS_KEY);
    }

    public void setSshDomains(String domains)
    {
        map.put(SSH_DOMAINS_KEY,domains);
    }
}
