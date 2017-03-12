package com.karambit.bookie;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class OtherUserProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = OtherUserProfileSettingsActivity.class.getSimpleName();

    public static final int RESULT_USER_BLOCKED = 666;

    public static final int REPORT_USER_WRONG_NAME = 0;
    public static final int REPORT_USER_WRONG_LOCATION = 1;
    public static final int REPORT_USER_INAPPROPRIATE_CONTENT = 2;
    public static final int REPORT_USER_MESSAGE_SPAM = 3;
    public static final int REPORT_USER_NOT_BOOK_GIVER = 4;
    public static final int REPORT_USER_OTHER = 5;

    private User mUser;
    private EditText mReportEditText;
    private Button mSendReportButton;
    private RadioGroup mReportRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile_settings);

        mUser = getIntent().getParcelableExtra("user");

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(getResources().getDimension(R.dimen.actionbar_starting_elevation));
            SpannableString s = new SpannableString(mUser.getName());
            s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
            s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_messaging_cancel_selection);
        }

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

        if (NetworkChecker.isNetworkAvailable(this)) {

            mSendReportButton = (Button) findViewById(R.id.reportSendButton);

            mReportRadioGroup = (RadioGroup) findViewById(R.id.reportUserRadioGroup);

            mReportEditText = (EditText) findViewById(R.id.reportUserEditText);

            mReportEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean focused) {
                    if (focused) {
                        if (mReportRadioGroup.getCheckedRadioButtonId() == -1) {
                            mReportRadioGroup.check(R.id.reportUserOther);
                        }
                    } else {
                        if (mReportEditText.length() == 0) {
                            mReportRadioGroup.clearCheck();
                            mSendReportButton.setClickable(false);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.secondaryTextColor));
                        }
                    }
                }
            });

            mReportEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0 || mReportRadioGroup.getCheckedRadioButtonId() != R.id.reportUserOther) {
                        mSendReportButton.setClickable(true);
                        mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));
                    } else {
                        mSendReportButton.setClickable(false);
                        mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.secondaryTextColor));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            mReportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    switch (id) {
                        case R.id.reportUserWrongName: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_user_additional_info));

                            break;
                        }

                        case R.id.reportUserWrongLocation: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_user_additional_info));

                            break;
                        }

                        case R.id.reportUserInappropriateContent: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_user_additional_info));

                            break;
                        }

                        case R.id.reportUserMessageSpam: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_user_additional_info));

                            break;
                        }

                        case R.id.reportUserNotBookGiver: {

                            mSendReportButton.setClickable(true);
                            mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));

                            mReportEditText.setHint(getString(R.string.report_hint_user_additional_info));

                            break;
                        }

                        case R.id.reportUserOther: {

                            if (mReportEditText.length() > 0) {
                                mSendReportButton.setClickable(true);
                                mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.colorAccent));
                            } else {
                                mSendReportButton.setClickable(false);
                                mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.secondaryTextColor));
                            }

                            mReportEditText.setHint(getString(R.string.report_hint_user_other, mUser.getName()));

                            mReportEditText.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mReportEditText, InputMethodManager.SHOW_IMPLICIT);

                            break;
                        }
                    }
                }
            });

            mSendReportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(OtherUserProfileSettingsActivity.this)
                        .setMessage(getString(R.string.send_report_prompt, mUser.getName()))
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                mReportEditText.setText("");

                                sendReport();

                                mReportRadioGroup.clearCheck();
                                mSendReportButton.setClickable(false);
                                mSendReportButton.setTextColor(ContextCompat.getColor(OtherUserProfileSettingsActivity.this, R.color.secondaryTextColor));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                }
            });

            Button blockButton = (Button) findViewById(R.id.blockButton);
            blockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(OtherUserProfileSettingsActivity.this)
                        .setMessage(getString(R.string.block_prompt, mUser.getName()))
                        .setPositiveButton(R.string.block, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                setResult(RESULT_USER_BLOCKED);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                }
            });

        } else {
            scrollView.setVisibility(View.GONE);

            View noConnectionView = findViewById(R.id.noConnectionView);
            noConnectionView.setVisibility(View.VISIBLE);
            ((TextView) noConnectionView.findViewById(R.id.emptyStateTextView)).setText(R.string.no_internet_connection);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendReport() {

        ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(this);
        progressDialog.setMessage(R.string.please_wait);
        progressDialog.setCancelable(false);
        // TODO progressDialog.show();

        String reportInfo = mReportEditText.getText().toString();

        int checkedRadioButtonId = mReportRadioGroup.getCheckedRadioButtonId();
        int reportCode = getCheckedReportCode(checkedRadioButtonId);


        if (!TextUtils.isEmpty(reportInfo)) {

            // TODO reportInfo can be empty

        }

        // TODO Server

    }

    private int getCheckedReportCode(int id) {
        switch (id) {
            case R.id.reportUserWrongName:
                return REPORT_USER_WRONG_NAME;
            case R.id.reportUserWrongLocation:
                return REPORT_USER_WRONG_LOCATION;
            case R.id.reportUserInappropriateContent:
                return REPORT_USER_INAPPROPRIATE_CONTENT;
            case R.id.reportUserMessageSpam:
                return REPORT_USER_MESSAGE_SPAM;
            case R.id.reportUserNotBookGiver:
                return REPORT_USER_NOT_BOOK_GIVER;
            case R.id.reportUserOther:
                return REPORT_USER_OTHER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
