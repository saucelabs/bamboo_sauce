package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.v2.build.CurrentBuildResultImpl;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.Every;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.omg.CORBA.Current;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by gavinmogan on 2016-02-09.
 */
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
            protected void storeBambooBuildNumberInSauce(SODMappedBuildConfiguration config, String sessionId, String jobName) {
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
        Mockito.verify(rest).getJobInfo(Matchers.eq("449f8e8f5940483ea6938ce6cdbea117"));
        Mockito.verify(rest).updateJobInfo(Matchers.eq("449f8e8f5940483ea6938ce6cdbea117"), argument.capture());

        assertEquals(argument.getValue(), updates);
    }
}