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
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private static final int REQUEST_CODE_UPDATE_PROFILE_PICTURE = 1;
    private static final int REQUEST_CODE_LOCATION = 2;

    public static final int RESULT_USER_LOGOUT = 1;
    public static final int RESULT_USER_UPDATED = 2;

    private TextView mLocationTextView;
    private Button mChangeLocationButton;
    private User.Details mCurrentUserDetails;
    private EditText mUsernameEditText;
    private EditText mBioEditText;
    private ScrollView mScrollView;

    private DBManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_profile_settings);

        mScrollView = (ScrollView) findViewById(R.id.settingsScrollView);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            actionBar.setElevation(elevation);
            SpannableString s = new SpannableString(getString(R.string.settings));
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
            s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            actionBar.setTitle(s);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_primary_text_color);
        }

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = mScrollView.getScrollY();
                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(scrollY));
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
                    intent.putExtra(PhotoViewerActivity.EXTRA_USER, mCurrentUserDetails.getUser());
                    intent.putExtra(PhotoViewerActivity.EXTRA_IMAGE, mCurrentUserDetails.getUser().getImageUrl());
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_PICTURE);
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
                    startActivityForResult(intent, REQUEST_CODE_LOCATION);
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
    }

    private void resendVerificationCode(Button resendVerificationCodeButton, ImageView verificationImageView, TextView verificationInfoTextView) {

        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        progressDialog.show();


        // TODO resend verification code server
        resendVerificationCodeButton.setClickable(false);
        resendVerificationCodeButton.setText(R.string.verification_code_sent);
        verificationImageView.setImageResource(R.drawable.ic_done_white_24dp);
        verificationInfoTextView.setText(R.string.verification_sent_info);
        progressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPDATE_PROFILE_PICTURE) {
            if (resultCode == PhotoViewerActivity.RESULT_PROFILE_PICTURE_UPDATED) {
                setResult(RESULT_USER_UPDATED);
                finish();
            }
        } else if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == LocationActivity.RESULT_LOCATION_UPDATED) {
                LatLng previousLocation = mCurrentUserDetails.getUser().getLocation();
                double newLatitude = data.getDoubleExtra(LocationActivity.EXTRA_LATITUDE, previousLocation != null ? previousLocation.latitude : Long.MIN_VALUE);
                double newLongitude = data.getDoubleExtra(LocationActivity.EXTRA_LONGITUDE, previousLocation != null ? previousLocation.longitude : Long.MIN_VALUE);
                mCurrentUserDetails.getUser().setLocation(new LatLng(newLatitude, newLongitude));
                fetchLocation();
            }
        }
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
        Call<ResponseBody> isPasswordValid = userApi.isPasswordCorrect(email, password, givenPassword);

        final EditText oldPasswordEditText = (EditText) oldPasswordDialog.findViewById(R.id.oldPasswordEditText);

        isPasswordValid.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("isValid")){
                                    if (responseObject.getBoolean("isValid")){
                                        oldPasswordDialog.dismiss();
                                        newPasswordDialog();
                                    }else {
                                        oldPasswordEditText.setError(getString(R.string.false_combination));
                                    }
                                }
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Current User Settings onFailure: " + t.getMessage());
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


        uploadNewPassword.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("newPassword")){
                                    newPasswordDialog.dismiss();
                                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();

                                    mDbManager.getUserDataSource().updateUserPassword(responseObject.getString("newPassword"));
                                    SessionManager.getCurrentUserDetails(CurrentUserProfileSettingsActivity.this).setPassword(responseObject.getString("newPassword"));

                                }

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Current User Settings onFailure: " + t.getMessage());
            }
        });
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
                                locationString += subAdminArea + " / ";
                            }

                            String adminArea = addresses.get(0).getAdminArea();
                            if (!TextUtils.isEmpty(adminArea) && !adminArea.equals("null")) {
                                locationString +=  adminArea;
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
        if (BookieApplication.hasNetwork()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.done_menu, menu);
            return super.onCreateOptionsMenu(menu);
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                finish();
                return true;
            }

            case R.id.action_done: {
                if (BookieApplication.hasNetwork()) {
                    saveChanges();
                } else {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void uploadUserParamsToServer(String name, String bio, LatLng location, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = mCurrentUserDetails.getEmail();
        String password = mCurrentUserDetails.getPassword();

        if (TextUtils.isEmpty(bio)){
            bio = null;
        }

        Call<ResponseBody> uploadUserDetails;
        if (location != null){
            uploadUserDetails = userApi.updateUserDetails(email, password, name, bio, location.latitude, location.longitude);
        }else{
            uploadUserDetails = userApi.updateUserDetails(email, password, name, bio);
        }

        uploadUserDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                setResult(RESULT_USER_UPDATED);
                                finish();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Current User Settings onFailure: " + t.getMessage());
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

        uploadFeedBack.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.thanks_for_feedback), Toast.LENGTH_SHORT).show();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Current User Settings Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Current User Settings Page Error)");
                            Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Current User Settings Page Error)");
                        Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CurrentUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Current User Settings onFailure: " + t.getMessage());
            }
        });
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
