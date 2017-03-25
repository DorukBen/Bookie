package com.karambit.bookie;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.karambit.bookie.helper.SessionManager;

/**
 * Created by doruk on 16.02.2017.
 */

public class BookieApplication extends Application {

    private static BookieApplication instance;

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;
    }

    public static BookieApplication getInstance ()
    {
        return instance;
    }

    public static boolean hasNetwork ()
    {
        return instance.checkIfHasNetwork();
    }

    public boolean checkIfHasNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
