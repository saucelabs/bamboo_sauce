package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;

import java.util.List;
import java.util.Map;

/**
 * {@link Condition} instance that controls whether the 'Sauce OnDemand' tab appears for Bamboo build
 * results.
 *
 * @author Ross Rowe
 */
public class ViewSODCondition implements Condition {
    private PlanManager planManager;
    private CachedPlanManager cachedPlanManager;

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public void setCachedPlanManager(CachedPlanManager cachedPlanManager) {
        this.cachedPlanManager = cachedPlanManager;
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
        if (!context.containsKey("planKey") || !context.containsKey("buildKey")) { return true; }

        final ImmutableChain chain = cachedPlanManager.getPlanByKey(PlanKeys.getPlanKey(
            context.get("planKey").toString()
        ), ImmutableChain.class);
        if (chain == null) { return false; }

        for (ImmutableJob job: chain.getAllJobs()) {
            SODMappedBuildConfiguration sodMappedBuildConfiguration = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
            if (sodMappedBuildConfiguration.isEnabled()) {
                return true;
            }
        }
        return false;
    }

}
