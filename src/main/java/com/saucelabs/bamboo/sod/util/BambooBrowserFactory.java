package com.saucelabs.bamboo.sod.util;

import com.saucelabs.ci.BrowserFactory;

/**
 * @author Ross Rowe
 */
public class BambooBrowserFactory extends BrowserFactory {
    
    private BambooSauceFactory sauceAPIFactory;

    @Override
    public boolean shouldRetrieveBrowsers() {
        return super.shouldRetrieveBrowsers()|| CacheTimeUtil.pastAcceptableDuration(lastLookup, "1h");
    }

    public BambooSauceFactory getSauceAPIFactory() {
        return sauceAPIFactory;
    }

    public void setSauceAPIFactory(BambooSauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }
}
