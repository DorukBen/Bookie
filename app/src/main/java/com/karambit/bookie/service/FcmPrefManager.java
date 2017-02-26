package com.karambit.bookie.service;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by doruk on 24.02.2017.
 */

public class FcmPrefManager {
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;
    Context mContext;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "fcm_prefs";

    private static final String IS_UPLOADED_TO_SERVER = "IsUploadedToServer";
    private static final String FCM_TOKEN = "FcmToken";

    public FcmPrefManager(Context context) {
        this.mContext = context;
        mPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPref.edit();

    }

    public void setUploadedToServer(boolean isUploaded) {
        mEditor.putBoolean(IS_UPLOADED_TO_SERVER, isUploaded);
        mEditor.commit();
    }

    public boolean isUploadedToServer() {
        return mPref.getBoolean(IS_UPLOADED_TO_SERVER, false);
    }

    public void setFcmToken(String fcmToken){
        mEditor.putString(FCM_TOKEN, fcmToken);
        mEditor.commit();

        setUploadedToServer(false);
    }

    public String getFcmToken(){
        return mPref.getString(FCM_TOKEN, "");
    }
}
