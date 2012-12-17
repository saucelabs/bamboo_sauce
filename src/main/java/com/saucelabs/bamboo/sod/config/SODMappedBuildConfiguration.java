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

    public String getUsername() {
        return map.get(SAUCE_USER_NAME);
    }

    public String getAccessKey() {
        return map.get(SAUCE_ACCESS_KEY);
    }

    public String[] getSelectedBrowsers() {
        return fromString(map.get(BROWSER_KEY));
    }

    public static String[] fromString(String string) {
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

    public boolean isSshEnabled() {
        return Boolean.parseBoolean(map.get(SSH_ENABLED_KEY));
    }

    public void setSshEnabled(boolean enabled) {
        map.put(SSH_ENABLED_KEY, Boolean.toString(enabled));
    }

    public boolean useSshDefaults() {
        return Boolean.parseBoolean(map.get(SSH_USE_DEFAULTS_KEY));
    }

    public String getSshHost() {
        return map.get(SSH_LOCAL_HOST_KEY);
    }

    public String getSshPorts() {
        return map.get(SSH_LOCAL_PORTS_KEY);
    }

    public void setSshPorts(String ports) {
        map.put(SSH_LOCAL_PORTS_KEY, ports);
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
}