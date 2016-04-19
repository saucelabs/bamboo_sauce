package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;

import java.util.Map;

/**
 * {@link Condition} instance that controls whether the 'Sauce OnDemand' tab appears for Bamboo build
 * results.
 *
 * @author Ross Rowe
 */
public class ViewSODCondition implements Condition {
    protected PlanManager planManager;

    public PlanManager getPlanManager() {
        return planManager;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }


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
        if (!context.containsKey("buildKey")) { return true; }
        Plan plan = planManager.getPlanByKey(PlanKeys.getPlanKey(context.get("buildKey").toString()));
        if (plan == null) { return true; }
        SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(
            plan.getBuildDefinition().getCustomConfiguration()
        );
        if (config == null) { return true; }
        return config.isEnabled();
    }
}
