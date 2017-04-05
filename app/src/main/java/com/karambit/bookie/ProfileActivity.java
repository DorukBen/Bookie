package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.helper.InformationDialog;
import com.karambit.bookie.helper.IntentHelper;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public final static String EXTRA_USER = "user";

    private static final int REQUEST_CODE_OTHER_USER_SETTINGS = 1;
    public final static int REQUEST_CODE_MESSAGE_PROCESS = 2;

    private DBManager mDbManager;

    private ActionBar mActionBar;
    private User mUser;
    private TextView mErrorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_APP_NAME_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setTitle("");
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            mActionBar.setElevation(elevation);

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            toolbar.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BookieApplication.hasNetwork()) {
                        Intent data = new Intent(ProfileActivity.this, OtherUserProfileSettingsActivity.class);
                        data.putExtra(OtherUserProfileSettingsActivity.EXTRA_USER, mUser);
                        startActivityForResult(data, REQUEST_CODE_OTHER_USER_SETTINGS);
                    } else {
                        Toast.makeText(ProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            toolbar.findViewById(R.id.messageButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchConversation();
                }
            });
        }

        mErrorView = ((TextView) findViewById(R.id.errorView));

        mDbManager = new DBManager(this);
        mDbManager.open();

        Bundle bundle = getIntent().getExtras();
        mUser = bundle.getParcelable(EXTRA_USER);
        ProfileFragment profileFragment = ProfileFragment.newInstance(mUser);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.profileFragmentFrame, profileFragment );
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OTHER_USER_SETTINGS) {
            if (resultCode == OtherUserProfileSettingsActivity.RESULT_USER_BLOCKED) {

                Logger.d(mUser.getName() + " blocked");
                finish();
            }
        }else if (requestCode == REQUEST_CODE_MESSAGE_PROCESS){
            if (resultCode == ConversationActivity.RESULT_ALL_MESSAGES_DELETED){
                mDbManager.Threaded(mDbManager.getMessageDataSource().cDeleteConversation((User) data.getParcelableExtra(ConversationActivity.EXTRA_OPPOSITE_USER)));
            }
        }
    }

    private void launchConversation() {// Verification control for messaging

        if (SessionManager.getCurrentUserDetails(this).isVerified()) {
            Bundle bundle = getIntent().getExtras();
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra(ConversationActivity.EXTRA_USER, bundle.getParcelable(EXTRA_USER));
            startActivityForResult(intent, REQUEST_CODE_MESSAGE_PROCESS);

        } else {
            final InformationDialog informationDialog = new InformationDialog(this);
            informationDialog.setCancelable(true);
            informationDialog.setPrimaryMessage(R.string.unverified_email_info_short);
            informationDialog.setSecondaryMessage(R.string.unverified_email_message_info);
            informationDialog.setDefaultClickListener(new InformationDialog.DefaultClickListener() {
                @Override
                public void onOkClick() {
                    informationDialog.dismiss();
                }

                @Override
                public void onMoreInfoClick() {
                    Intent intent = new Intent(ProfileActivity.this, InfoActivity.class);
                    intent.putExtra(InfoActivity.EXTRA_INFO_CODES, new int[]{
                        InfoActivity.INFO_CODE_VERIFICATION
                    });
                    startActivity(intent);
                }
            });
            informationDialog.setExtraButtonClickListener(R.string.check_email, new InformationDialog.ExtraButtonClickListener() {
                @Override
                public void onExtraButtonClick() {
                    IntentHelper.openEmailClient(ProfileActivity.this);
                }
            });
            informationDialog.show();
        }
    }

    public void hideError() {
        if (mErrorView != null) {
            mErrorView.setVisibility(View.GONE);
        }
    }

    public void showUnknownError() {
        if (mErrorView != null) {
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(R.string.unknown_error);
        }
    }

    public void showConnectionError() {
        if (mErrorView != null) {
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(R.string.no_internet_connection);
        }
    }

    public boolean isErrorShowing() {
        return mErrorView.getVisibility() == View.VISIBLE;
    }

    public void setErrorViewElevation(float dp) {
        if (mErrorView != null) {
            ViewCompat.setElevation(mErrorView, dp);
        }
    }

    public void setActionBarElevation(float dp) {
        mActionBar.setElevation(dp);
    }
}
