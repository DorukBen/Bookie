package com.karambit.bookie;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.fragment.HomeFragment;
import com.karambit.bookie.fragment.MessageFragment;
import com.karambit.bookie.fragment.ProfileFragment;
import com.karambit.bookie.fragment.SearchFragment;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.LayoutUtils;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TabFactory;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.ViewPagerAdapter;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.service.BookieIntentFilters;
import com.karambit.bookie.service.FcmPrefManager;
import com.orhanobut.logger.Logger;

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

    public static final String FONT_APP_NAME_TITLE = "comfortaa.ttf";
    public static final String FONT_GENERAL_TITLE = "comfortaa.ttf";

    public static final String EXTRA_MESSAGE_USER = "message_user";
    public static final String  EXTRA_IS_SINGLE_USER = "is_single_user";
    public static final String EXTRA_OPPOSITE_USER = "opposite_user";
    public static final String EXTRA_NOTIFICATION = "notification";

    private static final int REQUEST_CODE_LOGIN_REGISTER_ACTIVITY = 1;
    private static final int REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY = 2;
    private static final int REQUEST_CODE_CONVERSATION_ACTIVITY = 4;
    private static final int REQUEST_CODE_NOTIFICATION_ACTIVITY = 5;

    private TabHost mTabHost;
    private ViewPager mViewPager;
    private int mOldPos = HomeFragment.TAB_INDEX; //Specifys old position for tab view

    //This can be used for listeners
    private HomeFragment mHomeFragment;
    private SearchFragment mSearchFragment;
    private ProfileFragment mProfileFragment;
    private MessageFragment mMessageFragment;

    private ActionBar mActionBar;
    private float[] mElevations;

    private DBManager mDbManager;

    private View mIndicator;
    private DoubleTapHomeButtonListener mDoubleTapHomeButtonListener;
    private ArrayList<TouchEventListener> mTouchEventListeners = new ArrayList<>();
    private ArrayList<Integer> mIndexOfListenersWillBeDeleted = new ArrayList<>();
    private boolean mIsBackPressed = false;
    private BroadcastReceiver mMessageReceiver;
    private Menu mMenu;
    private TextView mErrorView;
    private LinearLayout mSearchContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        mDbManager = new DBManager(this);
        mDbManager.open();

        mErrorView = ((TextView) findViewById(R.id.errorView));

        boolean loggedIn = SessionManager.isLoggedIn(this);
        if (!loggedIn) {
            startActivityForResult(new Intent(this, LoginRegisterActivity.class), REQUEST_CODE_LOGIN_REGISTER_ACTIVITY);
        } else {
            boolean lovedGenresSelectedLocal = SessionManager.isLovedGenresSelectedLocal(this);
            if (!lovedGenresSelectedLocal) {
                //No need for start activity for result. LovedGenreActivity don't returns any result.
                startActivity(new Intent(this, LovedGenresActivity.class));
            }else{
                initializeViewPager(getFragments(), mViewPager);

                FcmPrefManager fcmPrefManager = new FcmPrefManager(MainActivity.this);
                if (!fcmPrefManager.isUploadedToServer()){
                    sendRegistrationToServer(fcmPrefManager.getFcmToken());
                }
            }
        }

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, FONT_APP_NAME_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            mActionBar = getSupportActionBar();
            mActionBar.setTitle(s);
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            mActionBar.setElevation(elevation);
        }

        mSearchContainer = (LinearLayout) findViewById(R.id.searchContainer);

        initializeTabHost(mTabHost);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED)){
                    fetchNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED)){
                    fetchNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)){
                    fetchNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)){
                    fetchNotificationMenuItemValue();
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_ADDED)) {
                    setCurrentPage(ProfileFragment.VIEW_PAGER_INDEX);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_ADDED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        mDbManager.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                // startActivityForResult for logout
                if (BookieApplication.hasNetwork()) {
                    startActivityForResult(new Intent(this, CurrentUserProfileSettingsActivity.class), REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY);
                } else {
                    showConnectionError();
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_notification:
                startActivityForResult(new Intent(this,NotificationActivity.class), REQUEST_CODE_NOTIFICATION_ACTIVITY);
                return true;

            default:
                startActivity(new Intent(this,CurrentUserProfileSettingsActivity.class));
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        fetchNotificationMenuItemValue();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        int currentPage = mViewPager.getCurrentItem();

        if (currentPage == HomeFragment.VIEW_PAGER_INDEX){
            inflater.inflate(R.menu.home_menu, menu);
        } else if (currentPage == ProfileFragment.VIEW_PAGER_INDEX){
            inflater.inflate(R.menu.current_user_menu, menu);
        }
        fetchNotificationMenuItemValue();

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * initialize a new tab host with fragments in viewpager
     * set's a new onTabChanged listener implement this method
     */
    private void initializeTabHost(final TabHost tabHost) {
        tabHost.setup();

        addTab(this, tabHost, tabHost.newTabSpec(HomeFragment.TAB_SPEC).setIndicator(HomeFragment.TAB_INDICATOR),R.drawable.tab_home_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec(SearchFragment.TAB_SPEC).setIndicator(SearchFragment.TAB_INDICATOR),R.drawable.tab_search_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec(AddBookActivity.TAB_SPEC).setIndicator(AddBookActivity.TAB_INDICATOR),R.drawable.tab_add_book_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec(ProfileFragment.TAB_SPEC).setIndicator(ProfileFragment.TAB_INDICATOR),R.drawable.tab_profile_indicator_selector);
        addTab(this, tabHost, tabHost.newTabSpec(MessageFragment.TAB_SPEC).setIndicator(MessageFragment.TAB_INDICATOR),R.drawable.tab_message_indicator_selector);

        tabHost.setOnTabChangedListener(this);

        float tabElevation = getResources().getDimension(R.dimen.tab_host_item_elevation);

        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(HomeFragment.TAB_INDEX), tabElevation);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(SearchFragment.TAB_INDEX),tabElevation);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(ProfileFragment.TAB_INDEX),tabElevation);
        ViewCompat.setElevation(tabHost.getTabWidget().getChildTabViewAt(MessageFragment.TAB_INDEX),tabElevation);

        tabHost.getTabWidget().getChildTabViewAt(HomeFragment.TAB_INDEX).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabHost.getCurrentTabView() == view){
                    mDoubleTapHomeButtonListener.onDoubleTapHomeButton();
                }else{
                    tabHost.setCurrentTab(HomeFragment.TAB_INDEX);
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
        mElevations[SearchFragment.VIEW_PAGER_INDEX] = ElevationScrollListener.ACTIONBAR_ELEVATION_DP * LayoutUtils.DP;

        return fList;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void onTabChanged(String s) {
        int pos = mTabHost.getCurrentTab();

        if (pos != AddBookActivity.TAB_INDEX){
            mOldPos = pos;
            mTabHost.getTabWidget().dispatchSetSelected(false);
            mTabHost.getCurrentTabView().setSelected(true);
            mIndicator.findViewById(R.id.tab_strip).setVisibility(View.INVISIBLE);

            mIndicator = mTabHost.getCurrentTabView();
            mIndicator.findViewById(R.id.tab_strip).setVisibility(View.VISIBLE);
            mTabHost.getCurrentTabView().findViewById(R.id.tab_strip).setVisibility(View.VISIBLE);

            //Because using position 2 for adding new book not for new fragment
            if (pos > SearchFragment.TAB_INDEX){
                mViewPager.setCurrentItem(pos-1);
            }else {
                mViewPager.setCurrentItem(pos);
            }

            if (pos == SearchFragment.TAB_INDEX) {
                mSearchContainer.setVisibility(View.VISIBLE);
                getSearchEditText().requestFocus();
            } else {
                mSearchContainer.setVisibility(View.GONE);
            }

            mActionBar.setElevation(mElevations[pos]);

            invalidateOptionsMenu();

        }else {
            mIndicator.setSelected(false);
            mTabHost.setCurrentTab(mOldPos);
            startActivity(new Intent(this,AddBookActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            //Controlling result from LoginRegisterActivity
            case REQUEST_CODE_LOGIN_REGISTER_ACTIVITY:
                if (resultCode == LoginRegisterActivity.RESULT_LOGGED_IN){

                    FcmPrefManager fcmPrefManager = new FcmPrefManager(MainActivity.this);
                    sendRegistrationToServer(fcmPrefManager.getFcmToken());


                    if (!SessionManager.isLovedGenresSelectedLocal(this)) {
                        startActivity(new Intent(this, LovedGenresActivity.class));
                    }

                    //Reinitialize view pager on each login session completed
                    initializeViewPager(getFragments(), mViewPager);
                    mTabHost.setCurrentTab(HomeFragment.TAB_INDEX);
                    mViewPager.setCurrentItem(HomeFragment.TAB_INDEX);

                    Logger.d("Result ok while returning to MainActivity from LoginRegisterActivity");
                }else if(resultCode == Activity.RESULT_CANCELED){
                    Logger.d("Result canceled while returning to MainActivity from LoginRegisterActivity");
                    finish();
                }else{
                    Logger.e("An error occurred while returning to MainActivity from LoginRegisterActivity");
                }
                break;
            case REQUEST_CODE_CURRENT_USER_PROFILE_SETTINGS_ACTIVITY:
                if (resultCode == CurrentUserProfileSettingsActivity.RESULT_USER_LOGOUT){
                    //Using SessionManager here to use startActivityForResult() on MainActivity
                    SessionManager.logout(this);
                    startActivityForResult(new Intent(this, LoginRegisterActivity.class), REQUEST_CODE_LOGIN_REGISTER_ACTIVITY);
                }
                break;

            case REQUEST_CODE_CONVERSATION_ACTIVITY:
                if (resultCode == ConversationActivity.RESULT_ALL_MESSAGES_DELETED){
                    mDbManager.Threaded(mDbManager.getMessageDataSource().cDeleteConversation((User) data.getParcelableExtra(EXTRA_OPPOSITE_USER)));
                }
                break;

            case REQUEST_CODE_NOTIFICATION_ACTIVITY:
                if (resultCode == NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED){
                    fetchNotificationMenuItemValue();
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

        if (intent.getParcelableExtra(EXTRA_MESSAGE_USER) != null){
            if (intent.getBooleanExtra(EXTRA_IS_SINGLE_USER, false)){
                Intent conversationIntent = new Intent(this, ConversationActivity.class);

                conversationIntent.putExtra(ConversationActivity.EXTRA_USER, intent.getParcelableExtra(EXTRA_MESSAGE_USER));
                startActivityForResult(conversationIntent, REQUEST_CODE_CONVERSATION_ACTIVITY); // TODO Bureye bak Result olması lazım mı?
            }

            mViewPager.setCurrentItem(MessageFragment.TAB_INDEX, false);
            mTabHost.setCurrentTab(MessageFragment.TAB_INDEX);

        } else if (intent.getParcelableExtra(EXTRA_NOTIFICATION) != null){
            startActivity(new Intent(this, NotificationActivity.class));
        }
    }

    public void fetchNotificationMenuItemValue() {
        int notificationCount = mDbManager.getNotificationDataSource().getUnseenNotificationCount();

        if (mMenu.hasVisibleItems()) {

            MenuItem notificationItem = mMenu.findItem(R.id.action_notification);

            if (notificationItem != null) {
                switch (notificationCount) {
                    case 0:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification));
                        break;
                    case 1:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_1));
                        break;
                    case 2:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_2));
                        break;
                    case 3:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_3));
                        break;
                    case 4:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_4));
                        break;
                    case 5:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_5));
                        break;
                    case 6:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_6));
                        break;
                    case 7:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_7));
                        break;
                    case 8:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_8));
                        break;
                    case 9:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_9));
                        break;
                    default:
                        notificationItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_notification_9plus));
                        break;
                }
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

    private void sendRegistrationToServer(final String token) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
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

                                Logger.d("Token sent completed successfuly");
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);
                            }
                        }else{
                            Logger.e("Response body is null. (Book Page Error)");
                        }
                    }else {
                        Logger.e("Response object is null. (Book Page Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("Book Page onFailure: " + t.getMessage());
                sendRegistrationToServer(token);
            }
        });
    }

    public void setCurrentPage(int viewpagerIndex) {
        mViewPager.setCurrentItem(viewpagerIndex, true);
        mTabHost.setCurrentTab(viewpagerIndex < 2 ? viewpagerIndex : viewpagerIndex + 1);
    }

    @Override
    public void onBackPressed() {
        if (mTabHost.getCurrentTab() != HomeFragment.TAB_INDEX){
            if (!mSearchFragment.isSearchEditTextEmpty() && mTabHost.getCurrentTab() == SearchFragment.TAB_INDEX) {
                mSearchFragment.clearSearchEditText();
            } else {
                mViewPager.setCurrentItem(HomeFragment.TAB_INDEX, true);
                mTabHost.setCurrentTab(HomeFragment.TAB_INDEX);
            }
        }else {
            if(!mIsBackPressed){
                Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
                mIsBackPressed = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsBackPressed = false;
                    }
                }, 1500);
            }else {
                super.onBackPressed();
            }

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

    public void setErrorViewElevation(float dp, int tabIndex) {
        if (mErrorView != null && tabIndex == mTabHost.getCurrentTab()) {
            ViewCompat.setElevation(mErrorView, dp);
        }
        mElevations[tabIndex] = dp;
    }

    public void setActionBarElevation(float dp, int tabIndex) {
        if (mActionBar != null && tabIndex == mTabHost.getCurrentTab()) {
            mActionBar.setElevation(dp);
        }
        mElevations[tabIndex] = dp;
    }

    public EditText getSearchEditText(){
        return (EditText) mSearchContainer.findViewById(R.id.searchEditText);
    }

    public ImageButton getSearchImageButton() {
        return (ImageButton) mSearchContainer.findViewById(R.id.searchButton);
    }
}
