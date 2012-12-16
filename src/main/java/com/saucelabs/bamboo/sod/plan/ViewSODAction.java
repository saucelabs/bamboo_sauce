package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.util.BambooSauceFactory;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Handles invoking the Sauce REST API to find the Sauce Job id that corresponds to the Bamboo build.
 *
 * @author Ross Rowe
 */
public class ViewSODAction extends ViewBuildResults {

    private static final Logger logger = Logger.getLogger(ViewSODAction.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH";

    public static final String JOB_DETAILS_URL = "http://saucelabs.com/rest/v1/%1$s/build/%2$s/jobs?full=true";

    public static final String JOB_DETAIL_URL = "http://saucelabs.com/rest/v1/%1$s/jobs/%2$s";

    /**
     * Populated by dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    /**
     * Populated by dependency injection.
     */
    private BambooSauceFactory sauceAPIFactory;
    private static final String HMAC_KEY = "HMACMD5";

    private List<JobInformation> jobInformation;

    /**
     * Attempts to retrieve the Sauce Session Id from the custom build data (it will be set if the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class detects if
     * the test output contains a line starting with 'SauceOnDemandSessionID').  If the session id has not been set in the custom build data,
     * then we attempt to retrieve the job id via the Sauce REST API.
     * <p/>
     * If a job id is found, then it is stored in the <code>jobId</code> instance variable,
     * for use by the sodView.ftl template. A HMAC token is also generated, which will be used
     * to authenticate the embedded job result requests.
     *
     * @return 'default'
     * @throws Exception thrown if an error occurs generating the key
     */
    @Override
    public String doDefault() throws Exception {
        logger.info("Processing ViewSODAction");
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();

        String username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        Plan plan = planManager.getPlanByKey(PlanKeys.getPlanKey(getBuildKey()));
        List<Job> jobs;
        if (plan != null) {
            jobs = planManager.getPlansByProject(plan.getProject(), Job.class);
            for (Job job : jobs) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (StringUtils.isNotEmpty(config.getUsername())) {
                    username = config.getUsername();
                    accessKey = config.getAccessKey();
                }
            }
        }

        setResultsSummary(resultsSummaryManager.getResultsSummary(PlanKeys.getPlanResultKey(getBuildKey(), getBuildNumber())));
        if (buildResultsSummary == null) {
            //we are on the Plan results pages, so drill down to the chain results to find the custom data
            if (resultsSummary instanceof ChainResultsSummary) {
                ChainResultsSummary chainSummary = (ChainResultsSummary) resultsSummary;
                List<ChainStageResult> chainStageResults = chainSummary.getStageResults();
                for (ChainStageResult chainStageResult : chainStageResults) {
                    Set<BuildResultsSummary> buildResultSummaries = chainStageResult.getBuildResults();
                    for (BuildResultsSummary summary : buildResultSummaries) {
                        processBuildResultsSummary(summary, username, accessKey);
                    }
                }
            }
        } else {
            processBuildResultsSummary(buildResultsSummary, username, accessKey);
        }

        return super.doDefault();
    }

    /**
     * @param summary
     * @param username
     * @param accessKey
     * @throws InvalidKeyException          thrown if an error occurs generating the key
     * @throws NoSuchAlgorithmException     thrown if an error occurs generating the key
     * @throws UnsupportedEncodingException thrown if an error occurs generating the key
     */
    private void processBuildResultsSummary(BuildResultsSummary summary, String username, String accessKey) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        logger.info("Processing build summary");
        String storedJobIds = summary.getCustomBuildData().get(SODKeys.SAUCE_SESSION_ID);

        jobInformation = new ArrayList<JobInformation>();
        if (storedJobIds == null) {
            logger.info("No stored job ids");
            retrieveJobIdsFromSauce(username, accessKey);
        } else {
            String[] jobIds = storedJobIds.split(",");
            for (String jobId : jobIds) {
                try {
                    JSONObject jsonObject = retrieveJobInfoFromSauce(username, accessKey, jobId);
                    JobInformation information = new JobInformation(jobId, calcHMAC(username, accessKey, jobId));
                    if (jsonObject.get("passed").equals(Boolean.TRUE)) {
                        information.setStatus("Passed");
                    } else {
                        information.setStatus("Failed");
                    }
                    jobInformation.add(information);
                } catch (IOException e) {
                    //log and attempt to continue
                    logger.error("Error retrieving results from Sauce OnDemand", e);
                }
            }
        }
    }

    /**
     * Invokes the Sauce REST API to retrieve the details for the jobs the user has access to.  Iterates over the jobs
     * and attempts to find the job that has a 'build' field matching the build key/number.
     *
     * @param username
     * @param accessKey
     * @throws Exception
     */
    private void retrieveJobIdsFromSauce(String username, String accessKey) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        //invoke Sauce Rest API to find plan results with those values
        String url = String.format(JOB_DETAILS_URL, username, getResultsSummary().getBuildResultKey());
        logger.info("Invoking REST API for " + url);
        String jsonResponse = null;
        try {
            jsonResponse = sauceAPIFactory.doREST(url, username, accessKey);
        } catch (IOException e) {
            //error occurred performing REST call.  Log the error and return out
            logger.error("Error occurred invoking Sauce REST API", e);
            return;
        }
        logger.info("REST response " + jsonResponse);
        JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonResponse);
        JSONArray jobs = (JSONArray) jsonObject.get("jobs");
        for (Object object : jobs) {
            JSONObject jobObject = (JSONObject) object;
            String jobId = (String) jobObject.get("id");
            if (jobId != null) {
                logger.info("Adding jobInformation for " + jobId);
                JobInformation information = new JobInformation(jobId, calcHMAC(username, accessKey, jobId));
                Object passed = jobObject.get("passed");
                if (passed != null) {
                    if (passed.equals(Boolean.TRUE)) {
                        information.setStatus("Passed");
                    } else {
                        information.setStatus("Failed");
                    }
                }
                jobInformation.add(information);
            } else {
                logger.warn("Unable to find jobId in jsonData");
            }
        }

    }

    /**
     * @param username
     * @param accessKey
     * @param jobId
     * @return
     * @throws IOException thrown if an error occurs invoking Sauce REST API
     */
    private JSONObject retrieveJobInfoFromSauce(String username, String accessKey, String jobId) throws IOException {
        //invoke Sauce Rest API to find plan results with those values
        String url = String.format(JOB_DETAIL_URL, username, jobId);
        logger.info("Invoking REST API for " + url);
        String jsonResponse = sauceAPIFactory.doREST(url, username, accessKey);
        logger.info("REST response " + jsonResponse);
        return (JSONObject) JSONValue.parse(jsonResponse);

    }

    /**
     * @param username
     * @param accessKey
     * @param jobId
     * @return
     * @throws NoSuchAlgorithmException     thrown if an error occurs generating the key
     * @throws InvalidKeyException          thrown if an error occurs generating the key
     * @throws UnsupportedEncodingException thrown if an error occurs generating the key
     */
    public String calcHMAC(String username, String accessKey, String jobId) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String key = username + ":" + accessKey + ":" + format.format(calendar.getTime());
        byte[] keyBytes = key.getBytes();
        SecretKeySpec sks = new SecretKeySpec(keyBytes, HMAC_KEY);
        Mac mac = Mac.getInstance(sks.getAlgorithm());
        mac.init(sks);
        byte[] hmacBytes = mac.doFinal(jobId.getBytes());
        byte[] hexBytes = new Hex().encode(hmacBytes);
        return new String(hexBytes, "ISO-8859-1");
    }

    public void setSauceAPIFactory(BambooSauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public List<JobInformation> getJobInformation() {
        return jobInformation;
    }

    @Override
    public boolean isRestartable(@NotNull ResultsSummary resultsSummary) {
        return false;
    }
}