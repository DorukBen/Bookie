package com.karambit.bookie;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public final static String USER = "user";
    private static final int REQUEST_OTHER_USER_SETTINGS = 111;

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
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan((int)convertDpToPixel(32, this)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(mActionBar != null){
            mActionBar.setTitle(s);
            mActionBar.setElevation(0);
        }

        Bundle bundle = getIntent().getExtras();
        mUser = bundle.getParcelable(USER);
        ProfileFragment profileFragment = ProfileFragment.newInstance(mUser);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.profileFragmentFrame, profileFragment );
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_more).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                Intent intent = new Intent(this, OtherUserProfileSettingsActivity.class);
                intent.putExtra("user", mUser);
                startActivityForResult(intent, REQUEST_OTHER_USER_SETTINGS);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OTHER_USER_SETTINGS) {
            if (resultCode == OtherUserProfileSettingsActivity.RESULT_USER_BLOCKED) {

                // TODO Block user
                Log.i(TAG, mUser.getName() + " blocked");
                finish();
            }
        }
    }

    public void setActionBarElevation(float dp) {
        mActionBar.setElevation(dp);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
