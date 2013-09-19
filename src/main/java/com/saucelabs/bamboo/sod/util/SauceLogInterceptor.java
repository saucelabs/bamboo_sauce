package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.LogInterceptor;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.saucelabs.bamboo.sod.action.PostBuildAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ross Rowe
 */
public class SauceLogInterceptor implements LogInterceptor {

    private static final Logger logger = Logger.getLogger(SauceLogInterceptor.class);
    private final BuildContext buildContext;


    public SauceLogInterceptor(BuildContext buildContext) {

        this.buildContext = buildContext;
    }

    public void intercept(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding log entry: " + logEntry.getLog());
            }
            CurrentBuildResult buildResult = buildContext.getBuildResult();
            buildResult.getCustomBuildData().put("SAUCE_JOB_ID_" + System.currentTimeMillis(), logEntry.getLog());

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping line " + logEntry.getLog());
            }
        }
    }

    public void interceptError(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding log entry: " + logEntry.getLog());
            }
            CurrentBuildResult buildResult = buildContext.getBuildResult();
            buildResult.getCustomBuildData().put("SAUCE_JOB_ID_" + System.currentTimeMillis(), logEntry.getLog());


        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping line " + logEntry.getLog());
            }
        }
    }

}
