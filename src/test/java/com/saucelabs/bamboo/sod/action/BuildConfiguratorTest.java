package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.saucelabs.rest.SauceTunnel;
import com.saucelabs.rest.SauceTunnelFactory;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.bamboo.sod.util.SauceTunnelManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ross Rowe
 */
public class BuildConfiguratorTest {

    private BuildConfigurator buildConfigurator;
    private SauceTunnelManager tunnelManager;
    private BuildDefinition buildDefinition;
    private SauceTunnel sauceTunnel;

    @Before
    public void setUp() throws Exception {
        this.buildConfigurator = new BuildConfigurator();
        this.tunnelManager = new SauceTunnelManager(){

            public void closeTunnelsForPlan(String planKey) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void addTunnelToMap(String planKey, Object tunnel) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object openConnection(String username, String apiKey, String localHost, int intLocalPort, int intRemotePort, List<String> domainList) throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Map getTunnelMap() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        buildConfigurator.setSauceTunnelManager(tunnelManager);

        BuildContext buildContext = mock(BuildContext.class);
        CurrentBuildResult buildResult = mock(CurrentBuildResult.class);
        buildDefinition = mock(BuildDefinition.class);
        when(buildContext.getBuildResult()).thenReturn(buildResult);
        when(buildContext.getBuildDefinition()).thenReturn(buildDefinition);
        when(buildContext.getPlanKey()).thenReturn("PLAN");
        Map<String, String> customBuildData = new HashMap<String, String>();
        
        when(buildResult.getCustomBuildData()).thenReturn(customBuildData);
        Map<String, String> customConfiguration = new HashMap<String, String>();
        customConfiguration.put(SODKeys.TEMP_USERNAME, "tempUser");
        customConfiguration.put(SODKeys.TEMP_API_KEY, "apiKey");
        customConfiguration.put(SODKeys.SSH_LOCAL_HOST_KEY, "sshhost");
        customConfiguration.put(SODKeys.SSH_LOCAL_PORTS_KEY, "1234");
        customConfiguration.put(SODKeys.SSH_REMOTE_PORTS_KEY, "5678");
        customConfiguration.put(SODKeys.SSH_DOMAINS_KEY, "sshDomains");
        customConfiguration.put(SODKeys.SSH_AUTO_DOMAIN_KEY, "true");

        when(buildDefinition.getCustomConfiguration()).thenReturn(customConfiguration);
        
        SauceTunnelFactory sauceTunnelFactory = mock(SauceTunnelFactory.class);
//        sauceTunnel = mock(SauceTunnel.class);
//        when(sauceTunnel.isRunning()).thenReturn(true);
        //when(sauceTunnel.connect(Integer.parseInt("5678"), "sshhost", Integer.parseInt("1234")));
        when(sauceTunnelFactory.create(any(List.class))).thenReturn(sauceTunnel);
        SauceFactory sauceAPIFactory = mock(SauceFactory.class);
//        when(sauceAPIFactory.createSauceTunnelFactory("tempUser", "apiKey")).thenReturn(sauceTunnelFactory);
        
        buildConfigurator.setSauceAPIFactory(sauceAPIFactory);
        buildConfigurator.init(buildContext);

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
        assertFalse("No sauce tunnels exist", tunnelManager.getTunnelMap().isEmpty());
    }

    @Test
    public void sshDisabled() throws Exception {
        buildDefinition.getCustomConfiguration().put(SODKeys.ENABLED_KEY, "true");
        buildConfigurator.call();
        assertTrue("Sauce tunnels not empty", tunnelManager.getTunnelMap().isEmpty());
    }

}
