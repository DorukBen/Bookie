package com.karambit.bookie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.karambit.bookie.helper.TypefaceSpan;

public class LoginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.app_name)).setText(s);

        SpannableString hint = new SpannableString(getResources().getString(R.string.name));
        hint.setSpan(new TypefaceSpan(this, "montserratalternates_extralight.otf"), 0, hint.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.nameEditText)).setHint(hint);

        hint = new SpannableString(getResources().getString(R.string.surname));
        hint.setSpan(new TypefaceSpan(this, "montserratalternates_extralight.otf"), 0, hint.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.surnameEditText)).setHint(hint);

        hint = new SpannableString(getResources().getString(R.string.email));
        hint.setSpan(new TypefaceSpan(this, "montserratalternates_extralight.otf"), 0, hint.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.emailEditText)).setHint(hint);

        hint = new SpannableString(getResources().getString(R.string.password));
        hint.setSpan(new TypefaceSpan(this, "montserratalternates_extralight.otf"), 0, hint.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.passwordEditText)).setHint(hint);

        hint = new SpannableString(getResources().getString(R.string.repassword));
        hint.setSpan(new TypefaceSpan(this, "montserratalternates_extralight.otf"), 0, hint.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)findViewById(R.id.repasswordEditText)).setHint(hint);

        findViewById(R.id.createNewAccountTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(findViewById(R.id.nameContainer).getVisibility() == View.GONE){
                    findViewById(R.id.nameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.surnameContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.VISIBLE);

                    ((TextView)findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.member_login));
                    ((TextView)findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.already_have_account));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.GONE);
                }else{
                    findViewById(R.id.nameContainer).setVisibility(View.GONE);
                    findViewById(R.id.surnameContainer).setVisibility(View.GONE);
                    findViewById(R.id.repasswordContainer).setVisibility(View.GONE);

                    ((TextView)findViewById(R.id.createNewAccountTextView)).setText(getResources().getString(R.string.create_new_account));
                    ((TextView)findViewById(R.id.noAccountYetTextView)).setText(getResources().getString(R.string.no_account_yet));
                    findViewById(R.id.forgotPasswordTextView).setVisibility(View.VISIBLE);
                }

            }
        });
    }
}
