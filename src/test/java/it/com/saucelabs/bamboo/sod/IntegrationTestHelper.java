package it.com.saucelabs.bamboo.sod;


import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.action.BuildConfigurator;

import com.saucelabs.bamboo.sod.util.SauceFactory;
import com.saucelabs.bamboo.sod.util.SauceTunnelManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;

import org.junit.BeforeClass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 *
 * @author Ross Rowe
 */
public class IntegrationTestHelper {        
    
    private static SauceTunnelManager sauceTunnelManager;
    
    private Properties properties;
    private static String DUMMY_PLAN_KEY;
    private static final String ENABLE_SSH = "";

    @BeforeClass
    public static void setupSSH() throws Exception {
        InputStream stream = IntegrationTestHelper.class.getClassLoader().getResourceAsStream("test.properties");
        Properties properties = new Properties();
        properties.load(stream);
        System.setProperties(properties);
        if (properties.getProperty("sauce.ssh").equals("true")) {
            sauceTunnelManager = new SauceTunnelManager() {

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
            BuildConfigurator buildConfigurator = new BuildConfigurator();
            String username = properties.getProperty("sauce.username");
            String apiKey = properties.getProperty("sauce.accessKey");
            String sshHost = properties.getProperty("sauce.sshHost");
            String sshPort = properties.getProperty("sauce.sshPort");
            String tunnelPort = properties.getProperty("sauce.tunnelPort");
            String domain = properties.getProperty("sauce.domain");
            boolean autoDomain = true;
            buildConfigurator.setSauceTunnelManager(sauceTunnelManager);
            buildConfigurator.setSauceAPIFactory(new SauceFactory());
            BuildContext buildContext = mock(BuildContext.class);
            when(buildContext.getPlanKey()).thenReturn(DUMMY_PLAN_KEY);
            buildConfigurator.init(buildContext);
            buildConfigurator.startTunnel(username, apiKey, sshHost, sshPort, tunnelPort, domain, autoDomain);
            
        } 
    }

    protected Selenium selenium;

    @Before
    public void setUp() throws Exception {
        selenium = SeleniumFactory.create();
       
        //selenium = new DefaultSelenium("ondemand.saucelabs.com",
        //        80, generateStartCommand(properties), "http://localhost:8085/");
        //selenium.start();
    }
    
    @AfterClass
    public static void closeSSH() throws Exception {
        if (ENABLE_SSH.equals("true")) {
            sauceTunnelManager.closeTunnelsForPlan(DUMMY_PLAN_KEY);
        }
    }
    
    
    
    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
    
    private String generateStartCommand(Properties properties) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"username\": \"").append(properties.get("sauce.username"));
        builder.append(",\"access-key\": \"").append(properties.get("sauce.accessKey"));
        builder.append(",\"os\": \"Windows 2003\"");
        builder.append("\"browser\": \"firefox\"," + "\"browser-version\": \"3.6.\"," + "\"name\": \"This is an example test\"}");
        return builder.toString();
    }
}
