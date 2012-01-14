package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.variables.VariableModifier;
import com.saucelabs.rest.Credential;
import com.saucelabs.rest.JobFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Invoked after a build has finished to reset the environment variables for the builder back to what they were prior
 * to the invocation of Sauce.  The class will also invoke the Sauce REST API to store the Bamboo build number against
 * the Sauce Job.  This will be performed if the output from the Bamboo Build includes a line beginning with 'SauceOnDemandSessionID'
 * (the selenium-client-factory library will output this line).
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class PostBuildAction extends AbstractSauceBuildPlugin implements CustomBuildProcessorServer {

    private static final Logger logger = Logger.getLogger(PostBuildAction.class);
    public static final String SAUCE_ON_DEMAND_SESSION_ID = "SauceOnDemandSessionID";

    /**
     * Populated via dependency injection.
     */
    private PlanManager planManager;

    @NotNull
    public BuildContext call() {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled()) {
            resetEnvironmentVariables(config);
            recordSauceJobResult();
        }
        return buildContext;
    }

    public void init(@NotNull BuildContext context) {
        this.buildContext = context;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    /**
     * Iterates over the output lines from the build.  For each line that begins with 'SauceOnDemandSessionID',
     * store the session id from the line in the custom build data of the build, and invoke the Sauce REST API
     * to store the Bamboo build number
     */
    private void recordSauceJobResult() {
        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        assert plan != null;
        BuildLogger buildLogger = plan.getBuildLogger();
        //iterate over the entries of the build logger to see if one starts with 'SauceOnDemandSessionID'
        for (LogEntry logEntry : buildLogger.getBuildLog()) {
            if (StringUtils.containsIgnoreCase(logEntry.getLog(), SAUCE_ON_DEMAND_SESSION_ID)) {
                //extract session id
                String sessionId = StringUtils.substringBetween(logEntry.getLog(), SAUCE_ON_DEMAND_SESSION_ID + "=", " ");
                if (sessionId == null) {
                    //we might not have a space separating the session id and job-name, so retrieve the text up to the end of the string
                    sessionId = StringUtils.substringAfter(logEntry.getLog(), SAUCE_ON_DEMAND_SESSION_ID + "=");
                }
                if (sessionId != null) {
                    //TODO session id still could be null due to invalid case
                    //TODO extract Sauce Job name (included on log line as 'job-name=')?
                    storeSessionId(sessionId);
                    storeBambooBuildNumberInSauce(sessionId);
                }
            }
        }
    }

    /**
     * Stores the Sauce Job Id in the Bamboo build context.
     *
     * @param sessionId the Sauce Job Id
     */
    private void storeSessionId(String sessionId) {
        //use the custom build data of the parent build context (if one is available) 
        Map<String, String> customBuildData = buildContext.getBuildResult().getCustomBuildData();
        String existingSessionId = customBuildData.get(SODKeys.SAUCE_SESSION_ID);
        if (existingSessionId == null) {
            customBuildData.put(SODKeys.SAUCE_SESSION_ID, sessionId);
        } else {
            customBuildData.put(SODKeys.SAUCE_SESSION_ID, existingSessionId + "," + sessionId);
        }
    }

    /**
     * Invokes the Sauce REST API to store the build number and pass/fail status against the Sauce Job.
     *
     * @param sessionId the Sauce Job Id
     */
    private void storeBambooBuildNumberInSauce(String sessionId) {
        Map<String, Object> updates = new HashMap<String, Object>();
        try {
            Credential credential = new Credential();
            updates.put("build", getBuildNumber());
            if (buildContext.getBuildResult().getBuildState().equals(BuildState.SUCCESS)) {
                updates.put("passed", Boolean.TRUE.toString());
            } else if (buildContext.getBuildResult().getBuildState().equals(BuildState.FAILED)) {
                updates.put("passed", Boolean.FALSE.toString());
            }
            JobFactory jobFactory = new JobFactory(credential);
            jobFactory.update(sessionId, updates);
        } catch (IOException e) {
            logger.error("Unable to set build number", e);
        }
    }

    private String getBuildNumber() {
        return getBuildContextToUse().getBuildResultKey();
    }

    private void resetEnvironmentVariables(final SODMappedBuildConfiguration config) {
        Plan plan = planManager.getPlanByKey(buildContext.getPlanKey());
        if (plan != null) {
            VariableModifier variableModifier = getVariableModifier(config, plan);
            if (variableModifier != null) {
                variableModifier.restoreVariables();
                planManager.savePlan(plan);
            }
        }
    }

    /**
     * Use the parent build context if available, otherwise use the build context.
     *
     * @return
     */
    private BuildContext getBuildContextToUse() {
        return buildContext.getParentBuildContext() == null ? buildContext : buildContext.getParentBuildContext();
    }
}