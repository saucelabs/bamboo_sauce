package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractMavenBuilder;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.BrowserFactory;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 * @author Ross Rowe
 */
public class MavenVariableModifier extends DefaultVariableModifier {
    
    public MavenVariableModifier(SODMappedBuildConfiguration config, BuildDefinition definition, BuildContext buildContext) {
        super(config, definition, buildContext);
    }

//    @Override
//    public void storeVariables() throws JSONException {
//        AbstractMavenBuilder builder = (AbstractMavenBuilder) definition.getBuilder();
//        if (builder != null) {
//            String originalEnv = builder.getGoal();
//            config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
//            String envBuffer = createSeleniumEnvironmentVariables("-D");
//            if (StringUtils.isNotBlank(originalEnv)) {
//                envBuffer = envBuffer + " ";
//            }
//            builder.setGoal(envBuffer + builder.getGoal());
//        }
//    }
//
//    @Override
//    public void restoreVariables() {
//        AbstractMavenBuilder builder = (AbstractMavenBuilder) definition.getBuilder();
//        builder.setGoal(config.getMap().get(SODKeys.TEMP_ENV_VARS));
//        config.getMap().put(SODKeys.TEMP_ENV_VARS, "");
//    }
//
//    @Override
//    protected String createSeleniumEnvironmentVariables(String prefix) throws JSONException {
//        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
//        String sodUsername = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
//        String sodKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
//        String host = adminConfig.getSystemProperty(SODKeys.SELENIUM_HOST_KEY);
//        String port = adminConfig.getSystemProperty(SODKeys.SELENIUM_PORT_KEY);
//        String browserUrl = config.getSeleniumStartingUrl();
//        String browserJson = getSodJson(sodUsername, sodKey, config);
//        String sodDriverURI = getSodDriverUri(sodUsername, sodKey, config);
//
//        config.setTempUsername(sodUsername);
//        config.setTempApikey(sodKey);
//
//        StringBuilder envBuffer = new StringBuilder();
//
//        String sodHost = config.getSshDomains();
//        String finalStartingUrl = browserUrl;
//
//        if (config.isAutoDomain()) {
//            sodHost = "bamboo-" + buildContext.getPlanKey() + ".bamboo";
//            finalStartingUrl = "http://" + sodHost + ':' + config.getSshTunnelPorts() + '/';
//        }
//
//        envBuffer.append("-DargLine=\"");
//
//        envBuffer.append(prefix).append(SODKeys.SELENIUM_HOST_ENV).append("=\\\"").append(host).append("\\\"");
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV).append('=').append(port);
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browserJson).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV).append(EQUALS).append(finalStartingUrl).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_ONDEMAND_HOST).append(EQUALS).append(sodHost).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV).append(EQUALS).append(sodDriverURI).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_HOST_ENV_LEGACY).append(EQUALS).append(host).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_PORT_ENV_LEGACY).append('=').append(port);
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_BROWSER_ENV_LEGACY).append(EQUALS).append(browserJson).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_STARTING_URL_ENV_LEGACY).append(EQUALS).append(finalStartingUrl).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_ONDEMAND_HOST_LEGACY).append(EQUALS).append(sodHost).append('"');
////        envBuffer.append(' ').append(prefix).append(SODKeys.SELENIUM_DRIVER_ENV_LEGACY).append(EQUALS).append(sodDriverURI).append('"');
////
////        if (buildContext.getParentBuildContext() == null) {
////            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
////                    String.format(CUSTOM_DATA, buildContext.getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getBuildResultKey())).append('"');
////        } else {
////            envBuffer.append(' ').append(prefix).append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
////                    String.format(CUSTOM_DATA, buildContext.getParentBuildContext().getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getParentBuildContext().getBuildResultKey())).append('"');
////        }
//        envBuffer.append("\"");
//        return envBuffer.toString();
//    }
}
