package com.sysbliss.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractMavenBuilder;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.sysbliss.bamboo.sod.BrowserFactory;
import com.sysbliss.bamboo.sod.config.SODKeys;
import com.sysbliss.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 * @author Ross Rowe
 */
public class MavenVariableModifier extends DefaultVariableModifier {
    
    public MavenVariableModifier(SODMappedBuildConfiguration config, AdministrationConfigurationManager administrationConfigurationManager, BuildDefinition definition, BuildContext buildContext, BrowserFactory sauceBrowserFactory) {
        super(config, administrationConfigurationManager, definition, buildContext, sauceBrowserFactory);
    }

    public MavenVariableModifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        super(config, definition, buildContext);
    }

    @Override
    public void storeVariables() throws JSONException {
        AbstractMavenBuilder builder = (AbstractMavenBuilder) definition.getBuilder();
        if (builder != null) {
            String originalEnv = builder.getGoal();
            config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
            String envBuffer = createSeleniumEnvironmentVariables();
            if (StringUtils.isNotBlank(originalEnv)) {
                envBuffer = " " + envBuffer;
            }
            builder.setGoal(builder.getGoal() + envBuffer);
        }
    }

    @Override
    public void restoreVariables() {
        AbstractMavenBuilder builder = (AbstractMavenBuilder) definition.getBuilder();
        builder.setGoal(config.getMap().get(SODKeys.TEMP_ENV_VARS));
        config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
    }
}
