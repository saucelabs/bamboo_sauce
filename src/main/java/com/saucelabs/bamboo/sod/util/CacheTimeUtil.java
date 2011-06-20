package com.saucelabs.bamboo.sod.util;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Contains several utility methods to perform current time calculations.
 * 
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public final class CacheTimeUtil
{
    /**
     * Class can't be constructed.
     */
    private CacheTimeUtil() {
    }
    
    private static Timestamp getMaxTimestampForDuration(Timestamp startTime, String duration) throws InvalidDurationException
    {
        long maxSeconds = DateUtils.getDuration(duration, 24, 7, DateUtils.Duration.DAY);
        long maxMillis = maxSeconds * 1000;
        return new Timestamp(startTime.getTime() + maxMillis);
    }

    public static Timestamp getCurrentTimestamp()
    {
        Date now = new Date();
        return new Timestamp(now.getTime());
    }

    public static boolean pastAcceptableDuration(Timestamp startTime, String duration) {

        boolean past = false;

        try
        {
            // Get the maximum end time
            Timestamp maxTimestamp = getMaxTimestampForDuration(startTime, duration);

            // Get the time elapsed since creation
            Timestamp timeElapsed = getCurrentTimestamp();

            // If we're after the max we return true
            if(timeElapsed.after(maxTimestamp)) {
                past = true;
            }

        } catch (InvalidDurationException e)
        {
            past = false;
        }

        return past;
    }
}
