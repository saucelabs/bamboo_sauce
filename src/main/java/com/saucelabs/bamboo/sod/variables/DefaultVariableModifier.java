package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.ci.*;
import org.apache.commons.lang.StringUtils;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the Builder instance (for pre-Bamboo 3 instances).
 * The variables are saved to the plan's configuration by the {@link com.saucelabs.bamboo.sod.action.EnvironmentConfigurator} class,
 * and are removed by the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class.
 *
 * @author Ross Rowe
 */
public abstract class DefaultVariableModifier implements VariableModifier {

    protected static final String EQUALS = "=\"";

    protected static final String CUSTOM_DATA = "sauce:job-build=%1$s";

    protected SODMappedBuildConfiguration config;
    protected AdministrationConfigurationManager administrationConfigurationManager;
    protected BuildDefinition definition;
    protected BuildContext buildContext;
    private BrowserFactory sauceBrowserFactory;

    public DefaultVariableModifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        this.config = config;
        this.definition = definition;
        this.buildContext = buildContext;
    }

    /**
     * @return
     */
    protected String createSeleniumEnvironmentVariables() {
        return createSeleniumEnvironmentVariables("");
    }

    /**
     * @param prefix Prefix for each environment variable (eg '-D'), can be null
     * @return String representing the set of environment variables to apply
     */
    protected String createSeleniumEnvironmentVariables(String prefix){
        if (config.getSeleniumVersion().equals(SeleniumVersion.ONE)) {
            return createSelenium1EnvironmentVariables(prefix);
        } else {
            return createSelenium2EnvironmentVariables(prefix);
        }
    }

    private String createSelenium2EnvironmentVariables(String prefix) {

        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        StringBuilder stringBuilder = new StringBuilder();
        createCommonEnvironmentVariables(prefix, stringBuilder, adminConfig);
        Browser browser = sauceBrowserFactory.webDriverBrowserForKey(config.getBrowserKey());
        //DefaultCapabilities information
        if (browser != null) {
            stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_PLATFORM_ENV).append(EQUALS).append(browser.getPlatform().toString()).append('"');
            stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browser.getBrowserName()).append('"');
            stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_VERSION_ENV).append(EQUALS).append(browser.getVersion()).append('"');
        }

        return stringBuilder.toString();
    }

    private String createSelenium1EnvironmentVariables(String prefix) {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String sodUsername = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String sodKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        String browserJson = getSodJson(sodUsername, sodKey, config);

        StringBuilder stringBuilder = new StringBuilder();
        createCommonEnvironmentVariables(prefix, stringBuilder, adminConfig);
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browserJson).append('"');
        if (buildContext.getParentBuildContext() == null) {
            stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA_ENV).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getBuildResultKey())).append('"');
        } else {
            stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA_ENV).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getParentBuildContext().getBuildResultKey())).append('"');
        }
        return stringBuilder.toString();
    }

    /**
     * Writes the following environment variables to the <code>stringBuilder</code>:
     * <ul>
     * <li></li>
     * </ul>
     * @param prefix
     * @param stringBuilder
     * @param adminConfig
     */
    private void createCommonEnvironmentVariables(String prefix, StringBuilder stringBuilder, AdministrationConfiguration adminConfig) {

        String sodUsername = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String sodKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        config.setTempUsername(sodUsername);
        config.setTempApikey(sodKey);
        String host = adminConfig.getSystemProperty(SODKeys.SELENIUM_HOST_KEY);
        String port = adminConfig.getSystemProperty(SODKeys.SELENIUM_PORT_KEY);
        config.setSshPorts(port);
        String sodDriverURI = getSodDriverUri(sodUsername, sodKey, config);

        stringBuilder.append(prefix).append(SODKeys.SELENIUM_HOST_ENV).append(EQUALS).append(host).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV).append(EQUALS).append(port).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV).append(EQUALS).append(sodDriverURI).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV).append(EQUALS).append(config.getSeleniumStartingUrl()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_USER_NAME).append(EQUALS).append(sodUsername).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_API_KEY).append(EQUALS).append(sodKey).append('"');
        if (buildContext.getParentBuildContext() == null) {
            stringBuilder.append(' ').append(prefix).append(SODKeys.BAMBOO_BUILD_NUMBER_ENV).append(EQUALS).append(buildContext.getBuildResultKey()).append('"');
        } else {
            stringBuilder.append(' ').append(prefix).append(SODKeys.BAMBOO_BUILD_NUMBER_ENV).append(EQUALS).append(buildContext.getParentBuildContext().getBuildResultKey()).append('"');
        }
    }

    /**
     * @param username
     * @param apiKey
     * @param config
     * @return
     */
    protected String getSodJson(String username, String apiKey, SODMappedBuildConfiguration config) {

        SODSeleniumConfiguration sodConfig = new SODSeleniumConfiguration(username, apiKey, sauceBrowserFactory.webDriverBrowserForKey(config.getBrowserKey()));
        sodConfig.setJobName(buildContext.getPlanName() + "-" + Integer.toString(buildContext.getBuildNumber()));
        sodConfig.setFirefoxProfileUrl(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sodConfig.setIdleTimeout(config.getIdleTimeout());
        sodConfig.setMaxDuration(config.getMaxDuration());
        sodConfig.setRecordVideo(config.recordVideo());
        sodConfig.setUserExtensions(config.getUserExtensionsJson());
        return sodConfig.toJson();
    }

    /**
     * Generates a String that represents the Sauce OnDemand driver URL. This is used by the
     * <a href="http://selenium-client-factory.infradna.com/">selenium-client-factory</a> library to instantiate the Sauce-specific drivers.
     *
     * @param username
     * @param apiKey
     * @param config
     * @param config
     * @return String representing the Sauce OnDemand driver URI
     */
    protected String getSodDriverUri(String username, String apiKey, SODMappedBuildConfiguration config) {
        StringBuilder sb = new StringBuilder("sauce-ondemand:?username=");
        sb.append(username);
        sb.append("&access-key=").append(apiKey);
        sb.append("&job-name=").append(buildContext.getPlanName().trim()).append('-').append(Integer.toString(buildContext.getBuildNumber()));

        Browser browser = sauceBrowserFactory.webDriverBrowserForKey(config.getBrowserKey());
        if (browser != null) {
            sb.append("&os=").append(browser.getOs());
            sb.append("&browser=").append(browser.getBrowserName());
            sb.append("&browser-version=").append(browser.getVersion());
        }

        sb.append("&firefox-profile-url=").append(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sb.append("&max-duration=").append(config.getMaxDuration());
        sb.append("&idle-timeout=").append(config.getIdleTimeout());
        sb.append("&user-extensions-url=").append(StringUtils.defaultString(config.getUserExtensionsJson()));

        return sb.toString();
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory) {
        this.sauceBrowserFactory = sauceBrowserFactory;
    }
}
