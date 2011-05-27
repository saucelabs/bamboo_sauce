package com.sysbliss.bamboo.sod.action;

import com.atlassian.bamboo.buildqueue.manager.CustomPreBuildQueuedAction;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.sysbliss.bamboo.sod.AbstractSauceBuildPlugin;
import com.sysbliss.bamboo.sod.BrowserFactory;
import com.sysbliss.bamboo.sod.config.SODMappedBuildConfiguration;
import com.sysbliss.bamboo.sod.variables.VariableModifier;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

/**
 * Pre Build Action which generates and adds the Selenium environment variables that apply to the build
 * plan to the builder.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class EnvironmentConfigurator extends AbstractSauceBuildPlugin implements CustomPreBuildQueuedAction {

    private AdministrationConfigurationManager administrationConfigurationManager;
    private PlanManager planManager;
    private BrowserFactory sauceBrowserFactory;

    @NotNull
    public BuildContext call() throws JSONException {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());

        if (config.isEnabled()) {
            setSeleniumEnvironmentVars(config);
        }

        return buildContext;
    }

    private void setSeleniumEnvironmentVars(SODMappedBuildConfiguration config) throws JSONException {
        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        if (plan != null) {
            VariableModifier variableModifier = getVariableModifier(config, plan);
            if (variableModifier != null) {
                variableModifier.storeVariables();
                planManager.savePlan(plan);
            }
        }
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public PlanManager getPlanManager() {
        return planManager;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory) {
        this.sauceBrowserFactory = sauceBrowserFactory;
    }
}
