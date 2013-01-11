package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.LogInterceptor;
import com.saucelabs.bamboo.sod.action.PostBuildAction;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ross Rowe
 */
public class SauceLogInterceptor implements LogInterceptor {

    private List<LogEntry> logEntries;

    public SauceLogInterceptor() {
        this.logEntries = new ArrayList<LogEntry>();
    }

    public void intercept(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            logEntries.add(logEntry);
        }
    }

    public void interceptError(@NotNull LogEntry logEntry) {
        if (StringUtils.containsIgnoreCase(logEntry.getLog(), PostBuildAction.SAUCE_ON_DEMAND_SESSION_ID)) {
            logEntries.add(logEntry);
        }
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void clearLogEntries() {
        logEntries = new ArrayList<LogEntry>();

    }
}
