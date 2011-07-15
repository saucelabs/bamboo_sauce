package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.BrowserFactory;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the TaskDefinition instance (for Bamboo 3 instances).  
 * The variables are saved to the plan's configuration, and are removed by the {@link PostBuildAction} class.
 * @author Ross Rowe
 */
public class Bamboo3Modifier extends DefaultVariableModifier  {

    public Bamboo3Modifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        super(config, definition, buildContext);
    }

    public void storeVariables() throws JSONException {
        String envBuffer = createSeleniumEnvironmentVariables();
        try {
            Class taskDefinitionClass = Class.forName("com.atlassian.bamboo.task.TaskDefinition");
            if (taskDefinitionClass != null) {
                Method taskDefinitionsMethod = BuildDefinition.class.getMethod("getTaskDefinitions", null);
                List/*<TaskDefinition>*/ taskDefinitions = (List/*<TaskDefinition>*/) taskDefinitionsMethod.invoke(definition, null);
                for (Object taskDefinition : taskDefinitions) {
                    Method method = taskDefinitionClass.getMethod("getConfiguration");
                    Map<String, String> configuration = (Map<String, String>) method.invoke(taskDefinition);
                    String originalEnv = configuration.get("environmentVariables");
                    if (StringUtils.isNotBlank(originalEnv)) {
                        envBuffer = " " + envBuffer;
                    }

                    config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
                    configuration.put("environmentVariables", originalEnv + envBuffer);
                }
            }
        } catch (Exception e) {
            //ignore and attempt to continue
        }
    }

    @Override
    public void restoreVariables() {
        try {
            Class taskDefinitionClass = Class.forName("com.atlassian.bamboo.task.TaskDefinition");
            if (taskDefinitionClass != null) {
                Method taskDefinitionsMethod = BuildDefinition.class.getMethod("getTaskDefinitions", null);
                List/*<TaskDefinition>*/ taskDefinitions = (List/*<TaskDefinition>*/) taskDefinitionsMethod.invoke(definition, null);
                for (Object taskDefinition : taskDefinitions) {
                    Method method = taskDefinitionClass.getMethod("getConfiguration");
                    Map<String, String> configuration = (Map<String, String>) method.invoke(taskDefinition);
                    configuration.put("environmentVariables", config.getMap().get(SODKeys.TEMP_ENV_VARS));
                    config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
                }
            }
        } catch (Exception e) {
            //ignore and attempt to continue
        }
    }

}
