package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.ResultKey;
import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.PartialBuildDefinitionImpl;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.configuration.AdministrationConfigurationImpl;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.v2.build.CurrentBuildResultImpl;
import com.atlassian.bamboo.variable.CustomVariableContextImpl;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.singletons.SauceConnectFourManagerSingleton;
import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import com.saucelabs.ci.sauceconnect.AbstractSauceTunnelManager;
import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private SauceConnectFourManager tunnelManager;

    private final Map<String, Object> tunnelMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        this.buildConfigurator = new BuildConfigurator();
        tunnelManager = mock(SauceConnectFourManager.class);
        when(tunnelManager.openConnection(
            anyString(),
            anyString(),
            anyInt(),
            any(File.class),
            anyString(),
            any(PrintStream.class),
            anyBoolean(),
            anyString()
        )).thenAnswer(new Answer<Process>() {
            @Override
            public Process answer(InvocationOnMock invocationOnMock) throws Throwable {
                String username = (String) invocationOnMock.getArguments()[0];
                BuildConfiguratorTest.this.tunnelMap.put(username, mock(Process.class));
                return null;
            }
        });
        SauceConnectFourManagerSingleton.setSauceConnectFourTunnelManager(tunnelManager);

        final Map<String, String> customBuildData = new HashMap<>();
        Map<String, String> customConfiguration = new HashMap<>();
        customConfiguration.put(SODKeys.TEMP_USERNAME, "tempUser");
        customConfiguration.put(SODKeys.TEMP_API_KEY, "apiKey");
        customConfiguration.put(SODKeys.SSH_LOCAL_HOST_KEY, "sshhost");
        customConfiguration.put(SODKeys.SSH_LOCAL_PORTS_KEY, "1234");


        final CurrentBuildResult buildResult = mock(CurrentBuildResult.class);
        when(buildResult.getCustomBuildData()).thenReturn(customBuildData);

        buildDefinition = mock(BuildDefinition.class);
        when(buildDefinition.getCustomConfiguration()).thenReturn(customConfiguration);

        final ResultKey planResultKey = mock(ResultKey.class);

        BuildContext buildContext = mock(BuildContext.class);
        when(buildContext.getResultKey()).thenReturn(planResultKey);
        when(buildContext.getBuildDefinition()).thenReturn(buildDefinition);
        when(buildContext.getBuildResult()).thenReturn(buildResult);
        when(buildContext.getPlanKey()).thenReturn("PLAN");
        when(buildContext.getPlanKey()).thenReturn("PLAN NAME");

        final AdministrationConfiguration administrationConfiguration = new AdministrationConfigurationImpl(null);
        //when(administrationConfiguration.getSystemProperty(anyString())).thenAnswer("");

        AdministrationConfigurationAccessor administrationConfigurationAccessor = mock(AdministrationConfigurationAccessor.class);
        when(administrationConfigurationAccessor.getAdministrationConfiguration()).thenReturn(administrationConfiguration);
        buildConfigurator.setAdministrationConfigurationAccessor(administrationConfigurationAccessor);

        SauceTunnelFactory sauceTunnelFactory = mock(SauceTunnelFactory.class);
        when(sauceTunnelFactory.create(any(List.class))).thenReturn(sauceTunnel);
        buildConfigurator.init(buildContext);

        buildConfigurator.setCustomVariableContext(new CustomVariableContextImpl(null) {
            @NotNull
            @Override
            public Map<String, VariableDefinitionContext> getVariableContexts(@Nullable CommonContext commonContext) {
                return new HashMap<String, VariableDefinitionContext>();
            }

            @NotNull
            @Override
            public Map<String, VariableDefinitionContext> getVariableContexts() {
                return new HashMap<String, VariableDefinitionContext>();
            }
        });

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
        assertEquals(1, getTunnelMap().size());
    }

    @Test
    public void sshDisabled() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildConfigurator.call();
        assertEquals(0, getTunnelMap().size());
    }

    @Test
    public void sshEnabledWithUniqueKey() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.OVERRIDE_AUTHENTICATION_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_USERNAME, "halkeye");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_API_KEY, "SO FAKE");
        buildConfigurator.call();
        verify(tunnelManager, times(1)).openConnection(
            eq("halkeye"),
            eq("SO FAKE"),
            eq(4445),
            eq((File) null),
            eq("-x https://saucelabs.com/rest/v1 --tunnel-identifier null "),
            any(PrintStream.class),
            eq(false),
            eq((String) null)
        );
        assertFalse("No sauce tunnels exist for unique key", getTunnelMap().isEmpty());
    }

    @Test
    public void multipleAttemptsAtSauceConnect() throws Exception {
        buildConfigurator.getAdministrationConfigurationAccessor().getAdministrationConfiguration().setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_RETRY_WAIT_TIME, "1");
        buildConfigurator.getAdministrationConfigurationAccessor().getAdministrationConfiguration().setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_MAX_RETRIES, "1");

        when(tunnelManager.openConnection(
            anyString(),
            anyString(),
            anyInt(),
            any(File.class),
            anyString(),
            any(PrintStream.class),
            anyBoolean(),
            eq((String) null)
        ))
            .thenThrow(new AbstractSauceTunnelManager.SauceConnectDidNotStartException("Blah blah error"))
            .thenReturn(null);

        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.OVERRIDE_AUTHENTICATION_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_USERNAME, "halkeye");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_API_KEY, "SO FAKE");
        buildConfigurator.call();
        verify(tunnelManager, times(2)).openConnection(
            eq("halkeye"),
            eq("SO FAKE"),
            eq(4445),
            eq((File) null),
            eq("-x https://saucelabs.com/rest/v1 --tunnel-identifier null "),
            any(PrintStream.class),
            eq(false),
            eq((String) null)
        );
    }


    @Test
    public void multipleFailedAttemptsAtSauceConnect() throws Exception {
        buildConfigurator.getAdministrationConfigurationAccessor().getAdministrationConfiguration().setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_RETRY_WAIT_TIME, "1");
        buildConfigurator.getAdministrationConfigurationAccessor().getAdministrationConfiguration().setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_MAX_RETRIES, "1");

        when(tunnelManager.openConnection(
            anyString(),
            anyString(),
            anyInt(),
            any(File.class),
            anyString(),
            any(PrintStream.class),
            anyBoolean(),
            eq((String) null)
        ))
            .thenThrow(new AbstractSauceTunnelManager.SauceConnectDidNotStartException("Blah blah error"));

        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.OVERRIDE_AUTHENTICATION_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_USERNAME, "halkeye");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_API_KEY, "SO FAKE");
        buildConfigurator.call();
        verify(tunnelManager, times(2)).openConnection(
            eq("halkeye"),
            eq("SO FAKE"),
            eq(4445),
            eq((File) null),
            eq("-x https://saucelabs.com/rest/v1 --tunnel-identifier null "),
            any(PrintStream.class),
            eq(false),
            eq((String) null)
        );
    }

    @Test
    public void multipleFailedAttemptsAtSauceConnectDefaultRetries() throws Exception {
        when(tunnelManager.openConnection(
            anyString(),
            anyString(),
            anyInt(),
            any(File.class),
            anyString(),
            any(PrintStream.class),
            anyBoolean(),
            eq((String) null)
        ))
            .thenThrow(new AbstractSauceTunnelManager.SauceConnectDidNotStartException("Blah blah error"));

        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.SSH_USE_GENERATED_TUNNEL_ID, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.OVERRIDE_AUTHENTICATION_KEY, "true");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_USERNAME, "halkeye");
        buildDefinition.getCustomConfiguration().put(SODKeys.TEMP_API_KEY, "SO FAKE");
        buildConfigurator.call();
        verify(tunnelManager, times(1)).openConnection(
            eq("halkeye"),
            eq("SO FAKE"),
            eq(4445),
            eq((File) null),
            eq("-x https://saucelabs.com/rest/v1 --tunnel-identifier null "),
            any(PrintStream.class),
            eq(false),
            eq((String) null)
        );
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
