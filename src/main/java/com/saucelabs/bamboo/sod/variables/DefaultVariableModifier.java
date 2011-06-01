package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.Browser;
import com.saucelabs.bamboo.sod.BrowserFactory;
import com.saucelabs.bamboo.sod.SODSeleniumConfiguration;
import com.saucelabs.bamboo.sod.SeleniumVersion;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the Builder instance (for pre-Bamboo 3 instances).
 * The variables are saved to the plan's configuration by the {@link com.saucelabs.bamboo.sod.action.EnvironmentConfigurator} class,
 * and are removed by the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class.
 *
 * @author Ross Rowe
 */
public class DefaultVariableModifier implements VariableModifier {

    protected static final String EQUALS = "=\"";

    protected static final String CUSTOM_DATA = "sauce:job-tags=%3$s";

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
     * Stores the Sauce configuration values as environment variables.  Ideally, we would rather use system properties
     * instead of environment variables however there is a <a href="https://jira.atlassian.com/browse/BAM-7265">defect</a> in Bamboo
     * which causes quotes inside the -DargLine argument to be dropped.
     *
     * @throws JSONException if an error occurs generating the Selenium environment variables
     */
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
        if (builder != null) {
            builder.setEnvironmentVariables(config.getMap().get(SODKeys.TEMP_ENV_VARS));
        }
        config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
    }

    /**
     *
     * @return
     * @throws JSONException
     */
    protected String createSeleniumEnvironmentVariables() throws JSONException {
        return createSeleniumEnvironmentVariables("");
    }

    /**
     *
     * @param prefix Prefix for each environment variable (eg '-D'), can be null
     * @return String representing the set of environment variables to apply
     * @throws JSONException if an error occurs generating the Selenium environment variables
     */
    protected String createSeleniumEnvironmentVariables(String prefix) throws JSONException {
        if (config.getSeleniumVersion().equals(SeleniumVersion.ONE)) {
            return createSelenium1EnvironmentVariables(prefix);
        } else {
            return createSelenium2EnvironmentVariables(prefix);
        }
    }

    private String createSelenium2EnvironmentVariables(String prefix) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private String createSelenium1EnvironmentVariables(String prefix) throws JSONException {
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

        if (buildContext.getParentBuildContext() == null) {
            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getBuildResultKey())).append('"');
        } else {
            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getParentBuildContext().getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getParentBuildContext().getBuildResultKey())).append('"');
        }
        return envBuffer.toString();
    }

    /**
     *
     * @param username
     * @param apiKey
     * @param config
     * @return
     * @throws JSONException if an error occurs converting the config to JSON
     */
    protected String getSodJson(String username, String apiKey, SODMappedBuildConfiguration config) throws JSONException {

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
     * Generates a String that represents the Sauce OnDemand driver URL. This is used by the
     * <a href="http://selenium-client-factory.infradna.com/">selenium-client-factory</a> library to instantiate the Sauce-specific drivers.
     *
     * @param username
     * @param apiKey
     * @param config
     * @param config
     * @return String repreenting the Sauce OnDemand driver URI
     */
    protected String getSodDriverUri(String username, String apiKey, SODMappedBuildConfiguration config) {
        StringBuilder sb = new StringBuilder("sauce-ondemand:?username=");
        sb.append(username);
        sb.append("&access-key=").append(apiKey);
        sb.append("&job-name=").append(StringUtils.trim(buildContext.getPlanName())).append('-').append(Integer.toString(buildContext.getBuildNumber()));

        Browser browser = sauceBrowserFactory.forKey(config.getBrowserKey());
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
