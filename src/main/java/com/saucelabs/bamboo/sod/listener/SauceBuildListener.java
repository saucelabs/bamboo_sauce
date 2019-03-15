package com.saucelabs.bamboo.sod.listener;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.build.BuildDefinitionManager;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.event.BuildCanceledEvent;
import com.atlassian.bamboo.event.BuildHungEvent;
import com.atlassian.bamboo.event.HibernateEventListener;
import com.atlassian.event.Event;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.saucerest.SauceREST;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ross Rowe
 */
public class SauceBuildListener implements HibernateEventListener {

    private static final Logger logger = Logger.getLogger(SauceBuildListener.class);

    public static final String JOB_DETAILS_URL = "http://saucelabs.com/rest/v1/%1$s/jobs?full=true&limit=20";
    public static final String STOP_JOB_URL = "http://saucelabs.com/rest/v1/%1$s/jobs/%2$s/stop";

    private BuildDefinitionManager buildDefinitionManager;
    private AdministrationConfigurationManager administrationConfigurationManager;

    public void handleEvent(Event event) {
        if (event instanceof BuildCanceledEvent) {

            BuildCanceledEvent buildCanceledEvent = (BuildCanceledEvent) event;
            BuildDefinition buildDefinition = buildDefinitionManager.getBuildDefinition(buildCanceledEvent.getPlanKey());
            final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildDefinition.getCustomConfiguration());
            String username = administrationConfigurationManager.getAdministrationConfiguration().getSystemProperty(SODKeys.SOD_USERNAME_KEY);
            String accessKey = administrationConfigurationManager.getAdministrationConfiguration().getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
            String dataCenter = administrationConfigurationManager.getAdministrationConfiguration().getSystemProperty(SODKeys.SOD_DATACENTER_KEY);

            SauceREST sauceREST = new SauceREST(username, accessKey, dataCenter);
            try {
                String jsonResponse = sauceREST.getFullJobs();
                JSONArray jobResults = new JSONArray(jsonResponse);
                if (jobResults == null) {
                    logger.info("Unable to find job data for " + buildCanceledEvent.getPlanKey());

                } else {
                    for (int i = 0; i < jobResults.length(); i++) {
                        //check custom data to find job that was for build
                        JSONObject jobData = jobResults.getJSONObject(i);
                        //if job is in progress
                        String status = jobData.getString("status");
                        if (status.equals("in progress")) {
                            String jobId = jobData.getString("id");
                            sauceREST.stopJob(jobId);
                        }

                    }
                }
            } catch (JSONException e) {
                logger.error("Unable to fetch jobs from sauce", e);
            }


        } else if (event instanceof BuildHungEvent) {
            BuildHungEvent buildHungEvent = (BuildHungEvent) event;
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class[] getHandledEventClasses() {
        return new Class[]{BuildCanceledEvent.class, BuildHungEvent.class};
    }

    public void setBuildDefinitionManager(BuildDefinitionManager buildDefinitionManager) {
        this.buildDefinitionManager = buildDefinitionManager;
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
            this.administrationConfigurationManager = administrationConfigurationManager;
        }
}
