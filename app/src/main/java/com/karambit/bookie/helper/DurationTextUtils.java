package com.karambit.bookie.helper;

import android.content.Context;

import com.karambit.bookie.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by orcan on 1/8/17.
 *
 * This class has calculating functions for duration or time processes
 */

public class DurationTextUtils {

    public static int calculateDayDiff(Calendar before, Calendar after) {
        long diff = after.getTimeInMillis() - before.getTimeInMillis();

        return (int) TimeUnit.MILLISECONDS.toDays(diff);
    }

    public static int calculateDayDiff(Calendar before) {
        return calculateDayDiff(before, Calendar.getInstance());
    }

    public static String getShortDurationString(Context context, Calendar before, Calendar after) {
        long millisecondDiff = after.getTimeInMillis() - before.getTimeInMillis();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondDiff);

        if (minutes == 0) {
            return context.getString(R.string.now);
        } else if (minutes < 60) {
            return context.getString(R.string.minute_short, minutes);

        } else {
            long hours = TimeUnit.MINUTES.toHours(minutes);

            if (hours < 24) {
                return context.getString(R.string.hour_short, hours);

            } else {
                long days = TimeUnit.HOURS.toDays(hours);

                if (days < 7) {
                    return context.getString(R.string.day_short, days);

                } else {
                    long weeks = days / 7;

                    if (weeks < 6) {
                        return context.getString(R.string.week_short, weeks);

                    } else {
                        long months = days / 30;

                        if (months < 12) {
                            return context.getString(R.string.month_short, months);

                        } else {
                            return context.getString(R.string.year_short, months / 12);
                        }
                    }
                }
            }
        }
    }

    public static String getShortDurationString(Context context, Calendar before) {
        return getShortDurationString(context, before, Calendar.getInstance());
    }
}
