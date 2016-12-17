package com.karambit.bookie;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;

import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class ProfileActivity extends AppCompatActivity {

    public final static String USER = "user";
    private android.support.v7.app.ActionBar mAcitonBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAcitonBar = getSupportActionBar();

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(120), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(mAcitonBar != null){
            mAcitonBar.setTitle(s);
            mAcitonBar.setElevation(0);
        }

        Bundle bundle = getIntent().getExtras();
        ProfileFragment profileFragment = ProfileFragment.newInstance(false,(User) bundle.getParcelable(USER));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.profileFragmentFrame, profileFragment );
        transaction.commit();
    }

    public void setActionBarElevation(float dp) {
        mAcitonBar.setElevation(dp);
    }
}
