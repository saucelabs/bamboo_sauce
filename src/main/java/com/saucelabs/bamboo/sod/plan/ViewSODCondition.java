package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
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

    private Map<String, String> map;

    private PlanManager planManager;


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
        Plan plan = planManager.getPlanByKey(PlanKeys.getPlanKey((String) planMap.get("planKey")));
        List<Job> jobs;
        if (plan != null) {
            jobs = planManager.getPlansByProject(plan.getProject(), Job.class);
            for (Job job : jobs) {
                final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(job.getBuildDefinition().getCustomConfiguration());
                if (config.isEnabled()) {
                    result = true;
                }
            }
        }

        return result;

    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }
}
