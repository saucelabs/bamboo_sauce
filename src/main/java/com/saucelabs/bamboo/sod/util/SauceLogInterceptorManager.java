package com.saucelabs.bamboo.sod.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ross Rowe
 */
public class SauceLogInterceptorManager {

    private Map<String, SauceLogInterceptor> interceptorMap;

    public SauceLogInterceptorManager() {
        this.interceptorMap = new HashMap<String, SauceLogInterceptor>();
    }

    public SauceLogInterceptor createLogInterceptor(String buildResultKey) {
        SauceLogInterceptor interceptor = new SauceLogInterceptor();
        interceptorMap.put(buildResultKey, interceptor);
        return interceptor;
    }

    public SauceLogInterceptor getLogInterceptor(String buildResultKey) {
        return interceptorMap.get(buildResultKey);
    }

    public void removeInterceptor(String buildResultKey) {
        interceptorMap.remove(buildResultKey);
    }
}
