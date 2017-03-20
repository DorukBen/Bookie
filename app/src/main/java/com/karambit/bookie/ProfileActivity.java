package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.database.DBHelper;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public final static String EXTRA_USER = "user";

    private static final int REQUEST_CODE_OTHER_USER_SETTINGS = 1;
    public final static int REQUEST_CODE_MESSAGE_PROCESS = 2;

    private DBManager mDbManager;

    private ActionBar mActionBar;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mActionBar = getSupportActionBar();

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_APP_NAME_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(mActionBar != null){
            mActionBar.setTitle(s);
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            mActionBar.setElevation(elevation);
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.other_user_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                Intent data = new Intent(this, OtherUserProfileSettingsActivity.class);
                data.putExtra(OtherUserProfileSettingsActivity.EXTRA_USER, mUser);
                startActivityForResult(data, REQUEST_CODE_OTHER_USER_SETTINGS);
                return true;

            case R.id.action_message:
                Bundle bundle = getIntent().getExtras();
                Intent intent = new Intent(this,ConversationActivity.class);
                intent.putExtra(ConversationActivity.EXTRA_USER, bundle.getParcelable(EXTRA_USER));
                startActivityForResult(intent, REQUEST_CODE_MESSAGE_PROCESS);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OTHER_USER_SETTINGS) {
            if (resultCode == OtherUserProfileSettingsActivity.RESULT_USER_BLOCKED) {

                // TODO Block user
                Log.i(TAG, mUser.getName() + " blocked");
                finish();
            }
        }else if (requestCode == REQUEST_CODE_MESSAGE_PROCESS){
            if (resultCode == ConversationActivity.RESULT_ALL_MESSAGES_DELETED){
                mDbManager.getMessageDataSource().deleteConversation((User) data.getParcelableExtra(ConversationActivity.EXTRA_OPPOSITE_USER));
            }
        }
    }

    public void setActionBarElevation(float dp) {
        mActionBar.setElevation(dp);
    }
}
