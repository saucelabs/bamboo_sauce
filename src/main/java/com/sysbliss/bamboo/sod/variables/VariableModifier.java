package com.sysbliss.bamboo.sod.variables;

import org.json.JSONException;

/**
 * @author Ross Rowe
 */
public interface VariableModifier {
    void storeVariables() throws JSONException;

    void restoreVariables();
}