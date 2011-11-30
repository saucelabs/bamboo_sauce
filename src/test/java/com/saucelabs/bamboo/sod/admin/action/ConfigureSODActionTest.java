package com.saucelabs.bamboo.sod.admin.action;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.config.SODKeys;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ross Rowe
 */
public class ConfigureSODActionTest extends AbstractTestHelper {

    private ConfigureSODAction configureSODAction;
    
    @Before
    public void setUp() throws Exception {
        configureSODAction = new ConfigureSODAction();
        AdministrationConfigurationManager adminConfigManager = mock(AdministrationConfigurationManager.class);
        AdministrationConfiguration adminConfig = mock(AdministrationConfiguration.class);
        when(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY)).thenReturn("defaultUser");
        when(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY)).thenReturn("defaultAccessKey");
        when(adminConfigManager.getAdministrationConfiguration()).thenReturn(adminConfig);
        configureSODAction.setAdministrationConfigurationManager(adminConfigManager);
    }
    
    @Test
    public void runDefault() throws Exception {
        String result = configureSODAction.doDefault();
        assertEquals("Result not INPUT", result, "input");
        assertEquals(configureSODAction.getUsername(), "defaultUser");
        assertEquals(configureSODAction.getAccessKey(), "defaultAccessKey");
        assertEquals(configureSODAction.getSeleniumHost(), "saucelabs.com");
        assertEquals(configureSODAction.getSeleniumPort(), "4444");
    }
    
    @Test
    public void runSave() throws Exception {
        
        String result = configureSODAction.doSave();
        assertEquals("Result not SUCCESS", result, "success");
    }
    
    @Test
    public void runValidate() throws Exception {
        configureSODAction.validate();
        //verify that error messages have been applied
    }
}
