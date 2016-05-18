package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.buildqueue.manager.CustomPreBuildQueuedAction;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.credentials.CredentialsAccessor;
import com.atlassian.bamboo.credentials.CredentialsData;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.repository.NameValuePair;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.VariableModifier;
import com.saucelabs.ci.BrowserFactory;
import com.atlassian.bamboo.variable.CustomVariableContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Pre Build Action which generates and adds the Selenium environment variables that apply to the build
 * plan to the builder.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */

public class EnvironmentConfigurator extends AbstractSauceBuildPlugin implements CustomPreBuildQueuedAction {

    /**
     * Populated via dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    /**
     * Populated via dependency injection.
     */
    private BrowserFactory sauceBrowserFactory;
    private EnvironmentVariableAccessor environmentVariableAccessor;
    private CustomVariableContext customVariableContext;

    public EnvironmentConfigurator() {
        super();
    }

    public CustomVariableContext getCustomVariableContext() {
        return customVariableContext;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }

    /**
     * Entry point into build action.
     *
     * @return ??? FIXME
     */
    @NotNull
    @Override
    public BuildContext call() {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        // setting unique tunnel identifier
        if (config.isEnabled() && config.useGeneratedTunnelIdentifier()) {
            String tunnelIdentifier = generateTunnelIdentifier(buildContext.getPlanName());
            customVariableContext.addCustomData(SODKeys.TUNNEL_ID, tunnelIdentifier);
        }

        if (config.isEnabled()) {
            setSeleniumEnvironmentVars(config);
        }
        return buildContext;
    }

    /**
     * @param config
     */
    private void setSeleniumEnvironmentVars(SODMappedBuildConfiguration config){
        VariableModifier variableModifier = getVariableModifier(config, buildContext.getBuildDefinition(), environmentVariableAccessor, customVariableContext);
        variableModifier.setAdministrationConfigurationManager(administrationConfigurationManager);
        variableModifier.setSauceBrowserFactory(getSauceBrowserFactory());
        variableModifier.storeVariables();
        variableModifier.populateVariables(buildContext.getVariableContext());
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
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

    public EnvironmentVariableAccessor getEnvironmentVariableAccessor() {
        return environmentVariableAccessor;
    }

    public void setEnvironmentVariableAccessor(EnvironmentVariableAccessor environmentVariableAccessor) {
        this.environmentVariableAccessor = environmentVariableAccessor;
    }
    //only allow word, digit, and hyphen characters
    private final String PATTERN_DISALLOWED_TUNNEL_ID_CHARS = "[^\\w\\d-]+";

    private String generateTunnelIdentifier(final String projectName) {
        String sanitizedName = projectName.replaceAll(PATTERN_DISALLOWED_TUNNEL_ID_CHARS, "_");
        return sanitizedName + "-" + System.currentTimeMillis();
    }

}
