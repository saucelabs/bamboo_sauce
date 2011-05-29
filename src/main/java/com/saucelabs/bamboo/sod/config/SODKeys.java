package com.saucelabs.bamboo.sod.config;

/**
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public final class SODKeys
{

    /** Disallow instantiation of class.*/
    private SODKeys() {}
    public static final String CUSTOM_PREFIX = "custom.sauceondemand.";
    public static final String SSH_PREFIX = "custom.sauceondemand.ssh.";

    /* BASIC OPTIONS */
    public static final String ENABLED_KEY = CUSTOM_PREFIX + "enabled";
    public static final String BROWSER_KEY = CUSTOM_PREFIX + "browser";
    public static final String SELENIUM_URL_KEY = CUSTOM_PREFIX + "selenium.url";

    /* ADVANCED OPTIONS */
    public static final String RECORD_VIDEO_KEY = CUSTOM_PREFIX + "record-video";
    public static final String USER_EXTENSIONS_JSON_KEY = CUSTOM_PREFIX + "user-extensions.json";
    public static final String FIREFOX_PROFILE_KEY = CUSTOM_PREFIX + "firefox-profile";
    public static final String MAX_DURATION_KEY = CUSTOM_PREFIX + "max-duration";
    public static final String IDLE_TIMEOUT_KEY = CUSTOM_PREFIX + "idle-timeout";

    /* SSH BASIC TUNNEL OPTIONS */
    public static final String SSH_ENABLED_KEY = SSH_PREFIX + "enabled";
    public static final String SSH_AUTO_DOMAIN_KEY = SSH_PREFIX + "auto-domain";
    public static final String SSH_USE_DEFAULTS_KEY = SSH_PREFIX + "defaults";
    public static final String SSH_LOCAL_HOST_KEY = SSH_PREFIX + "local.host";
    //NOTE: ports and tunnel ports must be same length
    public static final String SSH_LOCAL_PORTS_KEY = SSH_PREFIX + "local.ports";
    public static final String SSH_REMOTE_PORTS_KEY = SSH_PREFIX + "remote.ports";
    public static final String SSH_DOMAINS_KEY = SSH_PREFIX + "domains";

    /* ADMIN CONFIG KEYS */
    public static final String SOD_USERNAME_KEY = "sauceondemand.username";
    public static final String SOD_ACCESSKEY_KEY = "sauceondemand.accesskey";
    public static final String SELENIUM_HOST_KEY = "selenium.host";
    public static final String SELENIUM_PORT_KEY = "selenium.port";
    public static final String SELENIUM_BROWSER_KEY = "selenium.browser";


    /* ENV Vars */
    public static final String SELENIUM_DRIVER_ENV = "SELENIUM_DRIVER";
    public static final String SELENIUM_HOST_ENV = "SELENIUM_HOST";
    public static final String SELENIUM_PORT_ENV = "SELENIUM_PORT";
    public static final String SELENIUM_BROWSER_ENV = "SELENIUM_BROWSER";
    public static final String SAUCE_ONDEMAND_HOST = "SAUCE_ONDEMAND_HOST";
    public static final String SELENIUM_STARTING_URL_ENV = "SELENIUM_STARTING_URL";
    public static final String SAUCE_CUSTOM_DATA = "sauce_bamboo_buildNumber";

    public static final String TEMP_ENV_VARS = CUSTOM_PREFIX + "temp.env.vars";

    public static final String TEMP_USERNAME = CUSTOM_PREFIX + "temp.username";
    public static final String TEMP_API_KEY = CUSTOM_PREFIX + "temp.apikey";
}
