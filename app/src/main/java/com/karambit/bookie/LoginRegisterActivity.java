package com.karambit.bookie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.helper.TypefaceSpan;

public class LoginRegisterActivity extends AppCompatActivity {

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

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mSurnameEditText = (EditText) findViewById(R.id.surnameEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mRePasswordEditText = (EditText) findViewById(R.id.repasswordEditText);

        Button startReadingButton =  (Button) findViewById(R.id.startReadingButton);

        // Bookie font
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.app_name)).setText(s);

        findViewById(R.id.createNewAccountContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mIsLogin){
                    findViewById(R.id.nameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.surnameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.login));
                    ((TextView)findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.already_have_account));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.GONE);

                    mIsLogin = false;

                } else {
                    findViewById(R.id.nameContainer).setVisibility(View.GONE);
                    findViewById(R.id.surnameContainer).setVisibility(View.GONE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.GONE);

                    ((TextView) findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.create_new_account));
                    ((TextView)findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.no_account_yet));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.VISIBLE);

                    mIsLogin = true;
                }
            }
        });

        startReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (areAllInputsValid()) {
                    Toast.makeText(LoginRegisterActivity.this, "OK!", Toast.LENGTH_SHORT).show();
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

        } else if (! Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            ok = false;
            mEmailEditText.setError(getString(R.string.invalid_email_address));
        }

        // Password
        String passwordText = mPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(passwordText)) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.invalid_email_address));

        } else if (passwordText.length() < PASSWORD_LENGTH_MIN) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.password_must_be_longer_than, PASSWORD_LENGTH_MIN - 1));

        } else if (passwordText.length() > PASSWORD_LENGTH_MAX) {
            ok = false;
            mPasswordEditText.setError(getString(R.string.password_must_be_shorter_than, PASSWORD_LENGTH_MAX + 1));
        }

        // Register checks
        if (! mIsLogin) {

            // Name
            if (TextUtils.isEmpty(mNameEditText.getText())) {
                ok = false;
                mNameEditText.setError(getString(R.string.empty_field_message));
            }

            // Surname
            if (TextUtils.isEmpty(mSurnameEditText.getText())) {
                ok = false;
                mSurnameEditText.setError(getString(R.string.empty_field_message));
            }

            // rePassword
            if (! mRePasswordEditText.getText().toString().equals(mPasswordEditText.getText().toString())) {
                mRePasswordEditText.setError(getString(R.string.passwords_must_be_same));
            }
        }



        return ok;
    }
}
