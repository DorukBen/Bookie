package com.karambit.bookie;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.karambit.bookie.fragment.HomeFragment;
import com.karambit.bookie.fragment.MessageFragment;
import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.fragment.SearchFragment;
import com.karambit.bookie.helper.TabFactory;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    private TabHost mTabHost;
    private ViewPager mViewPager;
    private int mOldPos = 0; //Specifys old position for tab view

    //This can be used for listeners
    private HomeFragment mHomeFragment;
    private SearchFragment mSearchFragment;
    private ProfileFragment mProfileFragment;
    private MessageFragment mMessageFragment;

    private View mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConversationActivity.start(this); // TODO Remove

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(s);
            getSupportActionBar().setElevation(0);
        }

        initializeTabHost();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(4);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager() ,getFragments());
        mViewPager.setAdapter(adapter);
    }

    /**
     * initialize a new tab host with fragments in viewpager
     * set's a new onTabChanged listener implement this method
     */
    private void initializeTabHost() {
        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();

        addTab(this, mTabHost, mTabHost.newTabSpec("tab_home").setIndicator("Tab"),R.drawable.tab_home_indicator_selector);
        addTab(this, mTabHost, mTabHost.newTabSpec("tab_search").setIndicator("Tab2"),R.drawable.tab_search_indicator_selector);
        addTab(this, mTabHost, mTabHost.newTabSpec("tab_add_book").setIndicator("Tab3"),R.drawable.tab_add_book_indicator_selector);
        addTab(this, mTabHost, mTabHost.newTabSpec("tab_profile").setIndicator("Tab4"),R.drawable.tab_profile_indicator_selector);
        addTab(this, mTabHost, mTabHost.newTabSpec("tab_notification").setIndicator("Tab5"),R.drawable.tab_notification_indicator_selector);

        mTabHost.setOnTabChangedListener(this);

        ViewCompat.setElevation(mTabHost.getTabWidget().getChildTabViewAt(0),32);
        ViewCompat.setElevation(mTabHost.getTabWidget().getChildTabViewAt(1),32);
        ViewCompat.setElevation(mTabHost.getTabWidget().getChildTabViewAt(3),32);
        ViewCompat.setElevation(mTabHost.getTabWidget().getChildTabViewAt(4),32);

        mIndicator = mTabHost.getCurrentTabView().findViewById(R.id.tab_strip);
        mIndicator.setVisibility(View.VISIBLE);
    }

    private void addTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec,int drawableId) {
        tabSpec.setContent(new TabFactory(activity));

        View tabIndicator = LayoutInflater.from(getBaseContext()).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);

        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);

        icon.setImageResource(drawableId);

        tabSpec.setIndicator(tabIndicator);

        tabHost.addTab(tabSpec);
    }

    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<>();

        mHomeFragment = new HomeFragment();
        mSearchFragment = new SearchFragment();
        mProfileFragment = new ProfileFragment();
        mMessageFragment = new MessageFragment();
        fList.add(mHomeFragment);
        fList.add(mSearchFragment);
        fList.add(mProfileFragment);
        fList.add(mMessageFragment);

        return fList;
    }

    @Override
    public void onTabChanged(String s) {
        int pos = mTabHost.getCurrentTab();

        if (pos != 2){
            mOldPos = pos;
            mTabHost.getTabWidget().dispatchSetSelected(false);
            mTabHost.getCurrentTabView().setSelected(true);
            mIndicator.findViewById(R.id.tab_strip).setVisibility(View.INVISIBLE);

            mIndicator = mTabHost.getCurrentTabView();
            mIndicator.findViewById(R.id.tab_strip).setVisibility(View.VISIBLE);
            mTabHost.getCurrentTabView().findViewById(R.id.tab_strip).setVisibility(View.VISIBLE);

            //Because using position 2 for adding new book not for new fragment
            if (pos > 1){
                mViewPager.setCurrentItem(pos-1);
            }else {
                mViewPager.setCurrentItem(pos);
            }

            getSupportActionBar().setShowHideAnimationEnabled(false);
            if (pos == 1) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }
        }else {
            mIndicator.setSelected(false);
            mTabHost.setCurrentTab(mOldPos);
            //TODO: OnAddBook selected add code here
            Toast.makeText(MainActivity.this, "hele", Toast.LENGTH_SHORT).show();
        }
    }
}
