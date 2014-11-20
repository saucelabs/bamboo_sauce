package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.buildqueue.manager.CustomPreBuildQueuedAction;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.VariableModifier;
import com.saucelabs.ci.BrowserFactory;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Entry point into build action.
     *
     * @return
     */
    @NotNull
    //@Override
    public BuildContext call() {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled()) {
            setSeleniumEnvironmentVars(config);
        }
        return buildContext;
    }

    /**
     * @param config
     */
    private void setSeleniumEnvironmentVars(SODMappedBuildConfiguration config){
        VariableModifier variableModifier = getVariableModifier(config, buildContext.getBuildDefinition());
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

}
