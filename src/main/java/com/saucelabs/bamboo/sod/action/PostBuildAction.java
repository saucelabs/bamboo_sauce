package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.CustomBuildProcessor;
import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogUtils;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.tests.TestState;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.spring.container.ContainerManager;
import com.saucelabs.bamboo.sod.AbstractSauceBuildPlugin;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.ci.JobInformation;
import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Invoked after a build has finished to reset the environment variables for the builder back to what they were prior
 * to the invocation of Sauce.  The class will also invoke the Sauce REST API to store the Bamboo build number against
 * the Sauce Job.  This will be performed if the output from the Bamboo Build includes a line beginning with 'SauceOnDemandSessionID'
 * (the selenium-client-factory library will output this line).
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class PostBuildAction extends AbstractSauceBuildPlugin implements CustomBuildProcessor {

    private static final Logger logger = Logger.getLogger(PostBuildAction.class);
    public static final String SAUCE_ON_DEMAND_SESSION_ID = "SauceOnDemandSessionID";
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("SauceOnDemandSessionID=([0-9a-fA-F]+)(?:.job-name=(.*))?");
    private static final String JOB_NAME_PATTERN = "\\b({0})\\b";


    /**
     * Populated via dependency injection.
     */
    private PlanManager planManager;

    /**
     * Populated via dependency injection.
     */
    private BuildLoggerManager buildLoggerManager;




    private SauceConnectFourManager sauceConnectFourTunnelManager;
    private CustomVariableContext customVariableContext;

    @NotNull
    public BuildContext call() {

        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
        if (config.isEnabled()) {
            BuildLoggerManager buildLoggerManager = (BuildLoggerManager) ContainerManager.getComponent("buildLoggerManager");
            final BuildLogger buildLogger = buildLoggerManager.getLogger(buildContext.getResultKey());
            PrintStream printLogger = new PrintStream(new NullOutputStream()) {
                @Override
                public void println(String x) {
                    buildLogger.addBuildLogEntry(x);
                }
            };

            try {
                SauceTunnelManager sauceTunnelManager = getSauceConnectFourTunnelManager();
                String options = customVariableContext.substituteString(config.getSauceConnectOptions(), buildContext, null);
                sauceTunnelManager.closeTunnelsForPlan(
                    config.getTempUsername(),
                    options,
                    printLogger
                );
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
    protected void recordSauceJobResult(SODMappedBuildConfiguration config) throws IOException {
        //iterate over the entries of the build logger to see if one starts with 'SauceOnDemandSessionID'
        boolean foundLogEntry = false;
        logger.info("Checking log interceptor entries");
        CurrentBuildResult buildResult = buildContext.getBuildResult();
        for (Map.Entry<String, String> entry : buildResult.getCustomBuildData().entrySet()) {
            if (entry.getKey().contains("SAUCE_JOB_ID")) {
                if (processLine(config, entry.getValue())) {
                    foundLogEntry = true;
                }

            }
        }

        logger.info("Reading from log file");
        //try read from the log file directly
        File logDirectory = BuildLogUtils.getLogFileDirectory(buildContext.getPlanKey());
        String logFileName = BuildLogUtils.getLogFileName(buildContext.getPlanResultKey());
        List lines = FileUtils.readLines(new File(logDirectory, logFileName));
        for (Object object : lines) {
            String line = (String) object;
            if (logger.isDebugEnabled()) {
                logger.debug("Processing line: " + line);
            }
            if (processLine(config, line)) {
                foundLogEntry = true;
            }
        }


        logger.info("Reading from build logger output");
        BuildLogger buildLogger = buildLoggerManager.getLogger(buildContext.getResultKey());
        for (LogEntry logEntry : buildLogger.getBuildLog()) {
            if (processLine(config, logEntry.getLog())) {
                foundLogEntry = true;
            }
        }


        if (!foundLogEntry) {
            logger.warn("No Sauce Session ids found in build output");
        }

    }

    protected boolean processLine(SODMappedBuildConfiguration config, String line) {


        //extract session id
        String sessionId = null;
        String jobName = null;
        Matcher m = SESSION_ID_PATTERN.matcher(line);
        while (m.find()) {
            sessionId = m.group(1);
            if (m.groupCount() == 2) {
                jobName = m.group(2);
            }
        }

        if (sessionId == null) {
            sessionId = StringUtils.substringBetween(line, SAUCE_ON_DEMAND_SESSION_ID + "=", " ");
        }
        if (sessionId == null) {
            //we might not have a space separating the session id and job-name, so retrieve the text up to the end of the string
            sessionId = StringUtils.substringAfter(line, SAUCE_ON_DEMAND_SESSION_ID + "=");
        }
        if (sessionId != null && !sessionId.equalsIgnoreCase("null")) {
            if (sessionId.trim().equals("")) {
                logger.error("Session id for line" + line + " was blank");
                return false;
            } else {
                //TODO extract Sauce Job name (included on log line as 'job-name=')?
                storeBambooBuildNumberInSauce(config, sessionId, jobName);
                return true;
            }
        }
        return false;
    }

    /**
     * Invokes the Sauce REST API to store the build number and pass/fail status against the Sauce Job.
     *
     * @param config
     * @param sessionId the Sauce Job Id
     * @param jobName
     */

    protected void storeBambooBuildNumberInSauce(SODMappedBuildConfiguration config, String sessionId, String jobName) {
        SauceREST sauceREST = getSauceREST(config);

        try {
            logger.debug("Invoking Sauce REST API for " + sessionId);
            String json = sauceREST.getJobInfo(sessionId);
            logger.debug("Results: " + json);
            JobInformation jobInformation = new JobInformation(sessionId, "");
            jobInformation.populateFromJson(new org.json.JSONObject(json));
            if (jobInformation.getStatus() == null) {
                Boolean testPassed = hasTestPassed(jobInformation.getName());
                if (testPassed != null) {
                    //set the status to passed if the test was successful
                    jobInformation.setStatus(testPassed.booleanValue() ? "passed" : "failed");
                }
            }
            if (!jobInformation.hasJobName()) {
                jobInformation.setName(jobName);
            }
            if (!jobInformation.hasBuild()) {
                jobInformation.setBuild(getBuildNumber());
            }
            if (jobInformation.hasChanges()) {
                logger.debug("Performing Sauce REST update for " + jobInformation.getJobId());
                sauceREST.updateJobInfo(jobInformation.getJobId(), jobInformation.getChanges());
            }
        } catch (JSONException e) {
            logger.error("Unable to set build number for " + sessionId, e);
        } /*catch (Exception e) {
            logger.error("Unexpected error processing " + sessionId, e);
        }*/

    }

    protected SauceREST getSauceREST(SODMappedBuildConfiguration config) {
        return new SauceREST(config.getTempUsername(), config.getTempApikey());
    }

    private Boolean hasTestPassed(String name) {
        //do we have a test which matches the job name?
        TestResults testResults = findTestResult(name);
        if (testResults != null) {
            return testResults.getState().equals(TestState.SUCCESS);
        }

        return (buildContext.getBuildResult().getBuildState().equals(BuildState.SUCCESS));
    }

    private TestResults findTestResult(String name) {
        if (name == null) {
            return null;
        }
        TestResults testResult = findTestResult(name, buildContext.getBuildResult().getFailedTestResults());
        if (testResult == null) {
            testResult = findTestResult(name, buildContext.getBuildResult().getSuccessfulTestResults());
        }
        return testResult;
    }

    private TestResults findTestResult(String name, Collection<TestResults> testResults) {
        for (TestResults testResult : testResults) {
            Pattern jobNamePattern = Pattern.compile(MessageFormat.format(JOB_NAME_PATTERN, name));
            Matcher matcher = jobNamePattern.matcher(testResult.getActualMethodName());
            if (name.equals(testResult.getActualMethodName()) //if job name equals full name of test
                    || name.contains(testResult.getActualMethodName()) //or if job name contains the test name
                    || matcher.find()) { //or if the full name of the test contains the job name (matching whole words only)
                //then we have a match
                return testResult;
            }
        }
        return null;
    }

    protected String getBuildNumber() {
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

    public SauceConnectFourManager getSauceConnectFourTunnelManager() {
        if (sauceConnectFourTunnelManager == null) {
            setSauceConnectFourTunnelManager(new SauceConnectFourManager());
        }
        return sauceConnectFourTunnelManager;
    }

    public void setSauceConnectFourTunnelManager(SauceConnectFourManager sauceConnectFourTunnelManager) {
        this.sauceConnectFourTunnelManager = sauceConnectFourTunnelManager;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext) {
        this.customVariableContext = customVariableContext;
    }

}