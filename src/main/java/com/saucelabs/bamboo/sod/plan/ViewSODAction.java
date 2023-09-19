package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.saucelabs.ci.JobInformation;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
public class ViewSODAction extends PlanResultsAction {

    private static final Logger logger = Logger.getLogger(ViewSODAction.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH";

    /**
     * Populated by dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

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
        String username, accessKey, dataCenter;
        logger.info("Processing ViewSODAction");

        jobInformation = new ArrayList<JobInformation>();

        ImmutablePlan plan = getImmutablePlan();
        if (plan instanceof ImmutableChain) {
            List<ImmutableChain> chains = cachedPlanManager.getPlansByProject(getImmutablePlan().getProject(), ImmutableChain.class);
            for (ImmutableJob job : ((ImmutableChain) plan).getAllJobs()) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (StringUtils.isNotEmpty(config.getUsername())) {
                    username = config.getUsername();
                    accessKey = config.getAccessKey();
                    dataCenter = config.getDataCenter();
                    jobInformation.addAll(retrieveJobIdsFromSauce(username, accessKey, dataCenter));
                }
                if (jobInformation.size() != 0) {
                    break;
                }
            }
        }
        if (jobInformation.size() == 0) {
            AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();

            username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
            accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
            dataCenter = adminConfig.getSystemProperty(SODKeys.SOD_DATACENTER_KEY);
            jobInformation.addAll(retrieveJobIdsFromSauce(username, accessKey, dataCenter));
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
    private ArrayList<JobInformation> retrieveJobIdsFromSauce(String username, String accessKey, String dataCenter) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        ArrayList<JobInformation> jobInformations = new ArrayList<JobInformation>();

        String buildName = PlanKeys.getPlanResultKey(resultsSummary.getPlanKey(), getResultsSummary().getBuildNumber()).getKey();
        SauceREST sauceREST = new SauceREST(username, accessKey, dataCenter);
        //invoke Sauce Rest API to find plan results with those values
        logger.info("Fetching jobs for build " + buildName);
        String jsonResponse = sauceREST.getBuildFullJobs(buildName);
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
                    jobInformations.add(information);
                } else {
                    logger.warn("Unable to find jobId in jsonData");
                }
            }
        } catch (JSONException e) {
            logger.error("Unable to process json returned by saucelabs", e);
        }
        return jobInformations;
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