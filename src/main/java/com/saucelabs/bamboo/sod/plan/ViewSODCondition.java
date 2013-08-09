package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
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

    private Map<String, String> map;

    private PlanManager planManager;

    private CachedPlanManager cachedPlanManager;


    public void init(Map<String, String> map) throws PluginParseException {
        this.map = map;
    }

    /**
     * Always returns true.
     *
     * @param planMap
     * @return
     */
    public boolean shouldDisplay(Map<String, Object> planMap) {

        boolean result = false;
        ImmutableChain chain = cachedPlanManager.getPlanByKey(PlanKeys.getPlanKey((String) planMap.get("planKey")), ImmutableChain.class);
        if (chain != null) {
            for (ImmutableJob job : chain.getAllJobs()) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (config.isEnabled()) {
                    result = true;
                    break;
                }
            }
        }

        return result;

    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setCachedPlanManager(CachedPlanManager cachedPlanManager) {
        this.cachedPlanManager = cachedPlanManager;
    }
}
