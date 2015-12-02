package com.saucelabs.bamboo.sod.plan;

import com.saucelabs.ci.JobInformation;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.util.BambooSauceFactory;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
     * <p>
     * Attempts to retrieve the Sauce Session Id from the custom build data (it will be set if the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class detects if
     * the test output contains a line starting with 'SauceOnDemandSessionID').  If the session id has not been set in the custom build data,
     * then we attempt to retrieve the job id via the Sauce REST API.
     * </p>
     * <p>
     * If a job id is found, then it is stored in the <code>jobId</code> instance variable,
     * for use by the sodView.ftl template. A HMAC token is also generated, which will be used
     * to authenticate the embedded job result requests.
     * </p>
     *
     * @return 'default'
     * @throws Exception thrown if an error occurs generating the key
     */
    @Override
    public String doDefault() throws Exception {
        logger.info("Processing ViewSODAction");

        Plan plan = planManager.getPlanByKey(PlanKeys.getPlanKey(getBuildKey()));
        ResultsSummary resultSummary = resultsSummaryManager.getResultsSummary(PlanKeys.getPlanResultKey(getBuildKey(), getBuildNumber()));
        if (!(resultSummary instanceof Chain)) {
            //the build number stored within Sauce will be that of the default chain, find the default chain and retrieve the corresponding result summary
            List<Chain> chains = planManager.getPlansByProject(plan.getProject(), Chain.class);
            for (Chain chain : chains) {
                if (getBuildKey().startsWith(chain.getPlanKey().toString())) {
                    setResultsSummary(resultsSummaryManager.getResultsSummary(PlanKeys.getPlanResultKey(chain.getPlanKey(), getBuildNumber())));
                }
            }
        } else {
            setResultsSummary(resultSummary);
        }

        jobInformation = new ArrayList<JobInformation>();
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();

        String username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        retrieveJobIdsFromSauce(username, accessKey);

        List<Job> jobs;
        if (plan != null) {
            jobs = planManager.getPlansByProject(plan.getProject(), Job.class);
            for (Job job : jobs) {
                if (job.getKey().startsWith(getBuildKey())) {
                    final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                    if (StringUtils.isNotEmpty(config.getUsername())) {
                        username = config.getUsername();
                        accessKey = config.getAccessKey();
                        retrieveJobIdsFromSauce(username, accessKey);
                    }
                }
            }
        }

        return super.doDefault();
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
        String url = String.format(JOB_DETAILS_URL, username, PlanKeys.getPlanResultKey(resultsSummary.getPlanKey(), getResultsSummary().getBuildNumber()).getKey());
        logger.info("Invoking REST API for " + url);
        String jsonResponse;
        try {
            jsonResponse = sauceAPIFactory.doREST(url, username, accessKey);
        } catch (IOException e) {
            //error occurred performing REST call.  Log the error and return out
            logger.error("Error occurred invoking Sauce REST API", e);
            return;
        }
        logger.info("REST response " + jsonResponse);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray jobs = jsonObject.getJSONArray("jobs");
            for(int i = 0 ; i < jobs.length(); i++){
                JSONObject jobData = jobs.getJSONObject(i);
                String jobId = jobData.getString("id");
                if (jobId != null) {
                    logger.info("Adding jobInformation for " + jobId);
                    JobInformation information = new JobInformation(jobId, calcHMAC(username, accessKey, jobId));
                    information.populateFromJson(jobData);
                    jobInformation.add(information);
                } else {
                    logger.warn("Unable to find jobId in jsonData");
                }
            }
        } catch (JSONException e) {
            logger.error("Unable to process json returned by saucelabs", e);
        }
    }

    /**
     * @param username Sauce Username
     * @param accessKey Sauce Access key
     * @param jobId job id
     * @return HMAC token
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