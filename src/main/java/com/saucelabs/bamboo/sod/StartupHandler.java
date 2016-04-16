package com.saucelabs.bamboo.sod;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.saucelabs.saucerest.SauceREST;

public class StartupHandler implements LifecycleAware {
    @Override
    public void onStart() {
        String ver = "Bamboo/" + com.atlassian.bamboo.util.BuildUtils.getCurrentVersion() + " " +
            "BambooSauceOnDemand/" + com.saucelabs.bamboo.sod.BuildUtils.getCurrentVersion();
        SauceREST.setExtraUserAgent(ver);
    }

    @Override
    public void onStop() {

    }
}
