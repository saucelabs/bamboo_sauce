package it.com.saucelabs.bamboo.sod;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.saucelabs.ci.SauceFactory;
import org.junit.Ignore;
import org.junit.Test;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvokeBuildTest {
    private static final String BASE_URL = "http://localhost:8081/bamboo/rest/api/latest/%1$s?os_authType=basic";
    private static final int MAX_RETRIES = 10;
    //private static final String BASE_URL = "http://localhost:8081/bamboo/rest/api/latest/%1$s?os_authType=basic&os_username=admin&os_password=admin";

    /**
     * We use HtmlUnit for this test, rather than Selenium/Sauce Connect, as we want to invoke a build that will itself run
     * a Selenium test under Sauce OnDemand.  Once the build is completed, we verify that it was successful by connecting
     * to Sauce OnDemand to see if the corresponding Sauce Job exists.
     *
     * @throws Exception
     */
    @Test
    public void dashboardIsOkay() throws Exception {
        //using HtmlUnit, log in
        final WebClient webClient = new WebClient();
        final HtmlPage page = webClient.getPage("http://localhost:8081/bamboo/start.action?os_username=admin&os_password=admin");

        final String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Master"));

        webClient.closeAllWindows();
    }

    @Test
    public void runBuild() throws Exception {

        boolean foundSauceResult = false;
        String response = doGet("latest/result/TEST-MASTER");
        Document document = parseXml(response);
        String initialSize = getValueForNode(document, "/results/results/@size");
        response = doPost("queue/TEST-MASTER");
        //wait a few minutes or loop until timeout
        for (int i = 0; i < MAX_RETRIES; i++) {

            response = doGet("latest/result/TEST-MASTER");
            document = parseXml(response);
            String size = getValueForNode(document, "/results/results/@size");
            if (size.equals(initialSize)) {
                String buildKey = getValueForNode(document, "//results/@key");
                //assert job was successful
                //query sauce to see if job has been recorded
                foundSauceResult = true;
            }
            Thread.sleep(1000 * 60);
            i++;
        }
        //check to s
        assertTrue("Unable to find sauce result", foundSauceResult);

    }

    public Document parseXml(String xml) throws DocumentException {
        SAXReader reader = new SAXReader();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        Document document = reader.read(stream);
        return document;
    }


    private String getValueForNode(Document document, String xpathExpression) throws JaxenException {

        XPath xpath = new Dom4jXPath(xpathExpression);
//        xpath.setNamespaceContext(new SimpleNamespaceContext(contextMap));
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
