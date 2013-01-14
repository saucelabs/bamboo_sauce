package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.LogInterceptor;
import com.saucelabs.bamboo.sod.action.PostBuildAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ross Rowe
 */
public class SauceLogInterceptor implements LogInterceptor {

    private static final Logger logger = Logger.getLogger(SauceLogInterceptor.class);

    private List<LogEntry> logEntries;

    public SauceLogInterceptor() {
        this.logEntries = new ArrayList<LogEntry>();
    }

    public void intercept(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            logger.debug("Adding log entry: " + logEntry.getLog());
            logEntries.add(logEntry);
        } else {
            logger.debug("Skipping line " + logEntry.getLog());
        }
    }

    public void interceptError(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            logger.debug("Adding log entry: " + logEntry.getLog());
            logEntries.add(logEntry);
        } else {
            logger.debug("Skipping line " + logEntry.getLog());
        }
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void clearLogEntries() {
        logEntries = new ArrayList<LogEntry>();

    }
}
