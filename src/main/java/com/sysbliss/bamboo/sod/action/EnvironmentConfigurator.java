package com.sysbliss.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.builder.AbstractBuilder;
import com.atlassian.bamboo.buildqueue.manager.CustomPreBuildQueuedAction;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.ModuleDescriptor;
import com.sysbliss.bamboo.sod.AbstractSauceBuildPlugin;
import com.sysbliss.bamboo.sod.Browser;
import com.sysbliss.bamboo.sod.BrowserFactory;
import com.sysbliss.bamboo.sod.SODSeleniumConfiguration;
import com.sysbliss.bamboo.sod.config.*;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

/**
 * Pre Build Action which generates and adds the Selenium environment variables that apply to the build
 * plan to the builder.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class EnvironmentConfigurator extends AbstractSauceBuildPlugin implements CustomPreBuildQueuedAction {

    private AdministrationConfigurationManager administrationConfigurationManager;
    private PlanManager planManager;
    private BrowserFactory sauceBrowserFactory;
    private static final String EQUALS = "=\"";
//    private static final String CUSTOM_DATA = "sauce:job-info={\"custom-data\": {\"bamboo-buildKey\": \"%1$s\", \"bamboo-buildNumber\": \"%2$s\", \"bamboo-buildResultKey\": \"%3$s\"}";
    private static final String CUSTOM_DATA = "sauce:job-build=%3$s";

    @NotNull
    public BuildContext call() throws JSONException {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());

        if (config.isEnabled()) {


            setSeleniumEnvironmentVars(config);
        }

        return buildContext;
    }

    private void setSeleniumEnvironmentVars(SODMappedBuildConfiguration config) throws JSONException {
        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        if (plan != null) {
            //try the task definitions
            String envBuffer = createSeleniumEnvironmentVariables(config);
            BuildDefinition definition = plan.getBuildDefinition();
            try {
                Class taskDefinitionClass = Class.forName("com.atlassian.bamboo.task.TaskDefinition");
                if (taskDefinitionClass != null) {
                    Method taskDefinitionsMethod = BuildDefinition.class.getMethod("getTaskDefinitions", null);


                    List/*<TaskDefinition>*/ taskDefinitions = (List/*<TaskDefinition>*/) taskDefinitionsMethod.invoke(definition, null);
                    for (Object taskDefinition : taskDefinitions) {
                        Method method = taskDefinitionClass.getMethod("getConfiguration", null);
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

            //legacy pre Bamboo 3 support            
            AbstractBuilder builder = (AbstractBuilder) definition.getBuilder();
            if (builder != null) {

                String originalEnv = builder.getEnvironmentVariables();
                config.getMap().put(SODKeys.TEMP_ENV_VARS, originalEnv);
                if (StringUtils.isNotBlank(originalEnv)) {
                    envBuffer = " " + envBuffer;
                }
                builder.setEnvironmentVariables(builder.getEnvironmentVariables() + envBuffer.toString());

            }
            planManager.savePlan(plan);
        }
    //buildDefinitionManager.savePlanAndDefinition(plan);
    }

    private String createSeleniumEnvironmentVariables(SODMappedBuildConfiguration config) throws JSONException {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String sodUsername = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String sodKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        String host = adminConfig.getSystemProperty(SODKeys.SELENIUM_HOST_KEY);
        String port = adminConfig.getSystemProperty(SODKeys.SELENIUM_PORT_KEY);
        String browserUrl = config.getSeleniumStartingUrl();
        String browserJson = getSodJson(sodUsername, sodKey, config);
        String sodDriverURI = getSodDriverUri(sodUsername, sodKey, config);

        config.setTempUsername(sodUsername);
        config.setTempApikey(sodKey);


        StringBuilder envBuffer = new StringBuilder();


        String sodHost = config.getSshDomains();
        String finalStartingUrl = browserUrl;

        if (config.isAutoDomain()) {
            sodHost = "bamboo-" + buildContext.getPlanKey() + ".bamboo";
            finalStartingUrl = "http://" + sodHost + ':' + config.getSshTunnelPorts() + '/';
        }

        envBuffer.append("-D").append(SODKeys.SELENIUM_HOST_ENV).append(EQUALS).append(host).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_PORT_ENV).append('=').append(port);
        envBuffer.append(' ').append(SODKeys.SELENIUM_BROWSER_ENV).append(EQUALS).append(browserJson).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_STARTING_URL_ENV).append(EQUALS).append(finalStartingUrl).append('"');
        envBuffer.append(' ').append(SODKeys.SAUCE_ONDEMAND_HOST).append(EQUALS).append(sodHost).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_DRIVER_ENV).append(EQUALS).append(sodDriverURI).append('"');
        envBuffer.append(SODKeys.SELENIUM_HOST_ENV_LEGACY).append(EQUALS).append(host).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_PORT_ENV_LEGACY).append('=').append(port);
        envBuffer.append(' ').append(SODKeys.SELENIUM_BROWSER_ENV_LEGACY).append(EQUALS).append(browserJson).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_STARTING_URL_ENV_LEGACY).append(EQUALS).append(finalStartingUrl).append('"');
        envBuffer.append(' ').append(SODKeys.SAUCE_ONDEMAND_HOST_LEGACY).append(EQUALS).append(sodHost).append('"');
        envBuffer.append(' ').append(SODKeys.SELENIUM_DRIVER_ENV_LEGACY).append(EQUALS).append(sodDriverURI).append('"');

        if (buildContext.getParentBuildContext() == null) {
            envBuffer.append(' ').append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getBuildResultKey()))
                    .append('"');
        } else {
            envBuffer.append(' ').append(SODKeys.SAUCE_CUSTOM_DATA).append(EQUALS).append(
                    String.format(CUSTOM_DATA, buildContext.getParentBuildContext().getPlanKey(), Integer.toString(buildContext.getBuildNumber()), buildContext.getParentBuildContext().getBuildResultKey())).
                    append('"');
        }
        return envBuffer.toString();
    }

    private String getSodJson(String username, String apiKey, SODMappedBuildConfiguration config) throws JSONException {

        SODSeleniumConfiguration sodConfig = new SODSeleniumConfiguration(username, apiKey, sauceBrowserFactory.forKey(config.getBrowserKey()));
        sodConfig.setJobName(buildContext.getPlanName() + "-" + Integer.toString(buildContext.getBuildNumber()));
        sodConfig.setFirefoxProfileUrl(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sodConfig.setIdleTimeout(config.getIdleTimeout());
        sodConfig.setMaxDuration(config.getMaxDuration());
        sodConfig.setRecordVideo(config.recordVideo());
        sodConfig.setUserExtensions(StringUtils.defaultString(config.getUserExtensionsJson()));

        return sodConfig.toJson();
    }

    /**
     * Generates a String that represents the Sauce OnDemand driver URL.
     *
     * @param username
     * @param apiKey
     * @param config
     * @param config
     * @return
     * @throws org.json.JSONException
     */
    private String getSodDriverUri(String username, String apiKey, SODMappedBuildConfiguration config) throws JSONException {
        StringBuilder sb = new StringBuilder("sauce-ondemand:?username=");
        sb.append(username);
        sb.append("&access-key=").append(apiKey);
        sb.append("&job-name=").append(StringUtils.trim(buildContext.getPlanName())).append('-').append(Integer.toString(buildContext.getBuildNumber()));

        Browser browser = sauceBrowserFactory.forKey(config.getBrowserKey());
        sb.append("&os=").append(browser.getOs());
        sb.append("&browser=").append(browser.getBrowserName());
        sb.append("&browser-version=").append(browser.getVersion());

        sb.append("&firefox-profile-url=").append(StringUtils.defaultString(config.getFirefoxProfileUrl()));
        sb.append("&max-duration=").append(config.getMaxDuration());
        sb.append("&idle-timeout=").append(config.getIdleTimeout());
        sb.append("&user-extensions-url=").append(StringUtils.defaultString(config.getUserExtensionsJson()));

        return sb.toString();
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public PlanManager getPlanManager() {
        return planManager;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory) {
        this.sauceBrowserFactory = sauceBrowserFactory;
    }

    @Override
    public void init(@NotNull BuildContext buildContext) {
        super.init(buildContext);    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    public void init(@NotNull ModuleDescriptor moduleDescriptor) {
        super.init(moduleDescriptor);    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    public void prepareConfigObject(@NotNull BuildConfiguration buildConfiguration) {
        super.prepareConfigObject(buildConfiguration);    //To change body of overridden methods use File | Settings | File Templates.

    }
}
