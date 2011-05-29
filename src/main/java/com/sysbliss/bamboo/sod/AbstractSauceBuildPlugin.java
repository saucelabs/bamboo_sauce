
package com.sysbliss.bamboo.sod;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.builder.AbstractMavenBuilder;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.sysbliss.bamboo.sod.config.SODMappedBuildConfiguration;
import com.sysbliss.bamboo.sod.variables.Bamboo3Modifier;
import com.sysbliss.bamboo.sod.variables.DefaultVariableModifier;
import com.sysbliss.bamboo.sod.variables.MavenVariableModifier;
import com.sysbliss.bamboo.sod.variables.VariableModifier;

/**
 *
 * @author Ross Rowe
 */
public abstract class AbstractSauceBuildPlugin extends BaseConfigurableBuildPlugin {

    protected VariableModifier getVariableModifier(SODMappedBuildConfiguration config, Plan plan) {
        VariableModifier variableModifier = null;
        //try the task definitions
        BuildDefinition definition = plan.getBuildDefinition();
        try {
            Class taskDefinitionClass = Class.forName("com.atlassian.bamboo.task.TaskDefinition");
            if (taskDefinitionClass != null) {
                variableModifier = new Bamboo3Modifier(config, definition, buildContext);
            }
        } catch (Exception e) {
            //ignore and attempt to continue
        }

        //legacy,pre-Bamboo 3 support
        AbstractBuilder builder = (AbstractBuilder) definition.getBuilder();
        if (builder != null) {
            if (builder instanceof AbstractMavenBuilder) {
                variableModifier = new MavenVariableModifier(config, definition, buildContext);
            } else {
                variableModifier = new DefaultVariableModifier(config, definition, buildContext);
            }
        }

        return variableModifier;

    }
}
