package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.google.common.base.Strings;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.ci.JobInformation;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;

import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.saucerest.SauceREST;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    /**
     * Populated by dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    private static final String HMAC_KEY = "HMACMD5";

    private JobInformation jobInformation;

    private String jobId;

    private Credentials findSauceRestForPlan(ImmutablePlan plan) {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String username, accessKey;
        SauceREST sauceREST;

        // bad username or password probably
        if (plan instanceof ImmutableChain) {
            List<ImmutableChain> chains = cachedPlanManager.getPlansByProject(getImmutablePlan().getProject(), ImmutableChain.class);
            for (ImmutableJob job : ((ImmutableChain) plan).getAllJobs()) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (StringUtils.isNotEmpty(config.getUsername())) {
                    username = config.getUsername();
                    accessKey = config.getAccessKey();
                    sauceREST = new SauceREST(username, accessKey);
                    if (!Strings.isNullOrEmpty(sauceREST.getJobInfo(jobId))) {
                        return new Credentials(username, accessKey);
                    }
                }
            }
        }
        username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        sauceREST = new SauceREST(username, accessKey);
        if (!Strings.isNullOrEmpty(sauceREST.getJobInfo(jobId))) {
            return new Credentials(username, accessKey);
        }
        return null;
    }
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
        Credentials credentials = findSauceRestForPlan(getImmutablePlan());
        jobInformation = new JobInformation(jobId, calcHMAC(credentials.username, credentials.accessKey, jobId));
        return super.doDefault();
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

    private class Credentials {
        public final String username;
        public final String accessKey;

        public Credentials(String username, String accessKey) {

            this.username = username;
            this.accessKey = accessKey;
        }
    }
}