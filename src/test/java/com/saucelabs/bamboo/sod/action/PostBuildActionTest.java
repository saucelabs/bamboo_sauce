package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.ResultKey;
import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by gavinmogan on 2016-02-09.
 */
@Ignore
public class PostBuildActionTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testProcessLine() throws Exception {
        final String[] data = new String[2];
        PostBuildAction pba = new PostBuildAction() {
            @Override
            protected void storeBuildMetadata(SODMappedBuildConfiguration config, String sessionId, String jobName) {
                //super.storeBambooBuildNumberInSauce(config, sessionId, jobName);
                data[0] = sessionId;
                data[1] = jobName;
                return;
            }
        };
        SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(new HashMap<String,String>());
        assertTrue(
            pba.processLine(
                config,
                "SauceOnDemandSessionID=71bd8ffae68a4349b8965681a1d9659f job-name=tests.BTF.test0_0server_admin_setup_wizard.TestServerAdminSetupWizard.test_setup_wizard_success"
            )
        );
        assertEquals("71bd8ffae68a4349b8965681a1d9659f", data[0]);
        assertEquals("tests.BTF.test0_0server_admin_setup_wizard.TestServerAdminSetupWizard.test_setup_wizard_success", data[1]);
    }

    @Test
    public void testStoreBambooBuildNumberInSauce() throws Exception {
        final SauceREST rest = mock(SauceREST.class);
        when(rest.getJobInfo(anyString())).thenReturn(
            IOUtils.toString(getClass().getResourceAsStream("/job_info.json"), "UTF-8")
        );

        PostBuildAction pba = new PostBuildAction() {
            @Override
            protected SauceREST getSauceREST(SODMappedBuildConfiguration config) {
                return rest;
            }

            @Override
            protected String getBuildNumber() {
                return "1234";
            }

        };

        SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(new HashMap<String,String>());
        assertTrue(
            pba.processLine(
                config,
                "SauceOnDemandSessionID=449f8e8f5940483ea6938ce6cdbea117 job-name=tests.BTF.test0_0server_admin_setup_wizard.TestServerAdminSetupWizard.test_setup_wizard_success"
            )
        );

        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", "tests.BTF.test0_0server_admin_setup_wizard.TestServerAdminSetupWizard.test_setup_wizard_success");

        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(rest).getJobInfo(Matchers.eq("449f8e8f5940483ea6938ce6cdbea117"));
        verify(rest).updateJobInfo(Matchers.eq("449f8e8f5940483ea6938ce6cdbea117"), argument.capture());

        assertEquals(argument.getValue(), updates);
    }

    private void assertRecordSauceJobResults(SODMappedBuildConfiguration config, int times) throws Exception {
        final SauceREST rest = mock(SauceREST.class);
        when(rest.getJobInfo(anyString())).thenReturn(
            IOUtils.toString(getClass().getResourceAsStream("/job_info.json"), "UTF-8")
        );

        final BuildLoggerManager loggerManager = mock(BuildLoggerManager.class);
        Mockito.doReturn(mock(BuildLogger.class)).when(loggerManager).getLogger(any(ResultKey.class));

        final PostBuildAction pba = mock(PostBuildAction.class);
        Mockito.doReturn(config).when(pba).getBuildConfiguration(any(BuildContext.class));
        Mockito.doNothing().when(pba).recordSauceJobResult(any(SODMappedBuildConfiguration.class));
        Mockito.doReturn(rest).when(pba).getSauceREST(any(SODMappedBuildConfiguration.class));
        Mockito.doReturn("1234").when(pba).getBuildNumber();
        Mockito.doReturn(loggerManager).when(pba).getBuildLoggerManager();
        Mockito.doCallRealMethod().when(pba).call();
        Mockito.doCallRealMethod().when(pba).init(any(BuildContext.class));
        Mockito.doCallRealMethod().when(pba).setCustomVariableContext(any(CustomVariableContext.class));

        final BuildContext buildContext = mock(BuildContext.class);

        pba.setCustomVariableContext(mock(CustomVariableContext.class));
        pba.init(buildContext);
        pba.call();

        verify(pba, times(times)).recordSauceJobResult(any(SODMappedBuildConfiguration.class));
    }

    @Test
    public void testRecordSauceJobResult_Enabled_NoSauceConnect() throws Exception {
        SODMappedBuildConfiguration config = mock(SODMappedBuildConfiguration.class);
        Mockito.doReturn(true).when(config).isEnabled();
        Mockito.doReturn(false).when(config).isSauceConnectEnabled();
        assertRecordSauceJobResults(config, 1);
    }

    @Test
    public void testRecordSauceJobResult_Enabled_SauceConnect() throws Exception {
        SODMappedBuildConfiguration config = mock(SODMappedBuildConfiguration.class);
        Mockito.doReturn(true).when(config).isEnabled();
        Mockito.doReturn(true).when(config).isSauceConnectEnabled();
        assertRecordSauceJobResults(config, 1);
    }

    @Test
    public void testRecordSauceJobResult_Disabled_NotSauceConnect() throws Exception {
        SODMappedBuildConfiguration config = mock(SODMappedBuildConfiguration.class);
        Mockito.doReturn(false).when(config).isEnabled();
        Mockito.doReturn(false).when(config).isSauceConnectEnabled();
        assertRecordSauceJobResults(config, 0);
    }
}