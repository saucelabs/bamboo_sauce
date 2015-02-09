package com.saucelabs.bamboo.sod.variables;

import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.saucelabs.ci.BrowserFactory;

import java.util.Map;

/**
 * Interface that defines the behaviour for classes that control the generation and restoration of
 * Sauce OnDemand environment variables.
 *
 * @author Ross Rowe
 */
public interface VariableModifier {


    /**
     * @deprecated populates the build configuration with the environment variable definitions.  The {@link #getVariables()}
     * will produce a map of variables, which will be set within the variable context.  Although these variables are prefixed
     * with 'bamboo_', this will be the preferred approach moving forward.
     */
    void storeVariables();

    void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager);

    void setSauceBrowserFactory(BrowserFactory sauceBrowserFactory);

    void populateVariables(VariableContext variableContext);
}