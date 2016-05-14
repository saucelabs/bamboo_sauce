package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.ResultKey;
import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.CustomVariableContextImpl;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.singletons.SauceConnectFourManagerSingleton;
import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.saucelabs.bamboo.sod.config.SODKeys.TEMP_TUNNEL_ID;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Ross Rowe
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ContainerManager.class)
public class BuildConfiguratorTest extends AbstractTestHelper {

    private BuildConfigurator buildConfigurator;
    private BuildDefinition buildDefinition;
    private SauceTunnel sauceTunnel;

    private final Map<String, Object> tunnelMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        this.buildConfigurator = new BuildConfigurator();
        SauceConnectFourManager tunnelManager = new SauceConnectFourManager() {
            @Override
            public void closeTunnelsForPlan(String username, String options, PrintStream printStream) {
            }

            @Override
            public Process openConnection(String username, String apiKey, int port, File sauceConnectJar, String options, PrintStream printStream, Boolean verboseLogging, String sauceConnectPath) throws SauceConnectException {
                BuildConfiguratorTest.this.tunnelMap.put(username, mock(Process.class));
                return null;
            }
        };
        SauceConnectFourManagerSingleton.setSauceConnectFourTunnelManager(tunnelManager);

        BuildContext buildContext = mock(BuildContext.class);
        CurrentBuildResult buildResult = mock(CurrentBuildResult.class);
        buildDefinition = mock(BuildDefinition.class);
        when(buildContext.getBuildResult()).thenReturn(buildResult);
        ResultKey planResultKey = mock(ResultKey.class);
        when(buildContext.getResultKey()).thenReturn(planResultKey);
        when(buildContext.getBuildDefinition()).thenReturn(buildDefinition);
        when(buildContext.getPlanKey()).thenReturn("PLAN");
        when(buildContext.getPlanName()).thenReturn("PLAN NAME");
        Map<String, String> customBuildData = new HashMap<>();

        when(buildResult.getCustomBuildData()).thenReturn(customBuildData);
        Map<String, String> customConfiguration = new HashMap<>();
        customConfiguration.put(SODKeys.TEMP_USERNAME, "tempUser");
        customConfiguration.put(SODKeys.TEMP_API_KEY, "apiKey");
        customConfiguration.put(SODKeys.SSH_LOCAL_HOST_KEY, "sshhost");
        customConfiguration.put(SODKeys.SSH_LOCAL_PORTS_KEY, "1234");

        when(buildDefinition.getCustomConfiguration()).thenReturn(customConfiguration);

        SauceTunnelFactory sauceTunnelFactory = mock(SauceTunnelFactory.class);
        when(sauceTunnelFactory.create(any(List.class))).thenReturn(sauceTunnel);
        buildConfigurator.init(buildContext);

        mockStatic(ContainerManager.class);
        BuildLoggerManager buildLoggerManager = mock(BuildLoggerManager.class);
        BuildLogger buildLogger = mock(BuildLogger.class);
        LogInterceptorStack interceptorStack = mock(LogInterceptorStack.class);
        when(buildLogger.getInterceptorStack()).thenReturn(interceptorStack);
        when(buildLoggerManager.getLogger(planResultKey)).thenReturn(buildLogger);
        when(ContainerManager.getComponent("buildLoggerManager")).thenReturn(buildLoggerManager);
    }


    @Test
    public void defaultValues() throws Exception {
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfigurator.addDefaultValues(buildConfiguration);
        assertEquals(buildConfiguration.getProperty(SODKeys.MAX_DURATION_KEY), "300");

    }

    @Test
    public void addingDefaultValuesWithExisting() throws Exception {
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setProperty(SODKeys.MAX_DURATION_KEY, "200");
        buildConfigurator.addDefaultValues(buildConfiguration);
        assertEquals(buildConfiguration.getProperty(SODKeys.MAX_DURATION_KEY), "200");

    }

    @Test
    public void sshEnabled() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildConfigurator.call();
        assertFalse("No sauce tunnels exist", getTunnelMap().isEmpty());
    }

    @Test
    public void sshDisabled() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildConfigurator.call();
        assertTrue("Sauce tunnels not empty", getTunnelMap().isEmpty());
    }

    @Test
    public void sshEnabledWithUniqueKey() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        CustomVariableContext customVariableContext = mock(CustomVariableContext.class);
        buildConfigurator.setCustomVariableContext(customVariableContext);
        buildConfigurator.call();
        verify(customVariableContext, times(1)).addCustomData(anyString(), anyString());
        assertFalse("No sauce tunnels exist for unique key", getTunnelMap().isEmpty());
    }

    public Map<String, Object> getTunnelMap() {
        return tunnelMap;
    }

    @Test
    public void browserFromSaucelabs() throws Exception {
        BrowserFactory factory = new BrowserFactory();
        List<Browser> browsers = factory.getWebDriverBrowsers();
        assertFalse("browsers is empty", browsers.isEmpty());
    }
}
