package com.karambit.bookie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.introduction.IntroductionActivity;
import com.karambit.bookie.introduction.IntroductionPrefManager;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = LoginRegisterActivity.class.getSimpleName();

    public static final int RESULT_LOGGED_IN = 1;

    public static final int PASSWORD_LENGTH_MIN = 6;
    public static final int PASSWORD_LENGTH_MAX = 128;

    private boolean mIsLogin = true;

    private EditText mNameEditText;
    private EditText mSurnameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mRePasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        if (new IntroductionPrefManager(this).isFirstTimeLaunch()) {
            startActivity(new Intent(this, IntroductionActivity.class));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mSurnameEditText = (EditText) findViewById(R.id.surnameEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mRePasswordEditText = (EditText) findViewById(R.id.repasswordEditText);

        Button startReadingButton = (Button) findViewById(R.id.startReadingButton);


        // BookieApplication font
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_APP_NAME_TITLE), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView) findViewById(R.id.app_name)).setText(s);


        // Switch UI process
        findViewById(R.id.createNewAccountContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mIsLogin) {
                    findViewById(R.id.nameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.surnameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.login));
                    ((TextView) findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.already_have_account));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.GONE);

                    mIsLogin = false;

                } else {
                    findViewById(R.id.nameContainer).setVisibility(View.GONE);
                    findViewById(R.id.surnameContainer).setVisibility(View.GONE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.GONE);

                    ((TextView) findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.create_new_account));
                    ((TextView) findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.no_account_yet));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.VISIBLE);

                    mIsLogin = true;
                }
            }
        });


        startReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!BookieApplication.hasNetwork()) {
                    Toast.makeText(LoginRegisterActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                } else if (areAllInputsValid()) {

                    if (mIsLogin) {
                        attemptLogin();
                    } else {
                        attemptRegister();
                    }
                }
            }
        });
    }

    private boolean areAllInputsValid() {

        boolean ok = true;

        // Email
        String emailText = mEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(emailText)) {
            ok = false;
            mEmailEditText.setError(getString(R.string.empty_field_message));

        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            ok = false;
            mEmailEditText.setError(getString(R.string.invalid_email_address));
        }

        // Password
        String passwordText = mPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(passwordText)) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.empty_field_message));

        } else if (passwordText.length() < PASSWORD_LENGTH_MIN) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.password_must_be_longer_than, PASSWORD_LENGTH_MIN - 1));

        } else if (passwordText.length() > PASSWORD_LENGTH_MAX) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.password_must_be_shorter_than, PASSWORD_LENGTH_MAX + 1));
        }

        // Register checks
        if (!mIsLogin) {

            // Name
            String name = mNameEditText.getText().toString();

            mNameEditText.setText(name.trim());

            if (TextUtils.isEmpty(name)) {
                ok = false;
                mNameEditText.setError(getString(R.string.empty_field_message));
            }

            // Surname
            String surname = mSurnameEditText.getText().toString();

            mSurnameEditText.setText(surname.trim());

            if (TextUtils.isEmpty(surname)) {
                ok = false;
                mSurnameEditText.setError(getString(R.string.empty_field_message));
            }

            // rePassword
            if (!mRePasswordEditText.getText().toString().equals(mPasswordEditText.getText().toString())) {
                ok = false;
                mRePasswordEditText.setError(getString(R.string.passwords_must_be_same));
            }
        }

        return ok;
    }


    private void attemptRegister() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        UserApi userApi = BookieClient.getClient().create(UserApi.class);
        String nameSurname = mNameEditText.getText().toString().trim() + " " + mSurnameEditText.getText().toString().trim();
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        Call<ResponseBody> register = userApi.register(email, password, nameSurname);

        register.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                try {
                    if (response != null && response.body() != null) {

                        String json = response.body().string();

                        JSONObject responseObject = new JSONObject(json);
                        boolean error = responseObject.getBoolean("error");

                        if (!error) {

                            JSONObject userObject = responseObject.getJSONObject("userLoginModel");

                            User.Details userDetails = User.jsonObjectToUserDetails(userObject);

                            SessionManager.login(LoginRegisterActivity.this, userDetails);

                            if (userDetails != null) {
                                Log.i(TAG, userDetails.getUser().getName() + " Registered!");
                            } else {
                                Log.e(TAG, "Error occured while registering new user.");
                            }


                            setResult(RESULT_LOGGED_IN);
                            finish();

                        } else {
                            int errorCode = responseObject.getInt("errorCode");

                            if (errorCode == ErrorCodes.EMPTY_POST) {
                                Log.e(TAG, "Post is empty. (Register Error)");
                            } else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT) {
                                Log.e(TAG, "Post element missing. (Register Error)");
                            } else if (errorCode == ErrorCodes.SHORT_PASSWORD) {
                                Log.w(TAG, "Short Password. (Register Warning)");
                                mPasswordEditText.setError(getString(R.string.short_password));
                            } else if (errorCode == ErrorCodes.LONG_PASSWORD) {
                                Log.w(TAG, "Long Password. (Register Warning)");
                                mPasswordEditText.setError(getString(R.string.long_password));
                            } else if (errorCode == ErrorCodes.INVALID_EMAIL) {
                                Log.w(TAG, "Invalid Email. (Register Warning)");
                                mEmailEditText.setError(getString(R.string.invalid_email_address));
                            } else if (errorCode == ErrorCodes.INVALID_NAME_SURNAME) {
                                Log.w(TAG, "Invalid Name Surname. (Register Warning)");
                                mNameEditText.setError(getString(R.string.invalid_name_surname));
                                mSurnameEditText.setError(getString(R.string.invalid_name_surname));
                            } else if (errorCode == ErrorCodes.EMAIL_TAKEN) {
                                mEmailEditText.setError(getString(R.string.email_taken));
                            } else if (errorCode == ErrorCodes.UNKNOWN) {

                                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onResponse: errorCode = " + errorCode);
                            }
                        }

                    } else {
                        Log.e(TAG, "Response body is null. (Register Error)");
                        Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();


                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();

                Log.e(TAG, "Register onFailure: " + t.getMessage());

                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        final UserApi userApi = BookieClient.getClient().create(UserApi.class);
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        Call<ResponseBody> login = userApi.login(email, password);

        login.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                try {
                    if (response != null && response.body() != null) {

                        String json = response.body().string();

                        JSONObject responseObject = new JSONObject(json);
                        boolean error = responseObject.getBoolean("error");

                        if (!error) {
                            if (!responseObject.isNull("userLoginModel")) {
                                User.Details userDetails = User.jsonObjectToUserDetails(responseObject.getJSONObject("userLoginModel"));

                                SessionManager.login(LoginRegisterActivity.this, userDetails);

                                if (!responseObject.isNull("lovedGenres")) {
                                    JSONArray lovedGenresJsonArray = responseObject.getJSONArray("lovedGenres");
                                    if (lovedGenresJsonArray != null) {
                                        Integer[] lovedGenres = new Integer[lovedGenresJsonArray.length()];

                                        for (int i = 0; i < lovedGenresJsonArray.length(); ++i) {
                                            lovedGenres[i] = lovedGenresJsonArray.optInt(i);
                                        }

                                        if (userDetails != null) {
                                            DBHandler.getInstance(LoginRegisterActivity.this).insertLovedGenres(userDetails.getUser(), lovedGenres);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "lovedGenres is empty. (Login Error)");
                                    Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                                }

                                if (userDetails != null) {
                                    Log.i(TAG, userDetails.getUser().getName() + " Logged in!");
                                } else {
                                    Log.e(TAG, "Error occured while Login.");
                                }

                                setResult(RESULT_LOGGED_IN);
                                finish();
                            } else {
                                Log.e(TAG, "userLoginModel is empty. (Login Error)");
                                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            int errorCode = responseObject.getInt("errorCode");

                            if (errorCode == ErrorCodes.EMPTY_POST) {
                                Log.e(TAG, "Post is empty. (Login Error)");
                            } else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT) {
                                Log.e(TAG, "Post element missing. (Login Error)");
                            } else if (errorCode == ErrorCodes.FALSE_COMBINATION) {
                                Log.w(TAG, "False combination. (Login Warning)");
                                mPasswordEditText.setError(getString(R.string.false_combination));
                            } else if (errorCode == ErrorCodes.UNKNOWN) {
                                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onResponse: errorCode = " + errorCode);
                            }
                        }
                    } else {
                        Log.e(TAG, "Response body is null. (Login Error)");
                        Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();

                Log.e(TAG, "Login onFailure: " + t.getMessage());

                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
