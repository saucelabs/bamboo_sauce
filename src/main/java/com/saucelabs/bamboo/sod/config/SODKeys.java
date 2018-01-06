package com.saucelabs.bamboo.sod.config;

import com.atlassian.bamboo.util.BuildUtils;
import com.saucelabs.saucerest.SauceREST;

/**
 * Collection of constants that relate to the Bamboo Sauce OnDemand plugin.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public final class SODKeys
{
    public static final String SAUCE_USERNAME = "SAUCE_USERNAME";
    public static final String SAUCE_USER_NAME = "SAUCE_USER_NAME";
    public static final String SAUCE_API_KEY = "SAUCE_ACCESS_KEY";
    public static final String SAUCE_BROWSERS = "SAUCE_ONDEMAND_BROWSERS";
    public static final String SELENIUM_MAX_DURATION_ENV = "SELENIUM_MAX_DURATION";
    public static final String SELENIUM_IDLE_TIMEOUT_ENV = "SELENIUM_IDLE_TIMEOUT";

    /** Disallow instantiation of class.*/
    private SODKeys() {}
    public static final String CUSTOM_PREFIX = "custom.sauceondemand.";
    public static final String SSH_PREFIX = "custom.sauceondemand.ssh.";

    /* BASIC OPTIONS */
    public static final String ENABLED_KEY = CUSTOM_PREFIX + "enabled";
    public static final String BROWSER_KEY = CUSTOM_PREFIX + "browser";
    public static final String BROWSER_RC_KEY = CUSTOM_PREFIX + "rcbrowser";
    public static final String SELENIUM_URL_KEY = CUSTOM_PREFIX + "selenium.url";
    public static final String SELENIUM_VERSION_KEY = CUSTOM_PREFIX + "selenium.version";
    public static final String SAUCE_USER_KEY = CUSTOM_PREFIX + "user_name";
    public static final String OVERRIDE_AUTHENTICATION_KEY = CUSTOM_PREFIX + "auth.enabled";
    public static final String SAUCE_ACCESS_KEY = CUSTOM_PREFIX + "access_key";

    /* ADVANCED OPTIONS */
    public static final String RECORD_VIDEO_KEY = CUSTOM_PREFIX + "record-video";
    public static final String USER_EXTENSIONS_JSON_KEY = CUSTOM_PREFIX + "user-extensions.json";
    public static final String FIREFOX_PROFILE_KEY = CUSTOM_PREFIX + "firefox-profile";
    public static final String MAX_DURATION_KEY = CUSTOM_PREFIX + "max-duration";
    public static final String IDLE_TIMEOUT_KEY = CUSTOM_PREFIX + "idle-timeout";
    public static final String SELENIUM_HOST_KEY = CUSTOM_PREFIX + "selenium.host";
    public static final String SELENIUM_PORT_KEY = CUSTOM_PREFIX + "selenium.port";

    /* SSH BASIC TUNNEL OPTIONS */
    public static final String SSH_ENABLED_KEY = SSH_PREFIX + "enabled";
    public static final String SSH_VERBOSE_KEY = SSH_PREFIX + "verbose";
    public static final String SSH_USE_DEFAULTS_KEY = SSH_PREFIX + "defaults";
    public static final String SSH_LOCAL_HOST_KEY = SSH_PREFIX + "local.host";
    //NOTE: ports and tunnel ports must be same length
    public static final String SSH_LOCAL_PORTS_KEY = SSH_PREFIX + "local.ports";
    public static final String SSH_USE_LATEST_SAUCE_CONNECT = SSH_PREFIX + "useLatestSauceConnect";
    public static final String SSH_USE_GENERATED_TUNNEL_ID = SSH_PREFIX + "useGeneratedTunnelIdentifier";

    /* ADMIN CONFIG KEYS */
    public static final String SOD_USERNAME_KEY = "sauceondemand.username";
    public static final String SOD_ACCESSKEY_KEY = "sauceondemand.accesskey";
    public static final String SOD_SAUCE_CONNECT_DIRECTORY = "sauceondemand.sauceConnectDirectory";
    public static final String SOD_SAUCE_CONNECT_MAX_RETRIES = "sauceondemand.sauceConnectMaxRetries";
    public static final String SOD_SAUCE_CONNECT_RETRY_WAIT_TIME = "sauceondemand.sauceConnectRetryWaitTime";

    /* ENV Vars */
    public static final String SELENIUM_DRIVER_ENV = "SELENIUM_DRIVER";
    public static final String SELENIUM_HOST_ENV = "SELENIUM_HOST";
    public static final String SELENIUM_PORT_ENV = "SELENIUM_PORT";
    public static final String SELENIUM_BROWSER_ENV = "SELENIUM_BROWSER";
    public static final String SELENIUM_PLATFORM_ENV = "SELENIUM_PLATFORM";
    public static final String SELENIUM_VERSION_ENV = "SELENIUM_VERSION";
    public static final String SAUCE_ONDEMAND_HOST = "SAUCE_HOST";
    public static final String SELENIUM_URL_ENV = "SELENIUM_URL";
    public static final String SELENIUM_STARTING_URL_ENV = "SELENIUM_STARTING_URL";
    public static final String SAUCE_CUSTOM_DATA_ENV = "SAUCE_BAMBOO_BUILDNUMBER";
    public static final String BAMBOO_BUILD_NUMBER_ENV = "SAUCE_BAMBOO_BUILDNUMBER";
    public static final String SAUCE_BUILD_NAME = "SAUCE_BUILD_NAME";
    public static final String SAUCE_USERNAME_ENV = "SAUCE_USERNAME";
    public static final String SAUCE_ACCESS_KEY_ENV = "SAUCE_ACCESS_KEY";

    public static final String TEMP_USERNAME = CUSTOM_PREFIX + "temp.username";
    public static final String TEMP_API_KEY = CUSTOM_PREFIX + "temp.apikey";

    public static final String HTTPS_PROTOCOL = CUSTOM_PREFIX + "httpsProtocol";
    public static final String SAUCE_CONNECT_OPTIONS = CUSTOM_PREFIX + "sauceConnectOptions";
    public static final String SAUCE_CONNECT_V3 = CUSTOM_PREFIX + "sauceConnect3";

    public static final String SAUCE_SESSION_ID = "sauceondemand.sessionId";
    public static final String TUNNEL_IDENTIFIER = "TUNNEL_IDENTIFIER";
}
