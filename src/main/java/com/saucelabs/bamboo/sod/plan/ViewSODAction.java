package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.plan.PlanKeys;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.bamboo.sod.util.SauceFactory;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles invoking the Sauce REST API to find the Sauce Job id that corresponds to the Bamboo build.
 *
 * @author Ross Rowe
 */
public class ViewSODAction extends ViewBuildResults {

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH";

    private String hmac;

    private String jobId;

    public static final String JOB_DETAILS_URL = "http://saucelabs.com/rest/v1/%1$s/jobs?full=true";

    private AdministrationConfigurationManager administrationConfigurationManager;

    /**
     * Populated by dependency injection.
     */
    private SauceFactory sauceAPIFactory;

    /**
     * Invokes the Sauce REST API to retrieve the details for the jobs the user has access to.  Iterates over the jobs
     * and attempts to find the job that has custom data matching the build key/number.  If one is found, then the id
     * of the job is stored in the <code>jobId</code> instance variable, for use by the sodView.ftl template.
     *
     * @return 'default'
     * @throws Exception thrown if an error occurs during the invocation of the Sauce REST API
     */
    @Override
    public String doDefault() throws Exception {

        setResultsSummary(resultsSummaryManager.getResultsSummary(PlanKeys.getPlanResultKey(getBuildKey(), getBuildNumber())));
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String username = adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY);
        String accessKey = adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY);
        //invoke Sauce Rest API to find plan results with those values
        String jsonResponse = sauceAPIFactory.doREST(String.format(JOB_DETAILS_URL, username), username, accessKey);
        JSONArray jobResults = new JSONArray(jsonResponse);
        for (int i = 0; i < jobResults.length(); i++) {
            //check custom data to find job that was for build
            JSONObject jobData = jobResults.getJSONObject(i);

            if (!jobData.isNull("build")) {
//                JSONObject customData = (JSONObject) jobData.get("custom-data");
                String buildResultKey = jobData.getString("build");
//                String tags = jobData.getString("tags");
                if (buildResultKey.equals(getResultsSummary().getBuildResultKey())) {
                    jobId = jobData.getString("id");
                    Date now = new Date();
                    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

                    hmac = calcHMAC(username + ":" + accessKey + ":" + format.format(now), jobId);
                    break;
                }
                //try to find Bamboo build information
            }

        }
        //set sauce id variable
        return super.doDefault();
    }

    public String calcHMAC(String key, String msg) throws Exception {

        byte[] keyBytes = key.getBytes();
        SecretKeySpec sks = new SecretKeySpec(keyBytes, "HmacMD5");
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(sks);
        byte[] hmacBytes = mac.doFinal(msg.getBytes());
        byte[] hexBytes = new Hex().encode(hmacBytes);
        String hexStr = new String(hexBytes, "ISO-8859-1");
        return hexStr;

    }

    public void setSauceAPIFactory(SauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public String getJobId() {
        return jobId;
    }

    public String getHmac() {
        return hmac;
    }

}
