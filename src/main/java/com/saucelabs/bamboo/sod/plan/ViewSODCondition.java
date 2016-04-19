package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
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
        if (!context.containsKey("planKey") || !context.containsKey("buildKey")) { return true; }
        Plan plan = planManager.getPlanByKey(PlanKeys.getPlanKey(context.get("planKey").toString()));
        if (plan == null) { return true; }
        List<Job> jobs = planManager.getPlansByProject(plan.getProject(), Job.class);
        for (Job job : jobs) {
            if (job.getKey().startsWith(context.get("buildKey").toString())) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (config.isEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }
}
