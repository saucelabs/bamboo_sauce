package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.EnvironmentVariableAccessorImpl;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.variable.*;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import org.apache.xpath.operations.Bool;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ross Rowe
 */
@Ignore
public class EnvironmentConfiguratorTest {

    private EnvironmentConfigurator environmentConfigurator;
    private Map<String, String> customConfiguration;
    private ArrayList<TaskDefinition> taskDefinitions;
    private Map<String, String> updatedConfiguration;

    private Browser browser = new Browser("Windows 2008firefox7", "Windows 2008", "Firefox", "Firefox", "7", "7", "Firefox");
    private BuildContext buildContext;
    private EnvironmentVariableAccessor environmentVariableAccessor;

    @Before
    public void setUp() throws Exception {
        this.environmentConfigurator = new EnvironmentConfigurator();
        buildContext = mock(BuildContext.class);
        CurrentBuildResult buildResult = mock(CurrentBuildResult.class);
        BuildDefinition buildDefinition = mock(BuildDefinition.class);
        PlanManager planManager = mock(PlanManager.class);
        Plan plan = mock(Plan.class);
        VariableContext variableContext = new VariableContextImpl(Collections.<String, VariableDefinitionContext>emptyMap());
        CustomVariableContext customVariableContext = mock(CustomVariableContext.class);
        environmentConfigurator.setCustomVariableContext(customVariableContext);
        environmentVariableAccessor = new EnvironmentVariableAccessorImpl(
            null,
            null
        );
        AdministrationConfigurationManager adminConfigManager = mock(AdministrationConfigurationManager.class);
        AdministrationConfiguration adminConfig = mock(AdministrationConfiguration.class);
        TaskDefinition definition = mock(TaskDefinition.class);

        when(buildContext.getBuildResult()).thenReturn(buildResult);
        when(buildContext.getBuildDefinition()).thenReturn(buildDefinition);
        when(buildContext.getPlanName()).thenReturn("PLAN");
        when(buildContext.getPlanKey()).thenReturn("PLAN");
        when(buildContext.getVariableContext()).thenReturn(variableContext);
        when(planManager.getPlanByKey(eq("PLAN"))).thenReturn(plan);
        when(plan.getBuildDefinition()).thenReturn(buildDefinition);


        when(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY)).thenReturn("defaultUser");
        when(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY)).thenReturn("defaultAccessKey");
        when(adminConfig.getSystemProperty(SODKeys.SOD_DATACENTER_KEY)).thenReturn("defaultDataCenter");
        when(adminConfigManager.getAdministrationConfiguration()).thenReturn(adminConfig);
        environmentConfigurator.setAdministrationConfigurationManager(adminConfigManager);
        this.customConfiguration = new HashMap<String, String>();
        customConfiguration.put(SODKeys.ENABLED_KEY, Boolean.TRUE.toString());
        customConfiguration.put(SODKeys.SELENIUM_URL_KEY, "http://localhost");
        customConfiguration.put(SODKeys.TEMP_USERNAME, "tempUser");
        customConfiguration.put(SODKeys.TEMP_API_KEY, "apiKey");
        customConfiguration.put(SODKeys.TEMP_DATA_CENTER, "dataCenter");
        customConfiguration.put(SODKeys.ENABLED_KEY, "true");
        customConfiguration.put(SODKeys.BROWSER_KEY, "Windows 2008firefox7" );
        this.taskDefinitions = new ArrayList<TaskDefinition>();
        taskDefinitions.add(definition);

        when(buildDefinition.getTaskDefinitions()).thenReturn(taskDefinitions);
        this.updatedConfiguration = new HashMap<String, String>();
        when(definition.getConfiguration()).thenReturn(updatedConfiguration);

        when(buildDefinition.getCustomConfiguration()).thenReturn(customConfiguration);
        environmentConfigurator.init(buildContext);

        environmentConfigurator.setSauceBrowserFactory(new BrowserFactory() {

            @Override
            public Browser webDriverBrowserForKey(String key) {
                return browser;
            }

            @Override
            public List<Browser> getWebDriverBrowsers() throws JSONException {
                return Arrays.asList(browser);
            }
        });
        environmentConfigurator.setEnvironmentVariableAccessor(environmentVariableAccessor);
        customConfiguration.put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, Boolean.TRUE.toString());
        Map<String, VariableDefinitionContext> tempMap = new HashMap<>();
        tempMap.put(SODKeys.TUNNEL_IDENTIFIER, new VariableDefinitionContextImpl(SODKeys.TUNNEL_IDENTIFIER, "TUNNEL_IDENTIFIER", VariableType.CUSTOM));
        when(customVariableContext.getVariableContexts()).thenReturn(tempMap);
    }

    @Test
    public void seleniumOne() throws Exception {
        customConfiguration.put(SODKeys.SELENIUM_VERSION_KEY, "1.x");
        environmentConfigurator.call();
        String variables = updatedConfiguration.get("environmentVariables");
        assertNotNull("Variables not set", variables);
		Map<String, VariableDefinitionContext> map = buildContext.getVariableContext().getEffectiveVariables();

        String startingUrl = map.get(SODKeys.SELENIUM_STARTING_URL_ENV).getValue();
        assertNotNull("Starting URL not set", startingUrl);
        assertEquals("Starting URL not localhost", startingUrl, "http://localhost");
    }

    @Test
    public void seleniumTwo() throws Exception {
        // confirm those used in https://wiki.saucelabs.com/display/DOCS/_environment_variables
        customConfiguration.put(SODKeys.SELENIUM_VERSION_KEY, "2.x");
        environmentConfigurator.call();
        String variables = updatedConfiguration.get("environmentVariables");
        assertNotNull("Variables not set", variables);
        Map<String, String> envMap = environmentVariableAccessor.splitEnvironmentAssignments(variables);
        Map<String, VariableDefinitionContext> map = buildContext.getVariableContext().getEffectiveVariables();

		String version = map.get(SODKeys.SELENIUM_VERSION_ENV).getValue();
		assertNotNull("Version not set", version);
        assertEquals("Version not 7", version, "7");

        String host = map.get(SODKeys.SELENIUM_HOST_ENV).getValue();
        assertNotNull(host);
        assertEquals(host, "ondemand.saucelabs.com");

        String port = map.get(SODKeys.SELENIUM_PORT_ENV).getValue();
        assertNotNull(port);
        assertEquals(port, "4444");

        String platform = map.get(SODKeys.SELENIUM_PLATFORM_ENV).getValue();
        assertNotNull("Platform not set", platform);
        assertEquals("Platfom not WINDOWS", platform, "Windows 2008");

        String browser = map.get(SODKeys.SELENIUM_BROWSER_ENV).getValue();
        assertNotNull("Browser not set", browser);
        assertEquals("Browser not firefox", browser, "Firefox");

        String driver = map.get(SODKeys.SELENIUM_DRIVER_ENV).getValue();
        assertNotNull(driver);
        assertEquals(driver, "sauce-ondemand:?username=defaultUser&access-key=defaultAccessKey&job-name=PLAN-0&os=Windows 2008&browser=Firefox&browser-version=7&firefox-profile-url=&max-duration=0&idle-timeout=0&user-extensions-url=");

        String url = map.get(SODKeys.SELENIUM_URL_ENV).getValue();
        assertNotNull(url);
        assertEquals(url, "http://localhost");

        String username = map.get(SODKeys.SAUCE_USERNAME_ENV).getValue();
        assertNotNull(username);
        assertEquals(username, "defaultUser");

        String key = map.get(SODKeys.SAUCE_ACCESS_KEY_ENV).getValue();
        assertNotNull(key);
        assertEquals(key, "defaultAccessKey");

        String dataCenter = map.get(SODKeys.SAUCE_DATA_CENTER_ENV).getValue();
        assertNotNull(dataCenter);
        assertEquals(dataCenter, "defaultDataCenter");

        String browsers = map.get(SODKeys.SAUCE_BROWSERS).getValue();
        assertNotNull(browsers);
        assertEquals(browsers, "[{\"os\":\"Windows 2008\",\"browser\":\"Firefox\",\"browser-version\":\"7\",\"platform\":\"Windows 2008\",\"url\":\"sauce-ondemand:?os=Windows 2008&browser=Firefox&browser-version=7\"}]");

        String startingUrl = map.get(SODKeys.SELENIUM_STARTING_URL_ENV).getValue();
        assertNotNull("Starting URL not set", startingUrl);
        assertEquals("Starting URL not localhost", startingUrl, "http://localhost");

        String tunnelIdentifier = map.get(SODKeys.TUNNEL_IDENTIFIER).getValue();
        assertNotNull("TUNNEL_IDENTIFIER not set", tunnelIdentifier);
        assertEquals("TUNNEL_IDENTIFIER not TunnelId", tunnelIdentifier, "TUNNEL_IDENTIFIER");

        Field[] fields = SODKeys.class.getDeclaredFields();

        for ( Field field : fields ) {
            field.setAccessible(true);
            if (field.getName().endsWith("_ENV")) {
                String keyName = (String) field.get(SODKeys.class);
                if (envMap.containsKey(keyName) || map.containsKey(keyName))
                {
                    assertTrue("if one contains " + keyName + " then so should env", envMap.containsKey(keyName));
                    assertTrue("if one contains " + keyName + " then so should bamboo", map.containsKey(keyName));
                }
            }
        }

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
