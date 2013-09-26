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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.net.URL;

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

            SauceREST sauceREST = new SauceREST(username, accessKey);
            try {
                String jsonResponse = sauceREST.retrieveResults(new URL(String.format(JOB_DETAILS_URL, username)));
                JSONArray jobResults = (JSONArray) new JSONParser().parse(jsonResponse);
                if (jobResults == null) {
                    logger.info("Unable to find job data for " + buildCanceledEvent.getPlanKey());

                } else {
                    for (int i = 0; i < jobResults.size(); i++) {
                        //check custom data to find job that was for build
                        JSONObject jobData = (JSONObject) jobResults.get(i);
                        //if job is in progress
                        String status = (String) jobData.get("status");
                        if (status.equals("in progress")) {
                            String jobId = (String) jobData.get("id");
                            sauceREST.stopJob(jobId);
                        }

                    }
                }

            } catch (MalformedURLException e) {
                logger.error(e);
            } catch (ParseException e) {
                logger.error(e);
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
