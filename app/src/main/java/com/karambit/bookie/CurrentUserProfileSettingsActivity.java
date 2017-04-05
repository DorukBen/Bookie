package com.karambit.bookie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentUserProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = CurrentUserProfileSettingsActivity.class.getSimpleName();

    public static final int RESULT_USER_LOGOUT = 1;

    private TextView mLocationTextView;
    private Button mChangeLocationButton;
    private User.Details mCurrentUserDetails;
    private EditText mUsernameEditText;
    private EditText mBioEditText;
    private ScrollView mScrollView;

    private DBManager mDbManager;
    private BroadcastReceiver mMessageReceiver;
    private CircleImageView mProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_profile_settings);

        mScrollView = (ScrollView) findViewById(R.id.settingsScrollView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            actionBar.setElevation(elevation);
            SpannableString s = new SpannableString(getString(R.string.settings));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
            s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            toolbar.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BookieApplication.hasNetwork()) {
                        saveChanges();
                    } else {
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            toolbar.findViewById(R.id.infoButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CurrentUserProfileSettingsActivity.this, InfoActivity.class));
                }
            });
        }

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = mScrollView.getScrollY();
                if (actionBar != null) {
                    actionBar.setElevation(ElevationScrollListener.getActionbarElevation(scrollY));
                }
            }
        });

        mDbManager = new DBManager(this);
        mDbManager.open();

        if (BookieApplication.hasNetwork()) {

            mCurrentUserDetails = SessionManager.getCurrentUserDetails(this);

            mUsernameEditText = (EditText) findViewById(R.id.userNameEditText);
            mUsernameEditText.setText(mCurrentUserDetails.getUser().getName());

            mBioEditText = (EditText) findViewById(R.id.bioEditText);
            String bio = mCurrentUserDetails.getBio();
            if (!TextUtils.isEmpty(bio)) {
                mBioEditText.setText(bio);
            }

            Button changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
            changePasswordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldPasswordDialog();
                }
            });

            mProfilePicture = (CircleImageView) findViewById(R.id.profilePictureImageView);

            Glide.with(this)
                 .load(mCurrentUserDetails.getUser().getThumbnailUrl())
                 .asBitmap()
                 .placeholder(R.drawable.placeholder_88dp)
                 .error(R.drawable.error_88dp)
                 .into(mProfilePicture);

            findViewById(R.id.profilePictureContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CurrentUserProfileSettingsActivity.this, PhotoViewerActivity.class);
                    intent.putExtra(PhotoViewerActivity.EXTRA_USER, mCurrentUserDetails.getUser());
                    intent.putExtra(PhotoViewerActivity.EXTRA_IMAGE, mCurrentUserDetails.getUser().getImageUrl());
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

            Button lovedGenresButton = (Button) findViewById(R.id.lovedGenresButton);
            lovedGenresButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CurrentUserProfileSettingsActivity.this, LovedGenresActivity.class));
                }
            });

            View verificationContainer = findViewById(R.id.verificationContainer);

            if (!mCurrentUserDetails.isVerified()) {
                verificationContainer.setVisibility(View.VISIBLE);

                final ImageView verificationImageView = (ImageView) findViewById(R.id.resendVerificationCodeImageView);
                final TextView verificationInfoTextView = (TextView) findViewById(R.id.verificationInfoTextView);

                final Button resendVerificationCodeButton = (Button) findViewById(R.id.resendVerificationCodeButton);
                resendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        resendVerificationCode(resendVerificationCodeButton, verificationImageView, verificationInfoTextView);
                    }
                });
            } else {
                verificationContainer.setVisibility(View.GONE);
            }

            final Button feedbackSendButton = (Button) findViewById(R.id.feedbackSendButton);
            feedbackSendButton.setClickable(false);
            feedbackSendButton.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this, R.color.secondaryTextColor));

            final EditText feedbackEditText = (EditText) findViewById(R.id.feedbackEditText);

            feedbackEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mScrollView.fullScroll(View.FOCUS_DOWN);

                    if (!TextUtils.isEmpty(s)) {
                        feedbackSendButton.setClickable(true);
                        feedbackSendButton.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this, R.color.colorAccent));
                    } else {
                        feedbackSendButton.setClickable(false);
                        feedbackSendButton.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this, R.color.secondaryTextColor));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            feedbackSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(CurrentUserProfileSettingsActivity.this)
                        .setMessage(R.string.feedback_prompt)
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                sendFeedback();

                                feedbackEditText.clearFocus();
                                feedbackEditText.setText("");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                }
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
        } else {

            mScrollView.setVisibility(View.GONE);

            View noConnectionView = findViewById(R.id.noConnectionView);
            noConnectionView.setVisibility(View.VISIBLE);
            ((TextView) noConnectionView.findViewById(R.id.emptyStateTextView)).setText(R.string.no_internet_connection);
        }

        mMessageReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_USER_VERIFIED)){
                    View verificationButtonContainer = findViewById(R.id.verificationButtonContainer);
                    verificationButtonContainer.setVisibility(View.GONE);

                    TextView verificationInfoTextView = (TextView) findViewById(R.id.verificationInfoTextView);
                    verificationInfoTextView.setText(R.string.verification_accepted_info);

                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_PROFILE_PICTURE_CHANGED)) {

                    String profilePictureUrl = intent.getStringExtra(BookieIntentFilters.EXTRA_PROFILE_PICTURE_URL);
                    String thumbnailUrl = intent.getStringExtra(BookieIntentFilters.EXTRA_PROFILE_THUMBNAIL_URL);

                    if (profilePictureUrl != null && thumbnailUrl != null) {

                        mCurrentUserDetails.getUser().setImageUrl(profilePictureUrl);
                        mCurrentUserDetails.getUser().setThumbnailUrl(thumbnailUrl);

                        Logger.d("Profile picture changed received from Local Broadcast: \n" +
                                     "Profile Picture URL: " + profilePictureUrl + "\nThumbnail URL: " + thumbnailUrl);

                        Glide.with(context)
                             .load(mCurrentUserDetails.getUser().getThumbnailUrl())
                             .asBitmap()
                             .placeholder(R.drawable.placeholder_88dp)
                             .error(R.drawable.error_88dp)
                             .into(mProfilePicture);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_LOCATION_UPDATED)) {
                    LatLng location = intent.getParcelableExtra(BookieIntentFilters.EXTRA_LOCATION);
                    if (location != null) {
                        mCurrentUserDetails.getUser().setLocation(location);
                        fetchLocation();
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_USER_VERIFIED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_PROFILE_PICTURE_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_LOCATION_UPDATED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void resendVerificationCode(Button resendVerificationCodeButton, ImageView verificationImageView, TextView verificationInfoTextView) {

        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        progressDialog.show();

        resendVerificationCode(resendVerificationCodeButton, verificationImageView, verificationInfoTextView, progressDialog);
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
                    ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(CurrentUserProfileSettingsActivity.this);
                    progressDialog.setMessage(R.string.please_wait);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    isPasswordCorrect(oldPasswordEditText.getText().toString(), progressDialog, oldPasswordDialog);
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

    private void isPasswordCorrect(String givenPassword, final ComfortableProgressDialog progressDialog, final AlertDialog oldPasswordDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = mCurrentUserDetails.getEmail();
        String password = mCurrentUserDetails.getPassword();
        Call<ResponseBody> isPasswordCorrect = userApi.isPasswordCorrect(email, password, givenPassword);

        final EditText oldPasswordEditText = (EditText) oldPasswordDialog.findViewById(R.id.oldPasswordEditText);

        Logger.d("isPasswordCorrect() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tgivenPassword=" + givenPassword);


        isPasswordCorrect.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                                Logger.d("Password is correct");

                                if (!responseObject.isNull("isValid")){
                                    if (responseObject.getBoolean("isValid")){
                                        oldPasswordDialog.dismiss();
                                        newPasswordDialog();
                                    }else {
                                        if (oldPasswordEditText != null) {
                                            oldPasswordEditText.setError(getString(R.string.false_combination));
                                        }
                                    }
                                }
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("isPasswordCorrect Failure: " + t.getMessage());
            }
        });
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
                    ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(CurrentUserProfileSettingsActivity.this);
                    progressDialog.setMessage(R.string.please_wait);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    uploadNewPassword(newPasswordEditText.getText().toString(), progressDialog, newPasswordDialog);
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

    private void uploadNewPassword(String newPassword, final ComfortableProgressDialog progressDialog, final AlertDialog newPasswordDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = mCurrentUserDetails.getEmail();
        String password = mCurrentUserDetails.getPassword();
        Call<ResponseBody> uploadNewPassword = userApi.uploadNewPassword(email, password, newPassword);

        Logger.d("uploadNewPassword() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tnewPassword=" + newPassword);

        uploadNewPassword.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("newPassword")){

                                    Logger.d("Password changed successfully");

                                    newPasswordDialog.dismiss();
                                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();

                                    mDbManager.Threaded(mDbManager.getUserDataSource().cUpdateUserPassword(responseObject.getString("newPassword")));
                                    SessionManager.getCurrentUserDetails(CurrentUserProfileSettingsActivity.this).setPassword(responseObject.getString("newPassword"));
                                }
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("uploadNewPassword Failure: " + t.getMessage());
            }
        });
    }

    private void fetchLocation() {

        if (TextUtils.isEmpty(SessionManager.getLocationText())) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    LatLng userLocation = mCurrentUserDetails.getUser().getLocation();

                    if (userLocation != null) {

                        try {
                            Geocoder geocoder = new Geocoder(CurrentUserProfileSettingsActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);

                            // Admin area equals Istanbul
                            // Subadmin area equals Bahçelievler

                            if (addresses.size() > 0) {
                                String locationString = "";

                                String subAdminArea = addresses.get(0).getSubAdminArea();
                                if (!TextUtils.isEmpty(subAdminArea) && !subAdminArea.equals("null")) {
                                    locationString += subAdminArea + " / ";
                                }

                                String adminArea = addresses.get(0).getAdminArea();
                                if (!TextUtils.isEmpty(adminArea) && !adminArea.equals("null")) {
                                    locationString += adminArea;
                                }

                                final String finalLocationString = locationString;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!TextUtils.isEmpty(finalLocationString)) {
                                            SessionManager.setLocationText(finalLocationString);
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

        } else {
            mLocationTextView.setText(SessionManager.getLocationText());
            mChangeLocationButton.setText(R.string.change_location);
            mChangeLocationButton.setVisibility(View.VISIBLE);
            mLocationTextView.setTextColor(ContextCompat.getColor(CurrentUserProfileSettingsActivity.this,
                                                                  R.color.primaryTextColor));
        }
    }

    private void resendVerificationCode(final Button resendVerificationCodeButton, final ImageView verificationImageView, final TextView verificationInfoTextView, final ComfortableProgressDialog comfortableProgressDialog){
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();

        Call<ResponseBody> resendVerificationCode = userApi.resendEmailVerificationCode(email, password);

        Logger.d("resendEmailVerificationCode() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password);

        resendVerificationCode.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                resendVerificationCodeButton.setClickable(false);
                                resendVerificationCodeButton.setText(R.string.verification_code_sent);
                                verificationImageView.setImageResource(R.drawable.ic_done_white_24dp);
                                verificationInfoTextView.setText(R.string.verification_sent_info);
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.USER_ALREADY_VERIFIED){
                                    Logger.e("User already verified. (Current User Profile Settings Error)");
                                    //TODO When user already verified
                                }else {
                                    Logger.e("Error true in response: errorCode = " + errorCode);
                                }
                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Profile Settings Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Profile Settings Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }


                comfortableProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("resendEmailVerificationCode Failure: " + t.getMessage());
                comfortableProgressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String bioLabel = trimNewLines(mBioEditText.getText().toString());
        String nameLabel = mUsernameEditText.getText().toString();

        if (TextUtils.isEmpty(nameLabel)) {
            mUsernameEditText.setError(getString(R.string.empty_field_message));
        } else {

            uploadUserParamsToServer(nameLabel, bioLabel, mCurrentUserDetails.getUser().getLocation(), progressDialog);
            SessionManager.setLocationText("");
        }
    }

    private void uploadUserParamsToServer(String name, String bio, final LatLng location, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = mCurrentUserDetails.getEmail();
        String password = mCurrentUserDetails.getPassword();

        name = upperCaseString(name);

        Call<ResponseBody> uploadUserDetails;

        uploadUserDetails = userApi.updateUserDetails(email, password, name, bio);

        Logger.d("updateUserDetails() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tname=" + name + ", \n\tbio=" + bio);

        final String finalBio = bio;
        final String finalName = name;
        uploadUserDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Intent intent = new Intent(BookieIntentFilters.INTENT_FILTER_PROFILE_PREFERENCES_CHANGED);
                                intent.putExtra(BookieIntentFilters.EXTRA_NAME_SURNAME, finalName);
                                intent.putExtra(BookieIntentFilters.EXTRA_BIO, finalBio);

                                LocalBroadcastManager.getInstance(CurrentUserProfileSettingsActivity.this).sendBroadcast(intent);

                                finish();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Profile Settings Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Profile Settings Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("uploadUserDetails Failure: " + t.getMessage());
            }
        });
    }

    private void sendFeedback() {
        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        progressDialog.show();

        EditText feedBackEditText = (EditText) findViewById(R.id.feedbackEditText);
        uploadFeedBackToServer(feedBackEditText.getText().toString(), progressDialog);
    }

    private void uploadFeedBackToServer(String feedBack, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = mCurrentUserDetails.getEmail();
        String password = mCurrentUserDetails.getPassword();
        Call<ResponseBody> uploadFeedBack = userApi.uploadFeedBack(email, password, feedBack);

        Logger.d("uploadFeedBack() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tfeedback=" + feedBack);

        uploadFeedBack.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Logger.d("Feedback uploaded successfully");
                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.thanks_for_feedback), Toast.LENGTH_SHORT).show();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Current User Profile Settings Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Current User Profile Settings Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("uploadFeedBack Failure: " + t.getMessage());
            }
        });
    }

    private String upperCaseString(String input){
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * @param text Input
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
    private String trimNewLines(String text) {

        StringBuilder stringBuilder = new StringBuilder(text);

        for (int i = 0; i < stringBuilder.length() - 1; i++) {
            char currentChar = stringBuilder.charAt(i);
            char nextChar = stringBuilder.charAt(i + 1);

            if (currentChar == '\n' && nextChar == '\n') {
                stringBuilder.deleteCharAt(i);
                i--;
            }
        }

        return stringBuilder.toString().trim();
    }
}
