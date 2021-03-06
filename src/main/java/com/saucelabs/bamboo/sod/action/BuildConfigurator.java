package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.saucelabs.bamboo.sod.BuildUtils;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.singletons.SauceConnectFourManagerSingleton;
import com.saucelabs.bamboo.sod.util.SauceLogInterceptor;
import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import com.saucelabs.ci.SeleniumVersion;
import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pre-Build Action which will start a SSH Tunnel via the Sauce REST API if the build is configured to run
 * Selenium tests via the Sauce Connect tunnel.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class BuildConfigurator extends BaseConfigurableBuildPlugin implements CustomPreBuildAction {

    private static final Logger logger = Logger.getLogger(BuildConfigurator.class);



    private SauceConnectFourManager sauceConnectFourTunnelManager;

    public CustomVariableContext getCustomVariableContext() {
        return customVariableContext;
    }

    private CustomVariableContext customVariableContext;

    /**
     * Populated via dependency injection.
     */
    private AdministrationConfigurationAccessor administrationConfigurationAccessor;

    /**
     * Populated via dependency injection.
     */
    private BrowserFactory sauceBrowserFactory;

    /**
     * Populated via dependency injection.
     */
    private PlanManager planManager;

    private static final Browser DEFAULT_BROWSER = new Browser("unknown", "unknown", "unknown", "unknown", "unknown", "unknown", "ERROR Retrieving Browser List!");
    private static final String DEFAULT_MAX_DURATION = "300";
    private static final String DEFAULT_IDLE_TIMEOUT = "90";
    private static final String DEFAULT_SELENIUM_URL = "http://saucelabs.com";
    private static final String DEFAULT_SSH_LOCAL_HOST = "localhost";
    private static final String DEFAULT_SSH_LOCAL_PORT = "8080";
    private static final String DEFAULT_SELENIUM_VERSION = SeleniumVersion.TWO.getVersionNumber();

    /**
     * Entry point into build action.
     *
     * @return FIXME - ???
     */
    @NotNull
    @Override
    public BuildContext call() {
        BuildLoggerManager buildLoggerManager = (BuildLoggerManager) ContainerManager.getComponent("buildLoggerManager");
        BuildLogger buildLogger = buildLoggerManager.getLogger(buildContext.getResultKey());
        try {
            final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
            SauceLogInterceptor logInterceptor = new SauceLogInterceptor(buildContext);
            buildLogger.getInterceptorStack().add(logInterceptor);
            if (config.isEnabled() && config.isSauceConnectEnabled()) {
                startTunnel(config);
            }
        } catch (AbstractSauceTunnelManager.SauceConnectException e) {
            buildLogger.addErrorLogEntry("Sauce Connect Error", e);
        } catch (Exception e) {
            //catch exceptions so that we don't stop the build from running
            logger.error("Error running Sauce OnDemand BuildConfigurator, attempting to continue", e);
        }
        return buildContext;
    }

    protected SauceREST getSauceREST(SODMappedBuildConfiguration config) {
        return new SauceREST(config.getTempUsername(), config.getTempApikey(), config.getTempDatacenter());
    }

    /**
     * Opens the tunnel and adds the tunnel instance to the sauceTunnelManager map.
     *
     * @param config Configuration as provided for the job
     * @throws IOException when unable to create tunnel for various reasons
     */
    public void startTunnel(SODMappedBuildConfiguration config) throws IOException {
        BuildLoggerManager buildLoggerManager = (BuildLoggerManager) ContainerManager.getComponent("buildLoggerManager");
        final BuildLogger buildLogger = buildLoggerManager.getLogger(buildContext.getResultKey());
        PrintStream printLogger = new PrintStream(new NullOutputStream()) {
            @Override
            public void println(String x) {
                buildLogger.addBuildLogEntry(x);
            }
        };
        SauceTunnelManager sauceTunnelManager = SauceConnectFourManagerSingleton.getSauceConnectFourTunnelManager();
        String options = getResolvedOptions(config.getSauceConnectOptions());
        if (config.useGeneratedTunnelIdentifier()) {
            VariableDefinitionContext key = getCustomVariableContext().getVariableContexts().get(SODKeys.TUNNEL_IDENTIFIER);
            String tunnelIdentifier = key == null ? null : key.getValue();
            options = "--tunnel-identifier " + tunnelIdentifier + " " + options;
        }

        SauceREST sauceREST = getSauceREST(config);
        options = "-x " + sauceREST.getServer() + "rest/v1" + " " + options;

        AdministrationConfiguration adminConfig = getAdministrationConfigurationAccessor().getAdministrationConfiguration();
        int maxRetries = 0, retryWaitTime = 0, retryCount = 0;
        try {
            maxRetries = Integer.parseInt(adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_MAX_RETRIES));
        } catch (NumberFormatException e) {
            maxRetries = 0;
        }
        try {
            retryWaitTime = Integer.parseInt(adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_RETRY_WAIT_TIME));
        } catch (NumberFormatException e) {
            retryWaitTime = 0;
        }

        // set to use latest sauce connect if set
        ((SauceConnectFourManager) sauceTunnelManager).setUseLatestSauceConnect(config.useLatestSauceConnect());

        do {
            try {
                sauceTunnelManager.openConnection(
                    config.getTempUsername(),
                    config.getTempApikey(),
                    Integer.parseInt(config.getSshPorts()),
                    null,
                    options,
                    printLogger,
                    config.isVerboseSSHLogging(),
                    getSauceConnectDirectory()
                );
                break;
            } catch (AbstractSauceTunnelManager.SauceConnectDidNotStartException e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    throw new AbstractSauceTunnelManager.SauceConnectException(e);
                } else {
                    buildLogger.addBuildLogEntry(String.format("Error launching Sauce Connect, trying %s time(s) more.", (maxRetries - retryCount)));
                }
                if (retryWaitTime > 0) {
                    try {
                        Thread.sleep(1000 * retryWaitTime);
                    } catch (InterruptedException ie) {
                        throw new AbstractSauceTunnelManager.SauceConnectException(ie);
                    }
                }
            }
        }
        while (retryCount <= maxRetries + 1);
    }

    private String getResolvedOptions(String sauceConnectOptions) {
        String options = sauceConnectOptions;
        if (options != null) {
            return customVariableContext.substituteString(options);
        }
        return "";
    }


    /**
     * Populates the <code>context</code> parameter with information to be presented on the 'Edit Configuration' screen.  The
     * list of available Browser types is included in the context.  If an exception occurs during the retrieval of browser information
     * (eg. if a network error occurs retrieving the browser information), then a series of 'unknown' browsers will be added.
     */
    @Override
    protected void populateContextForEdit(final Map<String, Object> context, final BuildConfiguration buildConfiguration, final Plan build) {
        populateCommonContext(context);
        populateSauceConnectVersion(context);
        try {
            String[] selectedBrowsers = getSelectedBrowsers(buildConfiguration);
            ValueStack stack = ActionContext.getContext().getValueStack();
            stack.getContext().put("selectedBrowsers", selectedBrowsers);
            context.put("selectedBrowsers", selectedBrowsers);

            context.put("webDriverBrowserList", getSauceBrowserFactory().getWebDriverBrowsers());
            context.put("appiumBrowserList", getSauceBrowserFactory().getAppiumBrowsers());
        } catch (Exception e) {
            //TODO detect a proxy exception as opposed to all exceptions?
            populateDefaultBrowserList(context);
        }
    }

    private String[] getSelectedBrowsers(BuildConfiguration buildConfiguration) throws Exception {
        List<Browser> browsers;
        List<String> selectedBrowsers = new ArrayList<String>();
        String[] selectedKeys = SODMappedBuildConfiguration.fromString(buildConfiguration.getString(SODKeys.BROWSER_KEY));

        browsers = getSauceBrowserFactory().getWebDriverBrowsers();

        for (Browser browser : browsers) {
            if (ArrayUtils.contains(selectedKeys, browser.getKey())) {
                selectedBrowsers.add(browser.getKey());
            }
        }
        return selectedBrowsers.toArray(new String[selectedBrowsers.size()]);
    }

    private void populateDefaultBrowserList(Map<String, Object> context) {
        context.put("browserList", Collections.singletonList(DEFAULT_BROWSER));
    }

    /**
     * Adds a series of default values to the build configuration.  Default values are only supplied if values
     * don't already exist in the configuration.
     */
    @Override
    public void addDefaultValues(@NotNull BuildConfiguration buildConfiguration) {
        super.addDefaultValues(buildConfiguration);

        //only set SSH enabled if we don't have any properties set
        if (!buildConfiguration.getKeys(SODKeys.CUSTOM_PREFIX).hasNext()) {
            addDefaultStringValue(buildConfiguration, SODKeys.SSH_ENABLED_KEY, Boolean.TRUE.toString());
            addDefaultStringValue(buildConfiguration, SODKeys.SSH_VERBOSE_KEY, Boolean.FALSE.toString());

        }
        addDefaultNumberValue(buildConfiguration, SODKeys.MAX_DURATION_KEY, DEFAULT_MAX_DURATION);
        addDefaultNumberValue(buildConfiguration, SODKeys.IDLE_TIMEOUT_KEY, DEFAULT_IDLE_TIMEOUT);
        addDefaultStringValue(buildConfiguration, SODKeys.SELENIUM_VERSION_KEY, DEFAULT_SELENIUM_VERSION);
        addDefaultStringValue(buildConfiguration, SODKeys.RECORD_VIDEO_KEY, Boolean.TRUE.toString());
        addDefaultStringValue(buildConfiguration, SODKeys.SELENIUM_URL_KEY, DEFAULT_SELENIUM_URL);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_LOCAL_HOST_KEY, DEFAULT_SSH_LOCAL_HOST);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_LOCAL_PORTS_KEY, DEFAULT_SSH_LOCAL_PORT);

    }

    private void addDefaultNumberValue(BuildConfiguration buildConfiguration, String configurationKey, String defaultValue) {
        if (!NumberUtils.isNumber(buildConfiguration.getString(configurationKey))) {
            buildConfiguration.setProperty(configurationKey, defaultValue);
        }
    }

    private void addDefaultStringValue(BuildConfiguration buildConfiguration, String configurationKey, String defaultValue) {
        if (StringUtils.isBlank(buildConfiguration.getString(configurationKey))) {
            buildConfiguration.setProperty(configurationKey, defaultValue);
        }
    }

    private void populateCommonContext(final Map<String, Object> context) {
        context.put("hasValidSauceConfig", hasValidSauceConfig());
    }

    /**
     * @return boolean indicating whether the Sauce configuration specified in the administration interface
     */
    public boolean hasValidSauceConfig() {
        AdministrationConfiguration adminConfig = administrationConfigurationAccessor.getAdministrationConfiguration();
        return (StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY))
                && StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY)));
    }

    private void populateSauceConnectVersion(final Map<String, Object> context) {
        String sauceConnectVersion = SauceConnectFourManagerSingleton.getSauceConnectFourTunnelManager().CURRENT_SC_VERSION;
        context.put("currentSauceConnectVersion", sauceConnectVersion);
    }

    public AdministrationConfigurationAccessor getAdministrationConfigurationAccessor() {
        return administrationConfigurationAccessor;
    }

    public void setAdministrationConfigurationAccessor(AdministrationConfigurationAccessor administrationConfigurationAccessor) {
        this.administrationConfigurationAccessor = administrationConfigurationAccessor;
    }

    public void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory) {
        this.sauceBrowserFactory = sauceBrowserFactory;
    }

    public BrowserFactory getSauceBrowserFactory() {
        if (sauceBrowserFactory == null) {
            setSauceBrowserFactory(BrowserFactory.getInstance());
        }
        return sauceBrowserFactory;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }

    public String getSauceConnectDirectory() {
        if (administrationConfigurationAccessor == null) { return null; }
        AdministrationConfiguration adminConfig = administrationConfigurationAccessor.getAdministrationConfiguration();
        if (adminConfig == null) { return null; }
        return adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_DIRECTORY);
    }

}
