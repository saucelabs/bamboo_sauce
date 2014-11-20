package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the TaskDefinition instance (for Bamboo 3 instances).
 * The variables are saved to the plan's configuration, and are removed by the {@link PostBuildAction} class.
 *
 * @author Ross Rowe
 */
public class Bamboo3Modifier extends DefaultVariableModifier {

    public Bamboo3Modifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        super(config, definition, buildContext);
    }


    public void populateVariables(VariableContext variableContext) {
        createSelenium2VariableContext(variableContext);
    }

    /**
     * {@inheritDoc}
     */
    public void storeVariables() {
        String envBuffer = createSeleniumEnvironmentVariables();
        try {
            Class taskDefinitionClass = TaskDefinition.class;
            if (taskDefinitionClass != null) {
                List<TaskDefinition> taskDefinitions = definition.getTaskDefinitions();
                for (TaskDefinition taskDefinition : taskDefinitions) {
                    Map<String, String> configuration = taskDefinition.getConfiguration();
                    String originalEnv = configuration.get("environmentVariables");
                    if (StringUtils.isNotBlank(originalEnv)) {
                        envBuffer = originalEnv + " " + envBuffer;
                    }

                    config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
                    configuration.put("environmentVariables", envBuffer);
                }
            }
        } catch (Exception e) {
            //ignore and attempt to continue
        }
    }


}
