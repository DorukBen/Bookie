package com.karambit.bookie.helper;

import android.content.Context;

import com.karambit.bookie.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
}
