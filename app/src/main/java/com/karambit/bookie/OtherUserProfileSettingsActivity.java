package com.karambit.bookie;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherUserProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = OtherUserProfileSettingsActivity.class.getSimpleName();

    public static final int RESULT_USER_BLOCKED = 666;

    public static final int REPORT_USER_WRONG_NAME = 0;
    public static final int REPORT_USER_WRONG_LOCATION = 1;
    public static final int REPORT_USER_INAPPROPRIATE_CONTENT = 2;
    public static final int REPORT_USER_MESSAGE_SPAM = 3;
    public static final int REPORT_USER_NOT_BOOK_GIVER = 4;
    public static final int REPORT_USER_OTHER = 5;

    public static final String EXTRA_USER = "user";

    private User mUser;
    private EditText mReportEditText;
    private Button mSendReportButton;
    private RadioGroup mReportRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile_settings);

        mUser = getIntent().getParcelableExtra(EXTRA_USER);

        SpannableString s = new SpannableString(mUser.getName());
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(getResources().getDimension(R.dimen.actionbar_starting_elevation));

            actionBar.setTitle("");

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
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

        if (BookieApplication.hasNetwork()) {

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

                                sendReport();

                                mReportEditText.setText("");
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
                                ComfortableProgressDialog progressDialog = new ComfortableProgressDialog(OtherUserProfileSettingsActivity.this);
                                progressDialog.setMessage(R.string.please_wait);
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                uploadBlock(mUser.getID(), progressDialog);
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
        progressDialog.show();

        String reportInfo = mReportEditText.getText().toString();

        int checkedRadioButtonId = mReportRadioGroup.getCheckedRadioButtonId();
        int reportCode = getCheckedReportCode(checkedRadioButtonId);

        uploadReport(reportCode, reportInfo, progressDialog);
    }

    private void uploadReport(int reportCode, String reportInfo, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = SessionManager.getCurrentUserDetails(this).getEmail();
        String password = SessionManager.getCurrentUserDetails(this).getPassword();
        Call<ResponseBody> uploadUserReport = userApi.uploadUserReport(email, password, mUser.getID(), reportCode, reportInfo);

        Logger.d("uploadUserReport() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tuserID=" + mUser.getID() +
                     ", \n\treportCode=" + reportCode + ", \n\treportInfo=" + reportInfo);

        uploadUserReport.enqueue(new Callback<ResponseBody>() {
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
                                Logger.d("Report uploaded succesfully");
                                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.thaks_for_your_report), Toast.LENGTH_SHORT).show();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.USER_NOT_VERIFIED){
                                    Logger.e("User not valid. (Other User Settings Page Error)");
                                }else {
                                    Logger.e("Error true in response: errorCode = " + errorCode);
                                }

                                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Other User Settings Page Error)");
                            Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Other User Settings Page Error)");
                        Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("UpladUserReport Failure: " + t.getMessage());
            }
        });
    }

    private void uploadBlock(int userId, final ComfortableProgressDialog progressDialog) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        String email = SessionManager.getCurrentUserDetails(this).getEmail();
        String password = SessionManager.getCurrentUserDetails(this).getPassword();
        Call<ResponseBody> uploadUserBlock = userApi.uploadUserBlock(email, password, userId);

        Logger.d("getHomePageBooks() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tuserID=" + userId);

        uploadUserBlock.enqueue(new Callback<ResponseBody>() {
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
                                Logger.d("User block uploaded successfully");
                                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.user_blocked, mUser.getName()), Toast.LENGTH_SHORT).show();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null. (Other User Settings Page Error)");
                            Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null. (Other User Settings Page Error)");
                        Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(OtherUserProfileSettingsActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                Logger.e("uploadBlock Failure: " + t.getMessage());
            }
        });
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
