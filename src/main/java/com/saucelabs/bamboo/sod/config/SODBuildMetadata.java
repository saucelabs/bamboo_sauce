package com.saucelabs.bamboo.sod.config;

/**
 *
 * @deprecated Can be deleted
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public interface SODBuildMetadata
{
        
    String getSshHost();
    void setSshHost(String host);

    String getSshPorts();
    void setSshPorts(String ports);

    String getSshTunnelPorts();
    void setSshTunnelPorts(String ports);

    String getSshDomains();
    void setSshDomains(String domains);
}
