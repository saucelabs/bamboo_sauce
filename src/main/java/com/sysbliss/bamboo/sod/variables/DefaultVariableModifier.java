package com.sysbliss.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.sysbliss.bamboo.sod.Browser;
import com.sysbliss.bamboo.sod.BrowserFactory;
import com.sysbliss.bamboo.sod.SODSeleniumConfiguration;
import com.sysbliss.bamboo.sod.config.SODKeys;
import com.sysbliss.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 * @author Ross Rowe
 */
public class DefaultVariableModifier implements VariableModifier {

    protected static final String EQUALS = "=\"";

    protected static final String CUSTOM_DATA = "sauce:job-build=%3$s";

    protected SODMappedBuildConfiguration config;
    private AdministrationConfigurationManager administrationConfigurationManager;
    protected BuildDefinition definition;
    private BuildContext buildContext;
    private BrowserFactory sauceBrowserFactory;

    public DefaultVariableModifier(SODMappedBuildConfiguration config, AdministrationConfigurationManager administrationConfigurationManager, BuildDefinition definition, BuildContext buildContext, BrowserFactory sauceBrowserFactory) {
        this(config, definition, buildContext);
        this.administrationConfigurationManager = administrationConfigurationManager;
        this.sauceBrowserFactory = sauceBrowserFactory;
    }

    public DefaultVariableModifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        this.config = config;
        this.definition = definition;
        this.buildContext = buildContext;
    }

    public void storeVariables() throws JSONException {

        String envBuffer = createSeleniumEnvironmentVariables();
        AbstractBuilder builder = (AbstractBuilder) definition.getBuilder();
        String originalEnv = null;
        if (builder != null) {
            originalEnv = builder.getEnvironmentVariables();
            config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
            if (StringUtils.isNotBlank(originalEnv)) {
                envBuffer = " " + envBuffer;
            }
            builder.setEnvironmentVariables(builder.getEnvironmentVariables() + envBuffer);
        }
    }

    public void restoreVariables() {
        AbstractBuilder builder = (AbstractBuilder) definition.getBuilder();
        builder.setEnvironmentVariables(config.getMap().get(SODKeys.TEMP_ENV_VARS));
        config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
    }

    protected String createSeleniumEnvironmentVariables() throws JSONException {
        return createSeleniumEnvironmentVariables("");
    }

    protected String createSeleniumEnvironmentVariables(String prefix) throws JSONException {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String sodUsername = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String sodKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        String host = adminConfig.getSystemProperty(SODKeys.SELENIUM_HOST_KEY);
        String port = adminConfig.getSystemProperty(SODKeys.SELENIUM_PORT_KEY);
        String browserUrl = config.getSeleniumStartingUrl();
        String browserJson = getSodJson(sodUsername, sodKey, config);
        String sodDriverURI = getSodDriverUri(sodUsername, sodKey, config);

        config.setTempUsername(sodUsername);
        config.setTempApikey(sodKey);

        StringBuilder envBuffer = new StringBuilder();

        String sodHost = config.getSshDomains();
        String finalStartingUrl = browserUrl;

        if (config.isAutoDomain()) {
            sodHost = "bamboo-" + buildContext.getPlanKey() + ".bamboo";
            finalStartingUrl = "http://" + sodHost + ':' + config.getSshTunnelPorts() + '/';
        }

        envBuffer.append(prefix).append(SODKeys.SELENIUM_HOST_ENV).append(EQUALS).append(host).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV).append('=').append(port);
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browserJson).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV).append(EQUALS).append(finalStartingUrl).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_ONDEMAND_HOST).append(EQUALS).append(sodHost).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV).append(EQUALS).append(sodDriverURI).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_HOST_ENV_LEGACY).append(EQUALS).append(host).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV_LEGACY).append('=').append(port);
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV_LEGACY).append(EQUALS).append(browserJson).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV_LEGACY).append(EQUALS).append(finalStartingUrl).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_ONDEMAND_HOST_LEGACY).append(EQUALS).append(sodHost).append('"');
        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV_LEGACY).append(EQUALS).append(sodDriverURI).append('"');

        if (buildContext.getParentBuildContext() == null) {
            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getBuildResultKey()));
        } else {
            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getParentBuildContext().getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getParentBuildContext().getBuildResultKey()));
        }
        return envBuffer.toString();
    }

    private String getSodJson(String username, String apiKey, SODMappedBuildConfiguration config) throws JSONException {

        SODSeleniumConfiguration sodConfig = new SODSeleniumConfiguration(username, apiKey, sauceBrowserFactory.forKey(config.getBrowserKey()));
        sodConfig.setJobName(buildContext.getPlanName() + "-" + Integer.toString(buildContext.getBuildNumber()));
        sodConfig.setFirefoxProfileUrl(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sodConfig.setIdleTimeout(config.getIdleTimeout());
        sodConfig.setMaxDuration(config.getMaxDuration());
        sodConfig.setRecordVideo(config.recordVideo());
        sodConfig.setUserExtensions(StringUtils.defaultString(config.getUserExtensionsJson()));

        return sodConfig.toJson();
    }

    /**
     * Generates a String that represents the Sauce OnDemand driver URL.
     *
     * @param username
     * @param apiKey
     * @param config
     * @param config
     * @return
     * @throws org.json.JSONException
     */
    private String getSodDriverUri(String username, String apiKey, SODMappedBuildConfiguration config) throws JSONException {
        StringBuilder sb = new StringBuilder("sauce-ondemand:?username=");
        sb.append(username);
        sb.append("&access-key=").append(apiKey);
        sb.append("&job-name=").append(StringUtils.trim(buildContext.getPlanName())).append('-').append(Integer.toString(buildContext.getBuildNumber()));

        Browser browser = sauceBrowserFactory.forKey(config.getBrowserKey());
        sb.append("&os=").append(browser.getOs());
        sb.append("&browser=").append(browser.getBrowserName());
        sb.append("&browser-version=").append(browser.getVersion());

        sb.append("&firefox-profile-url=").append(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sb.append("&max-duration=").append(config.getMaxDuration());
        sb.append("&idle-timeout=").append(config.getIdleTimeout());
        sb.append("&user-extensions-url=").append(StringUtils.defaultString(config.getUserExtensionsJson()));

        return sb.toString();
    }
}
