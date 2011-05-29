package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.VariableModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Invoked after a build has finished to reset the environment variables for the builder back to what they were prior
 * to the invocation of Sauce.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class PostBuildAction extends AbstractSauceBuildPlugin implements CustomBuildProcessorServer {
    private PlanManager planManager;

    @NotNull
    public BuildContext call() {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled()) {
            resetEnvironmentVariables(config);
            recordSauceJobResult();
        }
        return buildContext;
    }

    public void init(@NotNull BuildContext context) {
        this.buildContext = context;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    private void recordSauceJobResult() {
    }

    private void resetEnvironmentVariables(final SODMappedBuildConfiguration config) {
        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        if (plan != null) {
            VariableModifier variableModifier = getVariableModifier(config, plan);
            if (variableModifier != null) {
                variableModifier.restoreVariables();
                planManager.savePlan(plan);
            }
        }
    }
}
