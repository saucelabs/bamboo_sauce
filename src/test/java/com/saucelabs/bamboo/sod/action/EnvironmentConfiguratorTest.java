package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.saucelabs.bamboo.sod.config.SODKeys;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ross Rowe
 */
public class EnvironmentConfiguratorTest {

    private EnvironmentConfigurator environmentConfigurator;
    private Map<String, String> customConfiguration;
    private ArrayList<TaskDefinition> taskDefinitions;
    private Map<String, String> updatedConfiguration;

    @Before
    public void setUp() throws Exception {
        this.environmentConfigurator = new EnvironmentConfigurator();
        BuildContext buildContext = mock(BuildContext.class);
        CurrentBuildResult buildResult = mock(CurrentBuildResult.class);
        BuildDefinition buildDefinition = mock(BuildDefinition.class);
        PlanManager planManager = mock(PlanManager.class);
        Plan plan = mock(Plan.class);
        AdministrationConfigurationManager adminConfigManager = mock(AdministrationConfigurationManager.class);
        AdministrationConfiguration adminConfig = mock(AdministrationConfiguration.class);
        TaskDefinition definition = mock(TaskDefinition.class);

        when(buildContext.getBuildResult()).thenReturn(buildResult);
        when(buildContext.getBuildDefinition()).thenReturn(buildDefinition);
        when(buildContext.getPlanName()).thenReturn("PLAN");
        when(buildContext.getPlanKey()).thenReturn("PLAN");
        when(planManager.getPlanByKey(eq("PLAN"))).thenReturn(plan);
        when(plan.getBuildDefinition()).thenReturn(buildDefinition);


        when(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY)).thenReturn("defaultUser");
        when(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY)).thenReturn("defaultAccessKey");
        when(adminConfigManager.getAdministrationConfiguration()).thenReturn(adminConfig);
        environmentConfigurator.setAdministrationConfigurationManager(adminConfigManager);
        this.customConfiguration = new HashMap<String, String>();
        customConfiguration.put(SODKeys.SELENIUM_URL_KEY, "http://localhost");
        customConfiguration.put(SODKeys.TEMP_USERNAME, "tempUser");
        customConfiguration.put(SODKeys.TEMP_API_KEY, "apiKey");
        customConfiguration.put(SODKeys.ENABLED_KEY, "true");
        customConfiguration.put(SODKeys.BROWSER_KEY, "[Windows 2008firefox7, Windows 2008firefox7]" );

        this.taskDefinitions = new ArrayList<TaskDefinition>();
        taskDefinitions.add(definition);

        when(buildDefinition.getTaskDefinitions()).thenReturn(taskDefinitions);
        this.updatedConfiguration = new HashMap<String, String>();
        when(definition.getConfiguration()).thenReturn(updatedConfiguration);

        when(buildDefinition.getCustomConfiguration()).thenReturn(customConfiguration);
        environmentConfigurator.init(buildContext);
    }

    @Test
    public void seleniumOne() throws Exception {
        customConfiguration.put(SODKeys.SELENIUM_VERSION_KEY, "1.x");
        environmentConfigurator.call();
        String variables = updatedConfiguration.get("environmentVariables");
        assertNotNull("Variables not set", variables);
		Map<String, String> map = convertVariablesToMap(variables);

        String startingUrl = map.get(SODKeys.SELENIUM_STARTING_URL_ENV);
        assertNotNull("Starting URL not set", startingUrl);
        assertEquals("Starting URL not localhost", startingUrl, "http://localhost");
    }

    @Test
    public void seleniumTwo() throws Exception {
        customConfiguration.put(SODKeys.SELENIUM_VERSION_KEY, "2.x");
        environmentConfigurator.call();
        String variables = updatedConfiguration.get("environmentVariables");
        assertNotNull("Variables not set", variables);	
		Map<String, String> map = convertVariablesToMap(variables);

        String platform = map.get(SODKeys.SELENIUM_PLATFORM_ENV);
        assertNotNull("Platform not set", platform);
        assertEquals("Platfom not WINDOWS", platform, Platform.VISTA.toString());
		
		String browser = map.get(SODKeys.SELENIUM_BROWSER_ENV);
		assertNotNull("Browser not set", browser);
        assertEquals("Browser not firefox", browser, "firefox");

		String version = map.get(SODKeys.SELENIUM_VERSION_ENV);
		assertNotNull("Version not set", version);
        assertEquals("Version not 7", version, "7");

        String startingUrl = map.get(SODKeys.SELENIUM_STARTING_URL_ENV);
        assertNotNull("Starting URL not set", startingUrl);
        assertEquals("Starting URL not localhost", startingUrl, "http://localhost");

    }

	private Map<String,String> convertVariablesToMap(String variables) {
		Map<String, String> map = new HashMap<String, String>();
		StringTokenizer tokenizer = new StringTokenizer(variables, "\"");
		while (tokenizer.hasMoreTokens()) {
				String key = tokenizer.nextToken();
				String value = tokenizer.nextToken();
				map.put(key.replaceAll("=", "").trim(), value);
		}
		return map;
	}
}
