package com.saucelabs.bamboo.sod.config;

import com.saucelabs.ci.SeleniumVersion;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;

import static com.saucelabs.bamboo.sod.config.SODKeys.*;

/**
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class SODMappedBuildConfiguration
{
    private Map<String,String> map;

    public SODMappedBuildConfiguration(Map<String, String> map)
    {
        this.map = map;
    }

    public String getBrowserKey()
    {
        return map.get(BROWSER_KEY);
    }

    public void setBrowserKey(String browser)
    {
        map.put(BROWSER_KEY,browser);
    }

    public String getSeleniumStartingUrl()
    {
        return map.get(SELENIUM_URL_KEY);
    }

    public void setSeleniumStartingUrl(String url)
    {
        map.put(SELENIUM_URL_KEY,url);
    }

    public boolean isEnabled()
    {
        return Boolean.parseBoolean(map.get(ENABLED_KEY));
    }

    public void setEnabled(boolean enabled)
    {
        map.put(ENABLED_KEY,Boolean.toString(enabled));
    }

    public boolean recordVideo()
    {
        return Boolean.parseBoolean(map.get(RECORD_VIDEO_KEY));
    }

    public void setRecordVideo(boolean record)
    {
        map.put(RECORD_VIDEO_KEY,Boolean.toString(record));
    }

    public String getUserExtensionsJson()
    {
        return map.get(USER_EXTENSIONS_JSON_KEY);
    }

    public void setUserExtensionsJson(String json)
    {
        map.put(USER_EXTENSIONS_JSON_KEY,json);
    }

    public String getFirefoxProfileUrl()
    {
        return map.get(FIREFOX_PROFILE_KEY);
    }

    public void setFirefoxProfileUrl(String profileUrl)
    {
        map.put(FIREFOX_PROFILE_KEY,profileUrl);
    }

    public int getMaxDuration()
    {
        return NumberUtils.toInt(map.get(MAX_DURATION_KEY));
    }

    public void setMaxDuration(int duration)
    {
        map.put(MAX_DURATION_KEY,Integer.toString(duration));
    }

    public int getIdleTimeout()
    {
        return NumberUtils.toInt(map.get(IDLE_TIMEOUT_KEY));
    }

    public void setIdleTimeout(int timeout)
    {
        map.put(IDLE_TIMEOUT_KEY,Integer.toString(timeout));
    }

    public boolean isSshEnabled()
    {
        return Boolean.parseBoolean(map.get(SSH_ENABLED_KEY));
    }

    public void setSshEnabled(boolean enabled)
    {
        map.put(SSH_ENABLED_KEY,Boolean.toString(enabled));
    }

    public boolean isAutoDomain()
    {
        return Boolean.parseBoolean(map.get(SSH_AUTO_DOMAIN_KEY));
    }

    public void setAutoDomain(boolean auto)
    {
        map.put(SSH_AUTO_DOMAIN_KEY,Boolean.toString(auto));
    }

    public boolean useSshDefaults()
    {
        return Boolean.parseBoolean(map.get(SSH_USE_DEFAULTS_KEY));
    }

    public void setUseSshDefaults(boolean defaults)
    {
        map.put(SSH_USE_DEFAULTS_KEY,Boolean.toString(defaults));
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

    public String getTempApikey()
    {
        return map.get(TEMP_API_KEY);
    }

    public String getTempUsername()
    {
        return map.get(TEMP_USERNAME);
    }

    public void setTempUsername(String user)
    {
        map.put(TEMP_USERNAME,user);
    }

    public void setTempApikey(String key)
    {
        map.put(TEMP_API_KEY,key);
    }

    public void setSshDomains(String domains)
    {
        map.put(SSH_DOMAINS_KEY,domains);
    }

    public Map<String, String> getMap()
    {
        return map;
    }

    public SeleniumVersion getSeleniumVersion() {
        String versionNumber = map.get(SELENIUM_VERSION_KEY);
        SeleniumVersion version = null;
        for (SeleniumVersion storedVersion : SeleniumVersion.values()) {
            if (storedVersion.getVersionNumber().equals(versionNumber)) {
                version = storedVersion;
                break;
            }
        }
        return version;
    }
}