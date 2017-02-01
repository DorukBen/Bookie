package com.karambit.bookie.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;

/**
 * Created by orcan on 11/12/16.
 */

public class NetworkChecker {

    private static final String TAG = NetworkChecker.class.getSimpleName();

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isInternetAvaible = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (isInternetAvaible){
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
                return !ipAddr.equals("");

            } catch (Exception e) {
                return false;
            }
        }else {
            return false;
        }

    }

}
