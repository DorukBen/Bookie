package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.karambit.bookie.fragment.HomeFragment;
import com.karambit.bookie.fragment.MessageFragment;
import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.fragment.SearchFragment;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TabFactory;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private int mOldPos = 0; //Specifys old position for tab view

    //This can be used for listeners
    private HomeFragment mHomeFragment;
    private SearchFragment mSearchFragment;
    private ProfileFragment mProfileFragment;
    private MessageFragment mMessageFragment;

    private ActionBar mActionBar;
    private float[] mElevations;

    private View mIndicator;
    private MenuItem mProfilePageMenuItem;
    private MenuItem mNotificationMenuItem;
    private DoubleTapHomeButtonListener mDoubleTapHomeButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Fragment> fragments = new ArrayList<>();

        if (! SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginRegisterActivity.class));
            finish();
        } else if (!SessionManager.isLovedGenresSelectedLocal(this)) {
            // TODO Server check for loved genres. If loved genres does not exists for user then start LovedGenresActivity
            startActivity(new Intent(this, LovedGenresActivity.class));
            finish();
        }else{
            fragments = getFragments();
        }

            //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(120), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            mActionBar = getSupportActionBar();
            mActionBar.setTitle(s);
            mActionBar.setElevation(0);
        }

        initializeTabHost();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(4);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager() ,fragments);
        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                startActivity(new Intent(this,CurrentUserProfileSettingsActivity.class));
                return true;

            case R.id.action_notification:
                startActivity(new Intent(this,NotificationActivity.class));
                return true;

            default:
                startActivity(new Intent(this,CurrentUserProfileSettingsActivity.class));
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        mProfilePageMenuItem = menu.findItem(R.id.action_more);
        mNotificationMenuItem = menu.findItem(R.id.action_notification);

        if (mViewPager.getCurrentItem() == 3){
            mProfilePageMenuItem.setVisible(true);
        }else{
            mProfilePageMenuItem.setVisible(false);
        }

        if (mViewPager.getCurrentItem() == 0){
            mNotificationMenuItem.setVisible(true);
        }else{
            mNotificationMenuItem.setVisible(false);
        }

        setNotificationMenuItemValue(mNotificationMenuItem);

        return super.onCreateOptionsMenu(menu);
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

        mTabHost.getTabWidget().getChildTabViewAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTabHost.getCurrentTabView() == view){
                    mDoubleTapHomeButtonListener.onDoubleTapHomeButton();
                }else{
                    mTabHost.setCurrentTab(0);
                }
            }
        });

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

        DBHandler dbHandler = new DBHandler(MainActivity.this);

        mHomeFragment = new HomeFragment();
        mSearchFragment = new SearchFragment();
        mProfileFragment = ProfileFragment.newInstance(dbHandler.getCurrentUser());
        mMessageFragment = new MessageFragment();
        fList.add(mHomeFragment);
        fList.add(mSearchFragment);
        fList.add(mProfileFragment);
        fList.add(mMessageFragment);

        mElevations = new float[5];
        Arrays.fill(mElevations, 0);

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

            if (pos == 0){
                mNotificationMenuItem.setVisible(true);
            }else{
                mNotificationMenuItem.setVisible(false);
            }

            if (pos == 1) {
                mActionBar.setShowHideAnimationEnabled(false);
                mActionBar.hide();
            } else {
                mActionBar.setShowHideAnimationEnabled(false);
                mActionBar.show();
            }

            if (pos == 3){
                mProfilePageMenuItem.setVisible(true);
            }else{
                mProfilePageMenuItem.setVisible(false);
            }

            mActionBar.setElevation(mElevations[pos]);

        }else {
            mIndicator.setSelected(false);
            mTabHost.setCurrentTab(mOldPos);
            startActivity(new Intent(this,AddBookActivity.class));
        }
    }

    public void setActionBarElevation(float dp, int tabIndex) {
        if (mActionBar != null && tabIndex == mTabHost.getCurrentTab()) {
            mActionBar.setElevation(dp);
        }
        mElevations[tabIndex] = dp;
    }

    private void setNotificationMenuItemValue(MenuItem item) {
        //TODO: get notification count here
        int notificationCount = 90;
        switch (notificationCount){
            case 0:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification));
                break;
            case 1:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_1));
                break;
            case 2:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_2));
                break;
            case 3:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_3));
                break;
            case 4:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_4));
                break;
            case 5:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_5));
                break;
            case 6:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_6));
                break;
            case 7:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_7));
                break;
            case 8:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_8));
                break;
            case 9:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_9));
                break;
            default:
                item.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_9plus));
                break;
        }
    }

    public interface DoubleTapHomeButtonListener{
        void onDoubleTapHomeButton();
    }

    public void setDoubleTapHomeButtonListener(DoubleTapHomeButtonListener doubleTapHomeButtonListener){
        mDoubleTapHomeButtonListener = doubleTapHomeButtonListener;
    }
}
