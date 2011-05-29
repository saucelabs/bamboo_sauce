package com.saucelabs.bamboo.sod.plan;

import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * @author Ross Rowe
 */
public class ViewSODCondition implements Condition {

    private Map<String,String> map;

    private PlanManager planManager;


    public void init(Map<String, String> map) throws PluginParseException {
        this.map = map;        
    }

    public boolean shouldDisplay(Map<String, Object> planMap) {

        //TODO Sauce custom configuration doesn't appear to be available via plan.getBuildDefinition().getCustomConfiguration(), possible Bamboo bug?

//        Plan plan = planManager.getPlanByKey((String) planMap.get("planKey"));
//
//        final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(plan.getBuildDefinition().getCustomConfiguration());
//
//        return config.isEnabled();
        return true;

    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }
}
