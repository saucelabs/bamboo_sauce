package it.com.saucelabs.bamboo.sod;

import com.saucelabs.bamboo.sod.AbstractTestHelper;
import com.saucelabs.bamboo.sod.plan.ViewSODAction;
import com.saucelabs.bamboo.sod.util.BambooSauceFactory;
import com.saucelabs.rest.Credential;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvokeBuildTest {
    private static final String BASE_URL = "http://localhost:" + AbstractTestHelper.PORT + "/bamboo/rest/api/latest/%1$s?os_authType=basic";
    private static final int MAX_RETRIES = 5;

    /**
    * Invokes a build via the Bamboo REST API. The Bamboo project has been previously setup to run a test
    * using Sauce Connect.
    * 
    * Checks to see if the build was successful, and if so, query
    * Sauce OnDemand via the REST interface to verify that a job was registered with the build key.
    */
    @Test
    public void runBuild() throws Exception {

        boolean foundSauceResult = false;
        String response = doGet("result/TEST-MASTER");
        Document document = parseXml(response);
        String initialSize = getValueForNode(document, "/results/results/@size");
        response = doPost("queue/TEST-MASTER");
        for (int i = 0; i < MAX_RETRIES; i++) {
            response = doGet("result/TEST-MASTER");
            document = parseXml(response);
            String size = getValueForNode(document, "/results/results/@size");
            if (!(size.equals(initialSize))) {
                //assert build was successful
                String buildState = getValueForNode(document, "/results/results/result[1]/@state");
                assertEquals("Build was not successful", buildState, "Successful");
                String buildKey = getValueForNode(document, "/results/results/result[1]/@key");
                String jobId = findJobWithBuildKey(buildKey);
                if (jobId != null) {
                    foundSauceResult = true;
                    break;
                }
            }
            Thread.sleep(1000 * 60);
            i++;
        }
        assertTrue("Unable to find sauce result", foundSauceResult);

    }

    private String findJobWithBuildKey(String buildKey) throws IOException, JSONException {
        Credential credential = new Credential();
        String jsonResponse = new BambooSauceFactory().doREST(String.format(ViewSODAction.JOB_DETAILS_URL, credential.getUsername()), credential.getUsername(), credential.getKey());
        JSONArray jobResults = new JSONArray(jsonResponse);
        for (int i = 0; i < jobResults.length(); i++) {
            //check custom data to find job that was for build
            JSONObject jobData = jobResults.getJSONObject(i);
            if (!jobData.isNull("build")) {
                String buildResultKey = jobData.getString("build");
                if (buildResultKey.equals(buildKey)) {
                    return jobData.getString("id");
                }
            }
        }
        return null;
    }

    public Document parseXml(String xml) throws DocumentException {
        SAXReader reader = new SAXReader();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document document = reader.read(stream);
        return document;
    }


    private String getValueForNode(Document document, String xpathExpression) throws JaxenException {

        XPath xpath = new Dom4jXPath(xpathExpression);
        return xpath.stringValueOf(document);

    }


    private String doGet(String action) throws IOException {
        return doRest(String.format(BASE_URL, action), "GET");
    }

    private String doPost(String action) throws IOException {
        return doRest(String.format(BASE_URL, action), "POST");
    }

    private String doRest(String urlLocation, String method) throws IOException {
        URL url = new URL(urlLocation);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // set the credential
        String userpassword = "admin:admin";
        con.setRequestProperty("Authorization", "Basic " + new BASE64Encoder().encode(userpassword.getBytes()));

        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/xml");
        con.setDoOutput(true);
        con.connect();
        assertEquals("Response code not valid", con.getResponseCode(), 200);
        BufferedReader rd = null;
        try {

            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (rd != null) {
                rd.close();

            }
        }
    }
}
