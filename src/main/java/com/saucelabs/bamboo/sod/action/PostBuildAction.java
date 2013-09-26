package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.CustomBuildProcessorServer;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogUtils;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Populated via dependency injection.
     */
    private BuildLoggerManager buildLoggerManager;


    @NotNull
    public BuildContext call() {
        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled()) {
            try {
                recordSauceJobResult(config);
            } catch (IOException e) {
                logger.error(e);
            }
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
     *
     * @param config
     */
    private void recordSauceJobResult(SODMappedBuildConfiguration config) throws IOException {
        //iterate over the entries of the build logger to see if one starts with 'SauceOnDemandSessionID'
        boolean foundLogEntry = false;
        logger.debug("Checking log interceptor entries");

        CurrentBuildResult buildResult = buildContext.getBuildResult();
        for (Map.Entry<String, String> entry : buildResult.getCustomBuildData().entrySet()) {
            if (entry.getKey().contains("SAUCE_JOB_ID")) {
                foundLogEntry = foundLogEntry || processLine(config, foundLogEntry, entry.getValue());
            }
        }

        if (!foundLogEntry) {
            logger.warn("No Sauce Session ids found in build context, reading from log file");
            //try read from the log file directly
            File logDirectory = BuildLogUtils.getLogFileDirectory(buildContext.getPlanKey());
            String logFileName = BuildLogUtils.getLogFileName(buildContext.getPlanKey(), buildContext.getBuildNumber());
            List lines = FileUtils.readLines(new File(logDirectory, logFileName));
            for (Object object : lines) {
                foundLogEntry = foundLogEntry || processLine(config, foundLogEntry, (String) object);

            }
        }

        //if we still don't have anything, try the build logger output.  This will only have the last 100 lines.
        if (!foundLogEntry) {
            logger.warn("No Sauce Session ids found in log file, reading from build logger output");
            BuildLogger buildLogger = buildLoggerManager.getBuildLogger(buildContext.getBuildResultKey());
            for (LogEntry logEntry : buildLogger.getBuildLog()) {
                foundLogEntry = foundLogEntry || processLine(config, foundLogEntry, logEntry.getLog());

            }
        }

        if (!foundLogEntry) {
            logger.warn("No Sauce Session ids found in build output");
        }

    }

    private boolean processLine(SODMappedBuildConfiguration config, boolean foundLogEntry, String line) {
        //extract session id
        String sessionId = StringUtils.substringBetween(line, SAUCE_ON_DEMAND_SESSION_ID + "=", " ");
        if (sessionId == null) {
            //we might not have a space separating the session id and job-name, so retrieve the text up to the end of the string
            sessionId = StringUtils.substringAfter(line, SAUCE_ON_DEMAND_SESSION_ID + "=");
        }
        if (sessionId != null && !sessionId.equalsIgnoreCase("null")) {
            //TODO extract Sauce Job name (included on log line as 'job-name=')?
            foundLogEntry = true;
            storeBambooBuildNumberInSauce(config, sessionId);
        }
        return foundLogEntry;
    }

    /**
     * Invokes the Sauce REST API to store the build number and pass/fail status against the Sauce Job.
     *
     * @param config
     * @param sessionId the Sauce Job Id
     */
    private void storeBambooBuildNumberInSauce(SODMappedBuildConfiguration config, String sessionId) {
        SauceREST sauceREST = new SauceREST(config.getTempUsername(), config.getTempApikey());

        Map<String, Object> updates = new HashMap<String, Object>();
        try {
            String json = sauceREST.getJobInfo(sessionId);
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            updates.put("build", getBuildNumber());
            if (jsonObject.get("passed") == null || jsonObject.get("passed").equals("")) {
                if (buildContext.getBuildResult().getBuildState().equals(BuildState.SUCCESS)) {
                    updates.put("passed", Boolean.TRUE.toString());
                } else if (buildContext.getBuildResult().getBuildState().equals(BuildState.FAILED)) {
                    updates.put("passed", Boolean.FALSE.toString());
                }
            }

            logger.debug("About to update job " + sessionId + " with build number " + getBuildNumber());
            sauceREST.updateJobInfo(sessionId, updates);
        } catch (ParseException e) {
            logger.error("Unable to set build number", e);
        }
    }

    private String getBuildNumber() {
        return getBuildContextToUse().getBuildResultKey();
    }

    /**
     * Use the parent build context if available, otherwise use the build context.
     *
     * @return
     */
    private BuildContext getBuildContextToUse() {
        return buildContext.getParentBuildContext() == null ? buildContext : buildContext.getParentBuildContext();
    }

    public void setBuildLoggerManager(BuildLoggerManager buildLoggerManager) {
        this.buildLoggerManager = buildLoggerManager;
    }

}