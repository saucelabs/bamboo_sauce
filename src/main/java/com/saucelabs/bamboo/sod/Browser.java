package com.saucelabs.bamboo.sod;

/**
 * Represents a Sauce Browser instance.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class Browser {
   
    private final String key;
    private final String os;
    private final String browserName;
    private final String version;
    private final String name;   

    public Browser(String key, String os, String browserName, String version, String name) {
        this.key = key;
        this.os = os;
        this.browserName = browserName;
        this.version = version;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getBrowserName() {
        return browserName;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }

    public String getVersion() {
        return version;
    }

}
