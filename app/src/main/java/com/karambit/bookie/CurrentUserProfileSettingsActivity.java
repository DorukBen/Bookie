package com.karambit.bookie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CurrentUserProfileSettingsActivity extends AppCompatActivity {

    public static final int RESULT_USER_LOGOUT = 1;
    public static final int REQUEST_CODE_CHANGE_PROFILE_PICTURE_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_profile_settings);

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_USER_LOGOUT);
                finish();
            }
        });


    }
}
