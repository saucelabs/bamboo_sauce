package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.saucelabs.bamboo.sod.config.SODKeys;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Delegates requests to the Sauce API.
 *
 * @author Ross Rowe
 */
public class SauceFactory {

    private static final Logger logger = Logger.getLogger(SauceFactory.class);
    private static SauceFactory instance;

    public String doREST(String urlText) throws IOException {
        return doREST(urlText, null, null);
    }

    /**
     * Invokes a Sauce REST API command 
     * @param urlText
     * @param userName
     * @param password
     * @return results of REST command
     * @throws IOException
     */
    public synchronized String doREST(String urlText, final String userName, final String password) throws IOException {

        URL url = new URL(urlText);
        String auth = userName + ":" + password;
        BASE64Encoder encoder = new BASE64Encoder();
        auth = "Basic " + encoder.encode(auth.getBytes());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", auth);

        // Get the response
        BufferedReader rd = null;
        try {

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            return sb.toString();
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    logger.warn("Exception occurred when closing stream", e);
                }
            }
        }
    }

    public void setupProxy(AdministrationConfigurationManager administrationConfigurationManager) {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String proxyHost = adminConfig.getSystemProperty(SODKeys.PROXY_HOST_KEY);
        String proxyPort = adminConfig.getSystemProperty(SODKeys.PROXY_PORT_KEY);
        String proxyUsername = adminConfig.getSystemProperty(SODKeys.PROXY_USERNAME_KEY);
        String proxyPassword = adminConfig.getSystemProperty(SODKeys.PROXY_PASSWORD_KEY);
        setupProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    /**
     * Populates the http proxy system properties.
     * 
     * @param proxyHost
     * @param proxyPort
     * @param userName
     * @param password
     */
    public void setupProxy(String proxyHost, String proxyPort, final String userName, final String password) {
        if (StringUtils.isNotBlank(proxyHost)) {
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("https.proxyHost", proxyHost);
        }
        if (StringUtils.isNotBlank(proxyPort)) {
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyPort", proxyPort);
        }
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            System.setProperty("http.proxyUser", userName);
            System.setProperty("https.proxyUser", userName);
            System.setProperty("http.proxyPassword", password);
            System.setProperty("https.proxyPassword", password);
        }
    }
    
     /**
     * Returns a singleton instance of SauceFactory.  This is required because
     * remote agents don't have the Bamboo component plugin available, so the Spring
     * auto-wiring doesn't work. 
     * @return
     */
    public static SauceFactory getInstance() {
        if (instance == null) {
            instance = new SauceFactory();
        }
        return instance;
    }
}