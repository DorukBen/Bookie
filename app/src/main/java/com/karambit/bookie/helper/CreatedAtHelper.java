package com.karambit.bookie.helper;

import android.content.Context;

import com.karambit.bookie.R;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Interaction;
import com.karambit.bookie.model.Request;
import com.karambit.bookie.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by orcan on 2/11/17.
 */

public class CreatedAtHelper {

    public static String createdAtToSimpleString(Context context, Calendar createdAt) {

        int dayDurationMillis = 24 * 60 * 60 * 1000;

        long nowMillis = Calendar.getInstance().getTimeInMillis();
        long createdAtMillis = createdAt.getTimeInMillis();
        long difMillis = nowMillis - createdAtMillis;

        SimpleDateFormat sdf;
        if (difMillis < dayDurationMillis) {
            sdf = new SimpleDateFormat("kk:mm", Locale.getDefault());
        } else if (difMillis < 2 * dayDurationMillis){
            return context.getString(R.string.yesterday);
        } else {
            sdf = new SimpleDateFormat("d/M/yy", Locale.getDefault());
        }

        return sdf.format(createdAt.getTime());
    }

    public static String createdAtToDetailedString(Calendar createdAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy kk:mm", Locale.getDefault());
        return sdf.format(createdAt.getTime());
    }

    public static String calculateLongDurationText(Context context, ArrayList<Book.BookProcess> bookProcesses) {
        Book.BookProcess lastProcess;

        int i = 0;
        do {
            lastProcess = bookProcesses.get(bookProcesses.size() - 1 - i++);
        } while (lastProcess instanceof Request || lastProcess == null);

        Calendar createdAt = null;
        if (lastProcess instanceof Interaction) {
            createdAt = ((Interaction) lastProcess).getCreatedAt();
        } else if (lastProcess instanceof Transaction) {
            createdAt = ((Transaction) lastProcess).getCreatedAt();
        }

        int dayDiff = calculateDayDiff(createdAt);

        if (dayDiff > 0) {
            return context.getString(R.string.state_duration, dayDiff);
        } else {
            return context.getString(R.string.state_today);
        }
    }

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
