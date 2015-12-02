package com.saucelabs.bamboo.sod.plan;

import com.saucelabs.ci.JobInformation;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.util.BambooSauceFactory;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;

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
public class ViewSauceJobAction extends ViewBuildResults {

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
    private static final String HMAC_KEY = "HMACMD5";

    private JobInformation jobInformation;

    private String jobId;

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

    private void processBuildResultsSummary(BuildResultsSummary summary, String username, String accessKey) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        jobInformation = new JobInformation(jobId, calcHMAC(username, accessKey, jobId));
    }

    // FIXME this belongs in saucerest
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

    public JobInformation getJobInformation() {
        return jobInformation;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean isRestartable(@NotNull ResultsSummary resultsSummary) {
        return false;
    }
}