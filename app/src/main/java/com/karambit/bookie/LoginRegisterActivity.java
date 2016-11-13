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
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = LoginRegisterActivity.class.getSimpleName();

    public static final int PASSWORD_LENGTH_MIN = 6;
    public static final int PASSWORD_LENGTH_MAX = 128;

    private boolean mIsLogin = true;

    private EditText mNameEditText;
    private EditText mSurnameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mRePasswordEditText;

    private DBHandler mDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mDBHandler = new DBHandler(this);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mSurnameEditText = (EditText) findViewById(R.id.surnameEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mRePasswordEditText = (EditText) findViewById(R.id.repasswordEditText);

        Button startReadingButton = (Button) findViewById(R.id.startReadingButton);


        // Bookie font
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
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

                if (!NetworkChecker.isNetworkAvailable(LoginRegisterActivity.this)) {
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
        String emailText = mEmailEditText.getText().toString();

        if (TextUtils.isEmpty(emailText)) {
            ok = false;
            mEmailEditText.setError(getString(R.string.empty_field_message));

        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            ok = false;
            mEmailEditText.setError(getString(R.string.invalid_email_address));
        }

        // Password
        String passwordText = mPasswordEditText.getText().toString();

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
            mNameEditText.setText(mNameEditText.getText().toString().trim());

            if (TextUtils.isEmpty(mNameEditText.getText())) {
                ok = false;
                mNameEditText.setError(getString(R.string.empty_field_message));
            }

            // Surname
            mSurnameEditText.setText(mSurnameEditText.getText().toString().trim());

            if (TextUtils.isEmpty(mSurnameEditText.getText())) {
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
        String nameSurname = mNameEditText.getText().toString() + " " + mSurnameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        Call<ResponseBody> register = userApi.register(nameSurname, email, password);

        register.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();

                try {
                    String json = response.body().string();

                    JSONObject responseObject = new JSONObject(json);
                    boolean error = responseObject.getBoolean("error");

                    if (!error) {

                        JSONObject userObject = responseObject.getJSONObject("user");
                        User.Details userDetails = User.jsonObjectToUserDetails(userObject);

                        mDBHandler.insertCurrentUser(userDetails);
                        SessionManager.login(LoginRegisterActivity.this);

                        startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));

                        Log.i(TAG, "Registered!");

                    } else {

                        int errorCode = responseObject.getInt("error_code");

                        if (errorCode == ErrorCodes.EMAIL_TAKEN) {
                            mEmailEditText.setError(getString(R.string.email_taken));
                        } else {
                            Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onResponse: errorCode = " + errorCode);
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }
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

        UserApi userApi = BookieClient.getClient().create(UserApi.class);
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        Call<ResponseBody> login = userApi.login(email, password);

        login.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();

                try {
                    String json = response.body().string();

                    JSONObject responseObject = new JSONObject(json);
                    boolean error = responseObject.getBoolean("error");

                    if (!error) {
                        User.Details userDetails = User.jsonObjectToUserDetails(responseObject.getJSONObject("user"));

                        mDBHandler.insertCurrentUser(userDetails);
                        SessionManager.login(LoginRegisterActivity.this);

                        startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));

                        Log.i(TAG, "Logged in!");

                    } else {

                        int errorCode = responseObject.getInt("error_code");

                        switch (errorCode) {

                            case ErrorCodes.EMAIL_NOT_FOUND:
                                mEmailEditText.setError(getString(R.string.email_not_found, getString(R.string.app_name)));
                                break;

                            case ErrorCodes.FALSE_COMBINATION:
                                mPasswordEditText.setError(getString(R.string.false_combination));
                                break;

                            default:
                                Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onResponse: errorCode = " + errorCode);
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(LoginRegisterActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }
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
