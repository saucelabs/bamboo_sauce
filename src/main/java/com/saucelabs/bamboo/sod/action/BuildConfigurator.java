package com.sysbliss.bamboo.sod.action;

import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import com.sysbliss.bamboo.sod.Browser;
import com.sysbliss.bamboo.sod.BrowserFactory;
import com.sysbliss.bamboo.sod.config.*;
import com.sysbliss.bamboo.sod.util.SauceFactory;
import com.sysbliss.bamboo.sod.util.SauceTunnelManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pre-Build Action which will start a SSH Tunnel via the Sauce REST API if the build is configured to run
 * tests via the tunnel. 
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class BuildConfigurator extends BaseConfigurableBuildPlugin implements CustomPreBuildAction {

    /**
     * Populated via dependency injection.
     */
    private SauceTunnelManager sauceTunnelManager;

    /**
     * Populated by dependency injection.
     */
    private SauceFactory sauceAPIFactory;
    // TODO: set a real timeout
    /**
     * Timeout is hard-coded to 180 seconds.
     */
    private static final int SSH_TIMEOUT = 180 * 1000;
    /**
     * Populated via dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;
    /**
     * Populated via dependency injection.
     */
    private BrowserFactory sauceBrowserFactory;
    private static final Browser DEFAULT_BROWSER = new Browser("unknown", "unknown", "unknown", "unknown", "ERROR Retrieving Browser List!");
    private static final String DEFAULT_MAX_DURATION = "300";
    private static final String DEFAULT_IDLE_TIMEOUT = "90";
    private static final String DEFAULT_SELENIUM_URL = "http://saucelabs.com";
    private static final String DEFAULT_SSH_LOCAL_HOST = "localhost";
    private static final String DEFAULT_SSH_LOCAL_PORT = "8080";
    private static final String DEFAULT_SSH_REMOTE_PORT = "80";
    private static final String DEFAULT_SSH_DOMAIN = "AUTO";

    @NotNull
    public BuildContext call() throws IOException {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled() && config.isSshEnabled()) {
            startTunnel(config.getTempUsername(), config.getTempApikey(), config.getSshHost(), config.getSshPorts(), config.getSshTunnelPorts(), config.getSshDomains(), config.isAutoDomain());
        }
        return buildContext;
    }

    public void startTunnel(String username, String apiKey, String localHost, String localPorts, String remotePorts, String sshDomains, boolean autoDomain) throws IOException {
        String finalDomain = sshDomains;
        if (autoDomain) {
            finalDomain = "bamboo-" + buildContext.getPlanKey() + ".bamboo";
        }

        int intLocalPort = Integer.parseInt(localPorts);
        int intRemotePort = Integer.parseInt(remotePorts);

        List<String> domainList = Collections.singletonList(finalDomain);

        SauceTunnelFactory tunnelFactory = sauceAPIFactory.createSauceTunnelFactory(username, apiKey);
        SauceTunnel tunnel = tunnelFactory.create(domainList);

        if (tunnel != null) {
            try {
                tunnel.waitUntilRunning(SSH_TIMEOUT);
                if (!tunnel.isRunning()) {
                    throw new IOException("Sauce OnDemand Tunnel didn't come online. Aborting.");
                }
            } catch (InterruptedException e) {
                throw new IOException("Sauce OnDemand Tunnel Aborted.");
            }
            tunnel.connect(intRemotePort, localHost, intLocalPort);
        }

        sauceTunnelManager.addTunnelToMap(buildContext.getPlanKey(), tunnel);
    }


    /**
     * Populates the <code>context</code> parameter with information to be presented on the 'Edit Configuration' screen.  The
     * list of available Browser types is included in the context.  If an exception occurs during the retrieval of browser information
     * (eg. if a network error occurs retrieving the browser information), then a series of 'unknown' browsers will be added.
     */
    @Override
    protected void populateContextForEdit(final Map<String, Object> context, final BuildConfiguration buildConfiguration, final Plan build) {
        populateCommonContext(context);
        try {
            context.put("browserList", sauceBrowserFactory.values());
        } catch (IOException e) {
            //TODO are there a set of default browsers that we can use?
            //TODO detect a proxy exception as opposed to all exceptions?
            populateDefaultBrowserList(context);
        } catch (JSONException e) {
            populateDefaultBrowserList(context);
        }
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

        addDefaultNumberValue(buildConfiguration, SODKeys.MAX_DURATION_KEY, DEFAULT_MAX_DURATION);
        addDefaultNumberValue(buildConfiguration, SODKeys.IDLE_TIMEOUT_KEY, DEFAULT_IDLE_TIMEOUT);
        addDefaultStringValue(buildConfiguration, SODKeys.RECORD_VIDEO_KEY, Boolean.TRUE.toString());
        addDefaultStringValue(buildConfiguration, SODKeys.SELENIUM_URL_KEY, DEFAULT_SELENIUM_URL);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_ENABLED_KEY, Boolean.TRUE.toString());
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_LOCAL_HOST_KEY, DEFAULT_SSH_LOCAL_HOST);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_LOCAL_PORTS_KEY, DEFAULT_SSH_LOCAL_PORT);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_REMOTE_PORTS_KEY, DEFAULT_SSH_REMOTE_PORT);
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_AUTO_DOMAIN_KEY, Boolean.TRUE.toString());
        addDefaultStringValue(buildConfiguration, SODKeys.SSH_DOMAINS_KEY, DEFAULT_SSH_DOMAIN);

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
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        return (StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY))
                && StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY))
                && StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SELENIUM_HOST_KEY))
                && StringUtils.isNotBlank(adminConfig.getSystemProperty(SODKeys.SELENIUM_PORT_KEY)));
    }

    public AdministrationConfigurationManager getAdministrationConfigurationManager() {
        return administrationConfigurationManager;
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public void setSauceTunnelManager(SauceTunnelManager sauceTunnelManager) {
        this.sauceTunnelManager = sauceTunnelManager;
    }

    public void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory) {
        this.sauceBrowserFactory = sauceBrowserFactory;
    }

    public void setSauceAPIFactory(SauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }


}
