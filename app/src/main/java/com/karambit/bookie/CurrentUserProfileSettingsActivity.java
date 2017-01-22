package com.karambit.bookie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.karambit.bookie.helper.SessionManager;

public class CurrentUserProfileSettingsActivity extends AppCompatActivity {

    public static final int RESULT_USER_LOGOUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_USER_LOGOUT);
                finish();
            }
        });
    }
}
