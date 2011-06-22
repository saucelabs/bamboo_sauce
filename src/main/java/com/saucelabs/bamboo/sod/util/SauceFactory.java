package com.saucelabs.bamboo.sod.util;

import com.saucelabs.rest.Credential;
import com.saucelabs.rest.SauceTunnelFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Delegates requests to the Sauce API.
 *
 * @author Ross Rowe
 */
public class SauceFactory {

    private static final Logger logger = Logger.getLogger(SauceFactory.class);
    private static SauceFactory instance;

    /**
     * @param username
     * @param apiKey
     * @return a new {@link SauceTunnelFactory} instance
     */
    public SauceTunnelFactory createSauceTunnelFactory(String username, String apiKey) {
        return new SauceTunnelFactory(new Credential(username, apiKey));
    }

    public String doREST(String urlText) throws IOException {
        return doREST(urlText, null, null);
    }

    public synchronized String doREST(String urlText, final String userName, final String password) throws IOException {
        URL url = new URL(urlText);


        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }
            });
        }


        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

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
