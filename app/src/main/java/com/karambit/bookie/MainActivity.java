package com.karambit.bookie;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.service.FcmPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_LOGIN_REGISTER_ACTIVITY = 1;
    private static final int REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY = 2;
    private static final int REQUEST_CODE_ADD_BOOK_ACTIVITY = 3;
    private static final int REQUEST_CODE_IS_ALL_MESSAGES_DELETED = 1007;
    private static final int REQUEST_CODE_DELETE_NOTIFICATION_SEENS = 1008;
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
    private ArrayList<TouchEventListener> mTouchEventListeners = new ArrayList<>();
    private ArrayList<Integer> mIndexOfListenersWillBeDeleted = new ArrayList<>();
    private boolean mIsBackPressed = false;
    private BroadcastReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        if (! SessionManager.isLoggedIn(this)) {
            startActivityForResult(new Intent(this, LoginRegisterActivity.class), REQUEST_CODE_LOGIN_REGISTER_ACTIVITY);
        } else if (!SessionManager.isLovedGenresSelectedLocal(this)) {
            //No need for start activity for result. LovedGenreActivity don't returns any result.
            startActivity(new Intent(this, LovedGenresActivity.class));
        }else{
            initializeViewPager(getFragments(), mViewPager);
        }

            //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "comfortaa.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan((int) convertDpToPixel(18, this)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            mActionBar = getSupportActionBar();
            mActionBar.setTitle(s);
            mActionBar.setElevation(0);
        }

        initializeTabHost(mTabHost);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                startActivityForResult(new Intent(this,CurrentUserProfileSettingsActivity.class), REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY);
                return true;

            case R.id.action_notification:
                startActivityForResult(new Intent(this,NotificationActivity.class), REQUEST_CODE_DELETE_NOTIFICATION_SEENS);
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

        setNotificationMenuItemValue();

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * initialize a new tab host with fragments in viewpager
     * set's a new onTabChanged listener implement this method
     */
    private void initializeTabHost(final TabHost tabHost) {
        tabHost.setup();

        addTab(this, tabHost, tabHost.newTabSpec("tab_home").setIndicator("Tab"),R.drawable.tab_home_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec("tab_search").setIndicator("Tab2"),R.drawable.tab_search_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec("tab_add_book").setIndicator("Tab3"),R.drawable.tab_add_book_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec("tab_profile").setIndicator("Tab4"),R.drawable.tab_profile_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec("tab_notification").setIndicator("Tab5"),R.drawable.tab_notification_indicator_selector);

        tabHost.setOnTabChangedListener(this);

        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(0),32);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(1),32);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(3),32);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(4),32);

        tabHost.getTabWidget().getChildTabViewAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabHost.getCurrentTabView() == view){
                    mDoubleTapHomeButtonListener.onDoubleTapHomeButton();
                }else{
                    tabHost.setCurrentTab(0);
                }
            }
        });

        mIndicator = tabHost.getCurrentTabView().findViewById(R.id.tab_strip);
        mIndicator.setVisibility(View.VISIBLE);
    }

    private void initializeViewPager(List<Fragment> fragments, ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(4);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager() ,fragments);
        viewPager.setAdapter(adapter);
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
        mProfileFragment = ProfileFragment.newInstance(SessionManager.getCurrentUser(this));
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
            startActivityForResult(new Intent(this,AddBookActivity.class), REQUEST_CODE_ADD_BOOK_ACTIVITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            //Controlling result from LoginRegisterActivity
            case REQUEST_CODE_LOGIN_REGISTER_ACTIVITY:
                if (resultCode == Activity.RESULT_OK){

                    FcmPrefManager fcmPrefManager = new FcmPrefManager(MainActivity.this);
                    if (!fcmPrefManager.isUploadedToServer()){
                        sendRegistrationToServer(fcmPrefManager.getFcmToken());
                    }

                    if (!SessionManager.isLovedGenresSelectedLocal(this)) {
                        startActivity(new Intent(this, LovedGenresActivity.class));
                    }

                    //Reinitialize view pager on each login session completed
                    initializeViewPager(getFragments(), mViewPager);
                    mTabHost.setCurrentTab(HomeFragment.HOME_FRAGMENT_TAB_INEX);
                    mViewPager.setCurrentItem(HomeFragment.HOME_FRAGMENT_TAB_INEX);

                    Log.d(TAG,"Result ok while returning to MainActivity from LoginRegisterActivity");
                }else if(resultCode == Activity.RESULT_CANCELED){
                    Log.d(TAG,"Result canceled while returning to MainActivity from LoginRegisterActivity");
                    finish();
                }else{
                    Log.e(TAG, "An error occurred while returning to MainActivity from LoginRegisterActivity");
                }
                break;
            case REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY:
                if (resultCode == CurrentUserProfileSettingsActivity.RESULT_USER_LOGOUT){
                    //Using SessionManager here to use startActivityForResult() on MainActivity
                    SessionManager.logout(getApplicationContext());
                    startActivityForResult(new Intent(this, LoginRegisterActivity.class), REQUEST_CODE_LOGIN_REGISTER_ACTIVITY);

                } else if (resultCode == CurrentUserProfileSettingsActivity.RESULT_USER_UPDATED) {
                    mProfileFragment.refreshProfilePage();
                }

                break;

            case REQUEST_CODE_ADD_BOOK_ACTIVITY:
                if (resultCode == AddBookActivity.RESULT_BOOK_CREATED){
                    mProfileFragment.refreshProfilePage();
                }
                break;

            case REQUEST_CODE_IS_ALL_MESSAGES_DELETED:
                if (resultCode == ConversationActivity.ALL_MESSAGES_DELETED){
                    DBHandler dbHandler = new DBHandler(getApplicationContext());
                    dbHandler.deleteMessageUser((User) data.getParcelableExtra("opposite_user"));
                    dbHandler.deleteMessageUsersConversation((User) data.getParcelableExtra("opposite_user"));
                }
                break;

            case REQUEST_CODE_DELETE_NOTIFICATION_SEENS:
                if (resultCode == NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED){
                    setNotificationMenuItemValue();
                }
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Iterator<TouchEventListener> iterator = mTouchEventListeners.iterator();

        while (iterator.hasNext()) {
            TouchEventListener touchEventListener = iterator.next();

            touchEventListener.onTouchEvent(ev);

            Integer index = mTouchEventListeners.indexOf(touchEventListener);

            if (mIndexOfListenersWillBeDeleted.contains(index)) {
                mIndexOfListenersWillBeDeleted.remove(index);
                iterator.remove();
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getParcelableExtra("message_user") != null){
            DBHandler dbHandler = new DBHandler(getApplicationContext());
            if (dbHandler.isMessageUserExists((User)intent.getParcelableExtra("message_user"))){
                Intent conversationIntent = new Intent(this, ConversationActivity.class);

                conversationIntent.putExtra("user", intent.getParcelableExtra("message_user"));
                startActivityForResult(conversationIntent, REQUEST_CODE_IS_ALL_MESSAGES_DELETED);
            }
        }

        mViewPager.setCurrentItem(4, false);
        mTabHost.setCurrentTab(4);
    }

    public void setActionBarElevation(float dp, int tabIndex) {
        if (mActionBar != null && tabIndex == mTabHost.getCurrentTab()) {
            mActionBar.setElevation(dp);
        }
        mElevations[tabIndex] = dp;
    }

    @Override
    protected void onResume() {
        super.onResume();


        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.SENT_REQUEST_RECEIVED")){
                    setNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.REJECTED_REQUEST_RECEIVED")){
                    setNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.ACCEPTED_REQUEST_RECEIVED")){
                    setNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.BOOK_OWNER_CHANGED_DATA_RECEIVED")){
                    setNotificationMenuItemValue();
                }
            }
        };

        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.SENT_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.REJECTED_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.ACCEPTED_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.BOOK_OWNER_CHANGED_DATA_RECEIVED"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mMessageReceiver);
    }

    private void setNotificationMenuItemValue() {
        DBHandler dbHandler = new DBHandler(getApplicationContext());
        int notificationCount = dbHandler.getUnseenNotificationCount();
        if (mNotificationMenuItem != null){
            switch (notificationCount){
                case 0:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification));
                    break;
                case 1:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_1));
                    break;
                case 2:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_2));
                    break;
                case 3:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_3));
                    break;
                case 4:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_4));
                    break;
                case 5:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_5));
                    break;
                case 6:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_6));
                    break;
                case 7:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_7));
                    break;
                case 8:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_8));
                    break;
                case 9:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_9));
                    break;
                default:
                    mNotificationMenuItem.setIcon(ContextCompat.getDrawable(MainActivity.this ,R.drawable.main_notification_9plus));
                    break;
            }
        }
    }

    public interface DoubleTapHomeButtonListener{
        void onDoubleTapHomeButton();
    }

    public void setDoubleTapHomeButtonListener(DoubleTapHomeButtonListener doubleTapHomeButtonListener){
        mDoubleTapHomeButtonListener = doubleTapHomeButtonListener;
    }

    public interface TouchEventListener {
        void onTouchEvent(MotionEvent event);
    }

    public void addTouchEventListener(TouchEventListener touchEventListener) {
        mTouchEventListeners.add(touchEventListener);
    }

    public void removeTouchEventListener(TouchEventListener touchEventListener) {
        mIndexOfListenersWillBeDeleted.add(mTouchEventListeners.indexOf(touchEventListener));
    }

    public void clearTouchEventListener() {
        mTouchEventListeners.clear();
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

    private void sendRegistrationToServer(final String token) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);
        String email = SessionManager.getCurrentUserDetails(getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getApplicationContext()).getPassword();
        Call<ResponseBody> sendTokenToServer = fcmApi.sendFcmTokenToServer(email, password, token);

        sendTokenToServer.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                FcmPrefManager fcmPrefManager = new FcmPrefManager(MainActivity.this);
                                fcmPrefManager.setUploadedToServer(true);

                                Log.i(TAG, "Token sent completed successfuly");
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Book Page Error)");
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Book Page Error)");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Book Page onFailure: " + t.getMessage());
                sendRegistrationToServer(token);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!mIsBackPressed){
            if (mTabHost.getCurrentTab() != 0){
                mViewPager.setCurrentItem(0, true);
                mTabHost.setCurrentTab(0);
            }else {
                mIsBackPressed = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsBackPressed = false;
                    }
                }, 500);
            }
        }else {
            super.onBackPressed();
        }
    }
}
