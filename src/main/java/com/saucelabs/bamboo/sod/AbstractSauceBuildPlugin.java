package com.saucelabs.bamboo.sod;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.Bamboo3Modifier;
import com.saucelabs.bamboo.sod.variables.VariableModifier;
import com.atlassian.bamboo.variable.CustomVariableContext;

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
     * @param config        Mapped config for project
     * @param definition    Project definition
     * @param environmentVariableAccessor passthrough to Bamboo3Modifier
     * @param customVariableContext       FIXME -- ??
     * @return instance that will be used to construct the environment variables
     */
    protected VariableModifier getVariableModifier(
        SODMappedBuildConfiguration config,
        BuildDefinition definition,
        EnvironmentVariableAccessor environmentVariableAccessor,
        CustomVariableContext customVariableContext
    ) {
        return new Bamboo3Modifier(config, definition, buildContext, environmentVariableAccessor, customVariableContext);
    }
}
