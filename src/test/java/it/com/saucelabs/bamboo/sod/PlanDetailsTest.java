package it.com.saucelabs.bamboo.sod;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PlanDetailsTest extends SeleneseTestCase {

    @Test
    @Ignore
    public void testSaucePlan() throws Exception {
        selenium.open("/allPlans.action");
        selenium.click("login");
        selenium.waitForPageToLoad("30000");
        selenium.type("loginForm_os_username", "admin1");
        selenium.type("loginForm_os_password", "admin1");
        selenium.click("loginForm_save");
        selenium.waitForPageToLoad("30000");
        selenium.click("loginForm_save");
        selenium.waitForPageToLoad("30000");
        selenium.click("createPlanLink");
        selenium.waitForPageToLoad("30000");
        selenium.click("//a[@id='createNewPlan']/span");
        selenium.waitForPageToLoad("30000");
        selenium.select("createPlan_existingProjectKey", "label=New Project");
        selenium.type("createPlan_projectName", "BLAH1");
        selenium.type("createPlan_projectKey", "BLAH1");
        selenium.type("createPlan_chainName", "BLAH1");
        selenium.type("createPlan_chainKey", "BLAH1");
        selenium.select("createPlan_selectedRepository", "label=CVS");
        selenium.select("createPlan_selectedRepository", "label=Subversion");
        selenium.type("createPlan_repository_svn_repositoryUrl", "file:///c:/work/svnrepos/clearcase-bamboo");
        selenium.type("createPlan_builder_ant_buildFile", "build.xml");
        selenium.type("createPlan_builder_ant_target", "clean test");
        selenium.type("createPlan_builder_ant_testResultsDirectory", "**/test-reports/*.xml");
        selenium.click("createPlan_tmp_createAsEnabled");
        selenium.click("createPlan_save");
        selenium.waitForPageToLoad("30000");
        selenium.click("//div[@id='editConfigurationButton']/ul/li/a");
        selenium.click("navJob_BLAH1-BLAH1-JOB1");
        selenium.waitForPageToLoad("30000");
        selenium.click("//div[@id='editConfigurationButton']/ul/li/a");
        selenium.click("editBuilder_BLAH1-BLAH1-JOB1");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isTextPresent("Bamboo Sauce"));
        selenium.click("updateBuildBuilder_custom_sauceondemand_enabled");
        selenium.click("label_updateBuildBuilder_custom_sauceondemand_enabled");
        verifyTrue(selenium.isTextPresent("General Settings"));
        verifyTrue(selenium.isTextPresent("Video & Profile"));
        verifyTrue(selenium.isTextPresent("SSH Tunneling"));
        verifyTrue(selenium.isTextPresent("Environment Vars"));
        verifyEquals("300", selenium.getValue("updateBuildBuilder_custom_sauceondemand_max_duration"));
        verifyEquals("90", selenium.getValue("updateBuildBuilder_custom_sauceondemand_idle_timeout"));
        verifyEquals("http://saucelabs.com", selenium.getValue("updateBuildBuilder_custom_sauceondemand_selenium_url"));
        selenium.click("//div[@id='sauceBuilderTabs']/ul/li[2]");
        selenium.click("//div[@id='sauceBuilderTabs']/ul/li[3]");
        verifyEquals("on", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_enabled"));
        verifyEquals("localhost", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_local_host"));
        verifyEquals("8080", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_local_ports"));
        verifyEquals("80", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_remote_ports"));
        verifyEquals("on", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_auto_domain"));
        verifyEquals("AUTO", selenium.getValue("updateBuildBuilder_custom_sauceondemand_ssh_domains"));
        selenium.click("//div[@id='sauceBuilderTabs']/ul/li[4]");
        selenium.click("updateBuildBuilder_save");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isTextPresent("Job configuration saved successfully."));
    }
}
