package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.ci.SauceFactory;

/**
 * Delegates requests to the Sauce API.
 *
 * @author Ross Rowe
 */
public class BambooSauceFactory extends SauceFactory {

    public void setupProxy(AdministrationConfigurationManager administrationConfigurationManager) {
        AdministrationConfiguration adminConfig = administrationConfigurationManager.getAdministrationConfiguration();
        String proxyHost = adminConfig.getSystemProperty(SODKeys.PROXY_HOST_KEY);
        String proxyPort = adminConfig.getSystemProperty(SODKeys.PROXY_PORT_KEY);
        String proxyUsername = adminConfig.getSystemProperty(SODKeys.PROXY_USERNAME_KEY);
        String proxyPassword = adminConfig.getSystemProperty(SODKeys.PROXY_PASSWORD_KEY);
        setupProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

}