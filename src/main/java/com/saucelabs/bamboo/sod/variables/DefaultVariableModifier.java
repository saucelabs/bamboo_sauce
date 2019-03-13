package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.variable.VariableType;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import com.saucelabs.ci.SODSeleniumConfiguration;
import com.saucelabs.ci.SeleniumVersion;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.atlassian.bamboo.variable.CustomVariableContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the Builder instance (for pre-Bamboo 3 instances).
 * The variables are saved to the plan's configuration by the {@link com.saucelabs.bamboo.sod.action.EnvironmentConfigurator} class,
 * and are removed by the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class.
 *
 * @author Ross Rowe
 */
public abstract class DefaultVariableModifier implements VariableModifier {

    private static final Logger logger = Logger.getLogger(DefaultVariableModifier.class);

    public CustomVariableContext getCustomVariableContext() {
        return customVariableContext;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }

    private CustomVariableContext customVariableContext;

    protected static final String EQUALS = "=\"";

    protected static final String CUSTOM_DATA = "sauce:job-build=%1$s";

    protected SODMappedBuildConfiguration config;
    protected AdministrationConfigurationManager administrationConfigurationManager;
    protected BuildDefinition definition;
    protected BuildContext buildContext;
    private BrowserFactory sauceBrowserFactory;

    public DefaultVariableModifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext, CustomVariableContext customVariableContext) {
        this.config = config;
        this.definition = definition;
        this.buildContext = buildContext;
        this.customVariableContext = customVariableContext;
    }

    protected Map<String, VariableDefinitionContext> createSelenium2VariableContext(VariableContext variableContext) {
        Map<String, VariableDefinitionContext> variables = new HashMap<String, VariableDefinitionContext>();

        String[] selectedBrowsers = config.getSelectedBrowsers();
        JSONArray browsersJSON = new JSONArray();
        for (String browser : selectedBrowsers) {
            Browser browserInstance = sauceBrowserFactory.webDriverBrowserForKey(browser.replaceAll(" ", "_"));
            browserAsJSON(browsersJSON, browserInstance);
        }
        String jsonString = browsersJSON.toString();
        addVariable(variableContext, SODKeys.SAUCE_BROWSERS, jsonString);
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        createCommonEnvironmentVariables(variableContext, adminConfig);
        return variables;
    }

    private void browserAsJSON(JSONArray browsersJSON, Browser browserInstance) {
        if (browserInstance != null) {
            JSONObject config = new JSONObject();
            config.put("os", browserInstance.getOs());
            config.put("platform", browserInstance.getPlatform().toString());
            config.put("browser", browserInstance.getBrowserName());
            config.put("browser-version", browserInstance.getVersion());
            config.put("url", browserInstance.getUri());

            browsersJSON.add(config);
        }
    }

    private void addVariable(VariableContext variables, String key, String value) {
        variables.addLocalVariable(key, value);
        VariableDefinitionContext variableDefinitionContext = variables.getEffectiveVariables().get(key);
        if (variableDefinitionContext != null)
        {
            variableDefinitionContext.setVariableType(VariableType.ENVIRONMENT);
        }
    }

    /**
     * Writes the following environment variables to the <code>stringBuilder</code>:
     * @param variables      FIXME
     * @param adminConfig    FIXME
     */
    private void createCommonEnvironmentVariables(VariableContext variables, AdministrationConfiguration adminConfig) {

        if (config.shouldOverrideAuthentication() && StringUtils.isNotEmpty(config.getUsername())) {
            config.setTempUsername(config.getUsername());
        } else {
            config.setTempUsername(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY));
        }

        if (config.shouldOverrideAuthentication() && StringUtils.isNotEmpty(config.getAccessKey())) {
            config.setTempApikey(config.getAccessKey());
        } else {
            config.setTempApikey(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY));
        }

        if (config.shouldOverrideAuthentication() && StringUtils.isNotEmpty(config.getDataCenter())) {
            config.setTempDatacenter(config.getDataCenter());
        } else {
            config.setTempDatacenter(adminConfig.getSystemProperty(SODKeys.SOD_DATACENTER_KEY));
        }

        String sodDriverURI = getSodDriverUri(config.getTempUsername(), config.getTempApikey(), config);
        addVariable(variables, SODKeys.SELENIUM_DRIVER_ENV, sodDriverURI);
        addVariable(variables, SODKeys.SELENIUM_HOST_ENV, config.getSshHost());
        addVariable(variables, SODKeys.SELENIUM_PORT_ENV, config.getSshPorts());
        addVariable(variables, SODKeys.SELENIUM_STARTING_URL_ENV, config.getSeleniumStartingUrl());
        addVariable(variables, SODKeys.SELENIUM_URL_ENV, config.getSeleniumStartingUrl());
        addVariable(variables, SODKeys.SELENIUM_MAX_DURATION_ENV, config.getMaxDuration());
        addVariable(variables, SODKeys.SELENIUM_IDLE_TIMEOUT_ENV, config.getIdleTimeout());
        addVariable(variables, SODKeys.SAUCE_USERNAME, config.getTempUsername());
        addVariable(variables, SODKeys.SAUCE_USER_NAME, config.getTempUsername());
        addVariable(variables, SODKeys.SAUCE_API_KEY, config.getTempApikey());
        addVariable(variables, SODKeys.SAUCE_DATA_CENTER, config.getTempDatacenter());
        if (buildContext.getParentBuildContext() == null) {
            addVariable(variables, SODKeys.BAMBOO_BUILD_NUMBER_ENV, buildContext.getBuildResultKey());
            addVariable(variables, SODKeys.SAUCE_BUILD_NAME, buildContext.getBuildResultKey());
        } else {
            addVariable(variables, SODKeys.BAMBOO_BUILD_NUMBER_ENV, buildContext.getParentBuildContext().getBuildResultKey());
            addVariable(variables, SODKeys.SAUCE_BUILD_NAME, buildContext.getParentBuildContext().getBuildResultKey());

        }
        String[] selectedBrowsers = config.getSelectedBrowsers();
        if (selectedBrowsers.length == 1) {
            Browser browser = sauceBrowserFactory.webDriverBrowserForKey(selectedBrowsers[0].replaceAll(" ", "_"));
            //DefaultCapabilities information
            if (browser != null) {
                addVariable(variables, SODKeys.SELENIUM_PLATFORM_ENV, browser.getOs());
                addVariable(variables, SODKeys.SELENIUM_BROWSER_ENV, browser.getBrowserName());
                addVariable(variables, SODKeys.SELENIUM_VERSION_ENV, browser.getVersion());
            }
        }
        if (config.useGeneratedTunnelIdentifier()) {
            addVariable(variables, SODKeys.TUNNEL_IDENTIFIER, customVariableContext.getVariables(buildContext).get(SODKeys.TUNNEL_IDENTIFIER));
        }
    }

    private void addVariable(VariableContext variables, String key, int value) {
        addVariable(variables, key, String.valueOf(value));
    }

    /**
     * @param username    Sauce Username
     * @param apiKey      Sauce API Key
     * @param config      ??? FIXME
     * @return JSON FIXME
     */
    protected String getSodJson(String username, String apiKey, SODMappedBuildConfiguration config) {

        String[] selectedBrowsers = config.getSelectedBrowsers();
        if (selectedBrowsers.length == 1) {
            SODSeleniumConfiguration sodConfig = new SODSeleniumConfiguration(username, apiKey, sauceBrowserFactory.seleniumBrowserForKey(config.getSelectedBrowsers()[0]));
            sodConfig.setJobName(buildContext.getPlanName() + "-" + Integer.toString(buildContext.getBuildNumber()));
            sodConfig.setFirefoxProfileUrl(StringUtils.defaultString(config.getFirefoxProfileUrl()));
            sodConfig.setIdleTimeout(config.getIdleTimeout());
            sodConfig.setMaxDuration(config.getMaxDuration());
            sodConfig.setRecordVideo(config.recordVideo());
            sodConfig.setUserExtensions(config.getUserExtensionsJson());
            try {
                return sodConfig.toJson();
            } catch (JSONException e) {
                logger.error("Error converting object to JSON", e);
            }
        }
        return "";
    }

    /**
     * Generates a String that represents the Sauce OnDemand driver URL. This is used by the
     * <a href="http://selenium-client-factory.infradna.com/">selenium-client-factory</a> library to instantiate the Sauce-specific drivers.
     *
     * @param username    Sauce Username
     * @param apiKey      Sauce API Key
     * @param config      ??? FIXME
     * @return String representing the Sauce OnDemand driver URI
     */
    protected String getSodDriverUri(String username, String apiKey, SODMappedBuildConfiguration config) {
        StringBuilder sb = new StringBuilder("sauce-ondemand:?username=");
        sb.append(username);
        sb.append("&access-key=").append(apiKey);
        sb.append("&job-name=").append(buildContext.getPlanName().trim()).append('-').append(Integer.toString(buildContext.getBuildNumber()));

        String[] selectedBrowsers = config.getSelectedBrowsers();
        if (selectedBrowsers.length == 1) {
            Browser browser = sauceBrowserFactory.webDriverBrowserForKey(config.getSelectedBrowsers()[0].replaceAll(" ", "_"));
            if (browser != null) {
                sb.append("&os=").append(browser.getOs());
                sb.append("&browser=").append(browser.getBrowserName());
                sb.append("&browser-version=").append(browser.getVersion());
            }
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

    /**
     * @return String representing the set of environment variables to apply
     */
    protected String createSeleniumEnvironmentVariables() {
        return createSeleniumEnvironmentVariables("");
    }

    /**
     * @param prefix Prefix for each environment variable (eg '-D'), can be null
     * @return String representing the set of environment variables to apply
     * @deprecated
     */
    protected String createSeleniumEnvironmentVariables(String prefix) {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        StringBuilder stringBuilder = new StringBuilder();
        createCommonEnvironmentVariables(prefix, stringBuilder, adminConfig);
        String[] selectedBrowsers = config.getSelectedBrowsers();
        if (selectedBrowsers.length == 1) {
            Browser browser = sauceBrowserFactory.webDriverBrowserForKey(selectedBrowsers[0].replaceAll(" ", "_"));
            //DefaultCapabilities information
            if (browser != null) {
                stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_PLATFORM_ENV).append(EQUALS).append(browser.getOs()).append('"');
                stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browser.getBrowserName()).append('"');
                stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_VERSION_ENV).append(EQUALS).append(browser.getVersion()).append('"');
            }
        } //multiple browsers are populated within the bamboo_SAUCE_ONDEMAND_BROWSERS environment variable

//        JSONArray browsersJSON = new JSONArray();
//        for (String browser : selectedBrowsers) {
//            Browser browserInstance = sauceBrowserFactory.webDriverBrowserForKey(browser.replaceAll(" ", "_"));
//            browserAsJSON(browsersJSON, browserInstance);
//        }
//        String jsonString = browsersJSON.toString();
//        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_BROWSERS).append(EQUALS).append(StringEscapeUtils.escapeJava(jsonString)).append('"');;

        return stringBuilder.toString();
    }

    /**
     * Writes the following environment variables to the <code>stringBuilder</code>:
     *
     * @param prefix Prefix for each environment variable (eg '-D'), can be null
     * @param stringBuilder
     * @param adminConfig
     */
    private void createCommonEnvironmentVariables(String prefix, StringBuilder stringBuilder, AdministrationConfiguration adminConfig) {

        if (config.shouldOverrideAuthentication() && StringUtils.isNotEmpty(config.getUsername())) {
            config.setTempUsername(config.getUsername());
        } else {
            config.setTempUsername(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY));
        }

        if (config.shouldOverrideAuthentication() && StringUtils.isNotEmpty(config.getAccessKey())) {
            config.setTempApikey(config.getAccessKey());
        } else {
            config.setTempApikey(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY));
        }

        String sodDriverURI = getSodDriverUri(config.getTempUsername(), config.getTempApikey(), config);
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV).append(EQUALS).append(sodDriverURI).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_HOST_ENV).append(EQUALS).append(config.getSshHost()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV).append(EQUALS).append(config.getSshPorts()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_URL_ENV).append(EQUALS).append(config.getSeleniumStartingUrl()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV).append(EQUALS).append(config.getSeleniumStartingUrl()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_MAX_DURATION_ENV).append(EQUALS).append(config.getMaxDuration()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SELENIUM_IDLE_TIMEOUT_ENV).append(EQUALS).append(config.getIdleTimeout()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_USERNAME).append(EQUALS).append(config.getTempUsername()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_USER_NAME).append(EQUALS).append(config.getTempUsername()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_API_KEY).append(EQUALS).append(config.getTempApikey()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_USERNAME_ENV).append(EQUALS).append(config.getTempUsername()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_ACCESS_KEY_ENV).append(EQUALS).append(config.getTempApikey()).append('"');
        stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_DATA_CENTER_ENV).append(EQUALS).append(config.getTempDatacenter()).append('"');
        if (buildContext.getParentBuildContext() == null) {
            stringBuilder.append(' ').append(prefix).append(SODKeys.BAMBOO_BUILD_NUMBER_ENV).append(EQUALS).append(buildContext.getBuildResultKey()).append('"');
            stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_BUILD_NAME).append(EQUALS).append(buildContext.getBuildResultKey()).append('"');
        } else {
            stringBuilder.append(' ').append(prefix).append(SODKeys.BAMBOO_BUILD_NUMBER_ENV).append(EQUALS).append(buildContext.getParentBuildContext().getBuildResultKey()).append('"');
            stringBuilder.append(' ').append(prefix).append(SODKeys.SAUCE_BUILD_NAME).append(EQUALS).append(buildContext.getParentBuildContext().getBuildResultKey()).append('"');
        }
        if (config.useGeneratedTunnelIdentifier()) {
            String tunnelIdentifier = customVariableContext.getVariables(buildContext).get(SODKeys.TUNNEL_IDENTIFIER);
            stringBuilder.append(' ').append(prefix).append(SODKeys.TUNNEL_IDENTIFIER).append(EQUALS).append(tunnelIdentifier).append('"');
        }
    }


}
