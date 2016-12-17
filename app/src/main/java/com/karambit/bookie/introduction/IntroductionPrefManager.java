package com.karambit.bookie.introduction;

import android.content.Context;
import android.content.SharedPreferences;

public class IntroductionPrefManager {
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;
    Context mContext;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "introduction_prefs";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public IntroductionPrefManager(Context context) {
        this.mContext = context;
        mPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPref.edit();

    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        mEditor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        mEditor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return mPref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

}