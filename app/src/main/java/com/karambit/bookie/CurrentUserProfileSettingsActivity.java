package com.karambit.bookie;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CurrentUserProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = CurrentUserProfileSettingsActivity.class.getSimpleName();

    public static final int RESULT_USER_LOGOUT = 1;
    public static final int RESULT_USER_UPDATED = 2;
    public static final int REQUEST_CODE_CHANGE_PROFILE_PICTURE_ACTIVITY = 0;

    private TextView mLocationTextView;
    private Button mChangeLocationButton;
    private User.Details mCurrentUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_profile_settings);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            SpannableString s = new SpannableString(getString(R.string.settings));
            s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }

        mCurrentUserDetails = SessionManager.getCurrentUserDetails(this);

        mCurrentUserDetails.getUser().setLatitude(new LatLng(37, 41));

        final ScrollView scrollView = (ScrollView) findViewById(R.id.settingsScrollView);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                if (actionBar != null) {
                    actionBar.setElevation(ElevationScrollListener.getActionbarElevation(scrollY));
                }
            }
        });

        EditText usernameEditText = (EditText) findViewById(R.id.userNameEditText);
        usernameEditText.setText(mCurrentUserDetails.getUser().getName());

        EditText bioEditText = (EditText) findViewById(R.id.bioEditText);
        String bio = mCurrentUserDetails.getBio();
        if (!TextUtils.isEmpty(bio) && !bio.equals("null")) {
            bioEditText.setText(bio);
        }

        Button changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPasswordDialog();
            }
        });

        CircleImageView circleImageView = (CircleImageView) findViewById(R.id.profilePictureImageView);

        Glide.with(this)
             .load(mCurrentUserDetails.getUser().getThumbnailUrl())
             .asBitmap()
             .placeholder(R.drawable.placeholder_88dp)
             .error(R.drawable.error_88dp)
             .into(circleImageView);

        findViewById(R.id.profilePictureContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrentUserProfileSettingsActivity.this, PhotoViewerActivity.class);
                startActivity(intent);
            }
        });

        mLocationTextView = (TextView) findViewById(R.id.locationTextView);
        mLocationTextView.setTextColor(ContextCompat.getColor(this, R.color.primaryTextColor));
        mLocationTextView.setText(R.string.loading);

        mChangeLocationButton = (Button) findViewById(R.id.changeLocationButton);
        mChangeLocationButton.setVisibility(View.GONE);
        mChangeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CurrentUserProfileSettingsActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        fetchLocation();

        final Button feedbackSendButton = (Button) findViewById(R.id.feedbackSendButton);
        feedbackSendButton.setVisibility(View.GONE);

        EditText feedbackEditText = (EditText) findViewById(R.id.feedbackEditText);

        feedbackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scrollView.fullScroll(View.FOCUS_DOWN);

                if (!TextUtils.isEmpty(s)) {
                    feedbackSendButton.setVisibility(View.VISIBLE);
                } else {
                    feedbackSendButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CurrentUserProfileSettingsActivity.this)
                    .setMessage(R.string.logout_message)
                    .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_USER_LOGOUT);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
            }
        });
    }

    private void oldPasswordDialog() {

        final AlertDialog oldPasswordDialog = new AlertDialog.Builder(this).create();

        View oldPasswordDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_old_password, null);

        final EditText oldPasswordEditText = (EditText) oldPasswordDialogView.findViewById(R.id.oldPasswordEditText);

        Button ok = (Button) oldPasswordDialogView.findViewById(R.id.oldPasswordOkButton);
        Button cancel = (Button) oldPasswordDialogView.findViewById(R.id.oldPasswordCancelButton);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPasswordEditText.length() < LoginRegisterActivity.PASSWORD_LENGTH_MIN ||
                    oldPasswordEditText.length() > LoginRegisterActivity.PASSWORD_LENGTH_MAX) {

                    oldPasswordEditText.setError(getString(R.string.false_combination));

                } else {

                    // TODO Server and Progress Dialog

                    oldPasswordDialog.dismiss();
                    newPasswordDialog();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPasswordDialog.dismiss();
            }
        });

        oldPasswordDialog.setView(oldPasswordDialogView);
        oldPasswordDialog.show();
    }

    private void newPasswordDialog() {

        final AlertDialog newPasswordDialog = new AlertDialog.Builder(this).create();

        View newPasswordDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_password, null);

        final EditText newPasswordEditText = (EditText) newPasswordDialogView.findViewById(R.id.newPasswordEditText);
        final EditText passwordAgainEditText = (EditText) newPasswordDialogView.findViewById(R.id.passwordAgainEditText);

        Button ok = (Button) newPasswordDialogView.findViewById(R.id.oldPasswordOkButton);
        Button cancel = (Button) newPasswordDialogView.findViewById(R.id.oldPasswordCancelButton);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPasswordEditText.length() < LoginRegisterActivity.PASSWORD_LENGTH_MIN) {
                    newPasswordEditText.setError(getString(R.string.password_must_be_longer_than, LoginRegisterActivity.PASSWORD_LENGTH_MIN - 1));

                } else if (newPasswordEditText.length() > LoginRegisterActivity.PASSWORD_LENGTH_MAX) {
                    newPasswordEditText.setError(getString(R.string.password_must_be_shorter_than, LoginRegisterActivity.PASSWORD_LENGTH_MAX  + 1));

                } else if (!passwordAgainEditText.getText().toString().equals(newPasswordEditText.getText().toString())) {
                    passwordAgainEditText.setError(getString(R.string.passwords_must_be_same));

                } else {

                    // TODO Server and Progress Dialog

                    newPasswordDialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPasswordDialog.dismiss();
            }
        });

        newPasswordDialog.setView(newPasswordDialogView);
        newPasswordDialog.show();
    }

    private void fetchLocation() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                LatLng userLocation = mCurrentUserDetails.getUser().getLocation();

                if (userLocation != null) {

                    try {
                        Geocoder geocoder = new Geocoder(CurrentUserProfileSettingsActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);

                        // Admin area equals Istanbul
                        // Subadmin are equals Bahçelievler

                        if (addresses.size() > 0) {
                            String locationString = "";

                            String subAdminArea = addresses.get(0).getSubAdminArea();
                            if (!TextUtils.isEmpty(subAdminArea) && !subAdminArea.equals("null")) {
                                locationString += subAdminArea;
                            }

                            String adminArea = addresses.get(0).getAdminArea();
                            if (!TextUtils.isEmpty(adminArea) && !adminArea.equals("null")) {
                                locationString +=  " / " + adminArea;
                            }

                            final String finalLocationString = locationString;
                            runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (!TextUtils.isEmpty(finalLocationString)) {
                                            mLocationTextView.setText(finalLocationString);
                                            mChangeLocationButton.setText(R.string.change_location);

                                        } else {
                                            mLocationTextView.setText(R.string.no_location_info);
                                            mChangeLocationButton.setText(R.string.add_location);
                                        }
                                        mChangeLocationButton.setVisibility(View.VISIBLE);
                                        mLocationTextView.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this,
                                                                                              R.color.primaryTextColor));
                                    }
                                }
                            );

                        } else {
                            runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mLocationTextView.setText(R.string.no_location_info);
                                        mChangeLocationButton.setText(R.string.add_location);
                                        mChangeLocationButton.setVisibility(View.VISIBLE);
                                        mLocationTextView.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this,
                                                                                              R.color.primaryTextColor));
                                    }
                                }
                            );
                        }

                    } catch (IOException e) {
                        e.printStackTrace();

                        runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mLocationTextView.setText(R.string.no_location_info);
                                    mChangeLocationButton.setText(R.string.add_location);
                                    mChangeLocationButton.setVisibility(View.VISIBLE);
                                    mLocationTextView.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this,
                                                                                          R.color.primaryTextColor));
                                }
                            }
                        );
                    }
                } else {
                    runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mLocationTextView.setText(R.string.no_location_info);
                                mChangeLocationButton.setText(R.string.add_location);
                                mChangeLocationButton.setVisibility(View.VISIBLE);
                                mLocationTextView.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this,
                                                                                      R.color.primaryTextColor));
                            }
                        }
                    );
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.current_user_settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                finish();
                return true;
            }

            case R.id.action_done: {
                saveChanges();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveChanges() {

        // TODO Server

        setResult(RESULT_USER_UPDATED);

        finish();
    }

    /**
     * @param messageText Input
     * @return Modified Input
     * <p>
     * This method removes unnecessary new lines in string. For example:
     * <p>
     * "1
     * <p>
     * <p>
     * <p>
     * 2
     * 34
     * <p>
     * <p>
     * <p>
     * <p>
     * 5
     * 6
     * <p>
     * <p>
     * <p>
     * <p>
     * 7"
     * <p>
     * RETURNS:
     * "1
     * 2
     * 34
     * 5
     * 6
     * 7"
     */
    private String trimNewLines(String messageText) {

        StringBuilder stringBuilder = new StringBuilder(messageText);

        for (int i = 0; i < stringBuilder.length() - 1; i++) {
            char currentChar = stringBuilder.charAt(i);
            char nextChar = stringBuilder.charAt(i + 1);

            if (currentChar == '\n' && nextChar == '\n') {
                stringBuilder.deleteCharAt(i);
                i--;
            }
        }

        return stringBuilder.toString();
    }
}
