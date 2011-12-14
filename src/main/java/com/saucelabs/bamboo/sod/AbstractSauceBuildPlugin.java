package com.saucelabs.bamboo.sod;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.Bamboo3Modifier;
import com.saucelabs.bamboo.sod.variables.DefaultVariableModifier;
import com.saucelabs.bamboo.sod.variables.VariableModifier;

/**
 * Contains common logic for Sauce OnDemand plugin classes.
 *
 * @author Ross Rowe
 */
public abstract class AbstractSauceBuildPlugin extends BaseConfigurableBuildPlugin {

    /**
     * Return a new {@link VariableModifier} instance that will be used to construct the environment
     * variables.
     *
     * @param config
     * @param plan
     * @return
     */
    protected VariableModifier getVariableModifier(SODMappedBuildConfiguration config, Plan plan) {
        //try the task definitions
        BuildDefinition definition = plan.getBuildDefinition();

        return new Bamboo3Modifier(config, definition, buildContext);

    }
}
