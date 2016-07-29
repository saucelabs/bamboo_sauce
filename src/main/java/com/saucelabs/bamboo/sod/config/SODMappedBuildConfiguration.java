package com.saucelabs.bamboo.sod.config;

import com.saucelabs.ci.SeleniumVersion;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;

import static com.saucelabs.bamboo.sod.config.SODKeys.*;

/**
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class SODMappedBuildConfiguration {
    private Map<String, String> map;

    public SODMappedBuildConfiguration(Map<String, String> map) {
        this.map = map;
    }

    public boolean shouldOverrideAuthentication() {
        return map.get(OVERRIDE_AUTHENTICATION_KEY) != null && map.get(OVERRIDE_AUTHENTICATION_KEY).equals("true");
    }

    public String getUsername() {
        return map.get(SAUCE_USER_KEY);
    }

    public String getAccessKey() {
        return map.get(SAUCE_ACCESS_KEY);
    }

    public String[] getSelectedBrowsers() {
        String browsers = map.get(BROWSER_KEY);
        if (browsers == null) {
            map.get(BROWSER_RC_KEY);
        }
        return fromString(browsers);
    }

    public static String[] fromString(String string) {
        if (string == null)
            return new String[]{};
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        String result[] = new String[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = strings[i];
        }
        return result;
    }

    public void setBrowserKey(String browser) {
        map.put(BROWSER_KEY, browser);
    }

    public String getSeleniumStartingUrl() {
        return map.get(SELENIUM_URL_KEY);
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(map.get(ENABLED_KEY));
    }

    public boolean recordVideo() {
        return Boolean.parseBoolean(map.get(RECORD_VIDEO_KEY));
    }

    public String getUserExtensionsJson() {
        return map.get(USER_EXTENSIONS_JSON_KEY);
    }

    public String getFirefoxProfileUrl() {
        return map.get(FIREFOX_PROFILE_KEY);
    }

    public void setFirefoxProfileUrl(String profileUrl) {
        map.put(FIREFOX_PROFILE_KEY, profileUrl);
    }

    public int getMaxDuration() {
        return NumberUtils.toInt(map.get(MAX_DURATION_KEY));
    }

    public void setMaxDuration(int duration) {
        map.put(MAX_DURATION_KEY, Integer.toString(duration));
    }

    public int getIdleTimeout() {
        return NumberUtils.toInt(map.get(IDLE_TIMEOUT_KEY));
    }

    public void setIdleTimeout(int timeout) {
        map.put(IDLE_TIMEOUT_KEY, Integer.toString(timeout));
    }

    @Deprecated
    public boolean isSshEnabled() {
        return Boolean.parseBoolean(map.get(SSH_ENABLED_KEY));
    }

    public boolean isSauceConnectEnabled() {
        return Boolean.parseBoolean(map.get(SSH_ENABLED_KEY));
    }

    public boolean isVerboseSSHLogging() {
        return Boolean.parseBoolean(map.get(SSH_VERBOSE_KEY));
    }

    public boolean useGeneratedTunnelIdentifier() {
        return Boolean.parseBoolean(map.get(SSH_USE_GENERATED_TUNNEL_ID));
    }

    public String getSshPorts() {

        String port = map.get(SELENIUM_PORT_KEY);
        if (port == null || port.equals("")) {
            if (isSshEnabled()) {
                port = "4445";
            } else {
                port = "4444";
            }
        }
        return port;
    }

    public String getSshHost() {

        String host = map.get(SELENIUM_HOST_KEY);
        if (host == null || host.equals("")) {
            if (isSshEnabled()) {
                host = "localhost";
            } else {
                host = "ondemand.saucelabs.com";
            }
        }
        return host;
    }

    public String getTempApikey() {
        return map.get(TEMP_API_KEY);
    }

    public String getTempUsername() {
        return map.get(TEMP_USERNAME);
    }

    public void setTempUsername(String user) {
        map.put(TEMP_USERNAME, user);
    }

    public void setTempApikey(String key) {
        map.put(TEMP_API_KEY, key);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public SeleniumVersion getSeleniumVersion() {
        Boolean useSeleniumRc = Boolean.parseBoolean(map.get(SELENIUMRC_KEY));
        if (useSeleniumRc)
            return SeleniumVersion.ONE;
        else
            return SeleniumVersion.TWO;
    }

    public String getHttpsProtocol() {
        return map.get(HTTPS_PROTOCOL);
    }

    public String getSauceConnectOptions() {
        return map.get(SAUCE_CONNECT_OPTIONS);
    }


}