package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.EnvironmentVariableAccessorImpl;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.sal.api.component.ComponentLocator;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.saucelabs.bamboo.sod.action.PostBuildAction;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Handles writing and restoring the Sauce OnDemand environment variables to the TaskDefinition instance (for Bamboo 3 instances).
 * The variables are saved to the plan's configuration, and are removed by the {@link PostBuildAction} class.
 *
 * @author Ross Rowe
 */
public class Bamboo3Modifier extends DefaultVariableModifier {
    private static final Logger logger = Logger.getLogger(Bamboo3Modifier.class);

    private final EnvironmentVariableAccessor environmentVariableAccessor;

    /*
   * Directly copied from EnvironmentVariableAccessorImpl
   * and modified to wrap everything in quotes
   */
    private static enum CreateEnvironmentAssignment implements Function<Map.Entry<String, String>, String> {
        INSTANCE;
        private CreateEnvironmentAssignment() {
        }

        public String apply(@Nullable Map.Entry<String, String> input) {
            return String.format("%s=\"%s\"", new Object[]{
                EnvironmentVariableAccessorImpl.forceLegalIdentifier((String)((Map.Entry) Preconditions.checkNotNull(input)).getKey()),
                ((Map.Entry)Preconditions.checkNotNull(input)).getValue()
            });
        }
    }

    public Bamboo3Modifier(
        SODMappedBuildConfiguration config,
        BuildDefinition definition,
        BuildContext buildContext,
        EnvironmentVariableAccessor environmentVariableAccessor,
        CustomVariableContext customVariableContext
    ) {
        super(config, definition, buildContext, customVariableContext);
        this.environmentVariableAccessor = environmentVariableAccessor;
    }


    public void populateVariables(VariableContext variableContext) {
        createSelenium2VariableContext(variableContext);
    }

    /**
     * {@inheritDoc}
     */
    public void storeVariables() {
        String envBuffer = createSeleniumEnvironmentVariables();
        Map<String, String> envMap = environmentVariableAccessor.splitEnvironmentAssignments(envBuffer, false);

        try {
            Class taskDefinitionClass = TaskDefinition.class;
            if (taskDefinitionClass != null) {
                List<TaskDefinition> taskDefinitions = definition.getTaskDefinitions();
                for (TaskDefinition taskDefinition : taskDefinitions) {
                    Map<String, String> configuration = taskDefinition.getConfiguration();
                    String originalEnv = StringUtils.defaultString((String) configuration.get("environmentVariables"));

                    Map<String, String> origMap = environmentVariableAccessor.splitEnvironmentAssignments(originalEnv, false);
                    for (Map.Entry<String, String> entry : envMap.entrySet())
                    {
                        if (entry.getKey().startsWith("SELENIUM_") || entry.getKey().startsWith("SAUCE_") || entry.getKey().equals(SODKeys.TUNNEL_IDENTIFIER)) {
                            origMap.put(entry.getKey(), "${bamboo." + entry.getKey() + "}");

                        }
                    }

                    configuration.put(
                        "environmentVariables",
                        joinEnvMap(origMap)
                    );
                }
            }
        } catch (Exception e) {
            //ignore and attempt to continue
            logger.warn("Unable to process environment variables", e);
        }
    }

    /*
     * Directly copied from EnvironmentVariableAccessorImpl
     * and modified to wrap everything in quotes
     */
    @Nullable
    private static String joinEnvMap(Map<String, String> origMap) {
        return org.apache.commons.lang3.StringUtils.join(Iterables.transform(
            origMap.entrySet(),
            CreateEnvironmentAssignment.INSTANCE).iterator(),
            " "
        );
    }
}
