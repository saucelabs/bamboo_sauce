package com.saucelabs.bamboo.sod.plan;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * {@link Condition} instance that controls whether the 'Sauce OnDemand' tab appears for Bamboo build
 * results.
 *
 * @author Ross Rowe
 */
public class ViewSODCondition implements Condition {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Map<String, String> map) throws PluginParseException {
    }

    /**
     * Sauce on demand tab should always be shown
     * {@inheritDoc}
     * @return true
     */
    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return true;
    }
}
