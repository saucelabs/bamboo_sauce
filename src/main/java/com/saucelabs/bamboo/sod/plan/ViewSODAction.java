package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.util.BambooSauceFactory;
import org.apache.commons.codec.binary.Hex;
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
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Handles invoking the Sauce REST API to find the Sauce Job id that corresponds to the Bamboo build.
 * 
 * @author Ross Rowe
 */
public class ViewSODAction extends ViewBuildResults {

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH";

    public static final String JOB_DETAILS_URL = "http://saucelabs.com/rest/v1/%1$s/jobs?full=true";

    /**
     * Populated by dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    /**
     * Populated by dependency injection.
     */
    private BambooSauceFactory sauceAPIFactory;
    private static final String HMAC_KEY = "HmacMD5";
    
    private List<JobInformation> jobInformation;

    /**
     * Attempts to retrieve the Sauce Session Id from the custom build data (it will be set if the {@link com.saucelabs.bamboo.sod.action.PostBuildAction} class detects if
     * the test output contains a line starting with 'SauceOnDemandSessionID').  If the session id has not been set in the custom build data,
     * then we attempt to retrieve the job id via the Sauce REST API. 
     * 
     * If a job id is found, then it is stored in the <code>jobId</code> instance variable, 
     * for use by the sodView.ftl template. A HMAC token is also generated, which will be used
     * to authenticate the embedded job result requests.
     *
     * @return 'default'
     * @throws Exception thrown if an error occurs during the invocation of the Sauce REST API
     */
    @Override
    public String doDefault() throws Exception {

        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
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

    private void processBuildResultsSummary(BuildResultsSummary summary, String username, String accessKey) throws IOException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        String storedJobIds = summary.getCustomBuildData().get(SODKeys.SAUCE_SESSION_ID);
        jobInformation = new ArrayList<JobInformation>();
        if (storedJobIds == null) {
            retrieveJobIdsFromSauce(username, accessKey);
        } else {
            String[] jobIds = storedJobIds.split(",");
            for (String jobId : jobIds) {
                jobInformation.add(new JobInformation(jobId, calcHMAC(username, accessKey, jobId)));
            }
        }
    }

    /**
     * Invokes the Sauce REST API to retrieve the details for the jobs the user has access to.  Iterates over the jobs
     * and attempts to find the job that has a 'build' field matching the build key/number.
     * @param username
     * @param accessKey
     * @throws Exception
     */
    private void retrieveJobIdsFromSauce(String username, String accessKey) throws IOException, JSONException, InvalidKeyException, NoSuchAlgorithmException {
        //invoke Sauce Rest API to find plan results with those values
        String jsonResponse = sauceAPIFactory.doREST(String.format(JOB_DETAILS_URL, username), username, accessKey);
        JSONArray jobResults = new JSONArray(jsonResponse);
        for (int i = 0; i < jobResults.length(); i++) {
            //check custom data to find job that was for build
            JSONObject jobData = jobResults.getJSONObject(i);
            if (!jobData.isNull("build")) {
                String buildResultKey = jobData.getString("build");
                if (buildResultKey.equals(getResultsSummary().getBuildResultKey())) {
                    String jobId = jobData.getString("id");
                    jobInformation.add(new JobInformation(jobId, calcHMAC(username, accessKey, jobId)));
                }
            }
        }
    }

    public String calcHMAC(String username, String accessKey, String jobId) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        String key = username + ":" + accessKey + ":" + format.format(now);
        byte[] keyBytes = key.getBytes();
        SecretKeySpec sks = new SecretKeySpec(keyBytes, HMAC_KEY);
        Mac mac = Mac.getInstance(HMAC_KEY);
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
}