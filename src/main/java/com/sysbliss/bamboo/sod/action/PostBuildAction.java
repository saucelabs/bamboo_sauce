package com.sysbliss.bamboo.sod.action;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.sysbliss.bamboo.sod.AbstractSauceBuildPlugin;
import com.sysbliss.bamboo.sod.config.SODKeys;
import com.sysbliss.bamboo.sod.config.SODMappedBuildConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Invoked after a build has finished to reset the environment variables for the builder back to what they were prior
 * to the invocation of Sauce.
 * 
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class PostBuildAction extends AbstractSauceBuildPlugin implements CustomBuildProcessorServer
{
    private BuildContext buildContext;
    private PlanManager planManager;

    @NotNull
    public BuildContext call()
    {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled())
        {
            resetEnvironmentVariables(config);
            recordSauceJobResult();
        }

        return buildContext;
    }

    public void init(@NotNull BuildContext context)
    {
        this.buildContext = context;
    }


    public void setPlanManager(PlanManager planManager)
    {
        this.planManager = planManager;
    }

    private void recordSauceJobResult() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void resetEnvironmentVariables(final SODMappedBuildConfiguration config) {

        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        assert plan != null;
        AbstractBuilder builder = (AbstractBuilder) plan.getBuildDefinition().getBuilder();
        assert builder != null;
        builder.setEnvironmentVariables(config.getMap().get(SODKeys.TEMP_ENV_VARS));
        planManager.savePlan(plan);

        config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
    }
}
