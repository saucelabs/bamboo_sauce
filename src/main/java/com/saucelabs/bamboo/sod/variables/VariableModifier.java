package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.saucelabs.bamboo.sod.BrowserFactory;
import org.json.JSONException;

/**
 * @author Ross Rowe
 */
public interface VariableModifier {
    void storeVariables() throws JSONException;

    void restoreVariables();

    void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager);


    void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory);

}