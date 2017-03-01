package com.karambit.bookie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.karambit.bookie.adapter.NotificationAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    public static final int RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED = 1009;
    private BroadcastReceiver mMessageReceiver;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "comfortaa.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan((int)convertDpToPixel(18, this)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(s);
            getSupportActionBar().setElevation(0);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));


        DBHandler dbHandler = DBHandler.getInstance(this);

        mNotifications = dbHandler.getAllNotifications(dbHandler.getAllNotificationUsers(), dbHandler.getAllNotificationBooks(dbHandler.getAllNotificationBookUsers()));
        mNotificationAdapter = new NotificationAdapter(this, mNotifications);

        mNotificationAdapter.setSpanTextClickListeners(new NotificationAdapter.SpanTextClickListeners() {
            @Override
            public void onUserNameClick(User user) {
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onBookNameClick(Book book) {
                Intent intent = new Intent(NotificationActivity.this, BookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("book", book);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onUserPhotoClick(User user) {
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onBookImageClick(Book book) {
                Intent intent = new Intent(NotificationActivity.this, BookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("book", book);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mNotificationAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mNotificationAdapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            ActionBar actionBar = getSupportActionBar();
            int totalScrolled = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalScrolled += dy;
                totalScrolled = Math.abs(totalScrolled);

                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(totalScrolled));
            }
        });

        PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                //TODO: On page refresh events here layout.serRefreshing() true for start on refresh method
            }
        });

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }

    @Override
    protected void onResume() {
        super.onResume();


        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.SENT_REQUEST_RECEIVED")){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.REJECTED_REQUEST_RECEIVED")){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.ACCEPTED_REQUEST_RECEIVED")){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.BOOK_OWNER_CHANGED_DATA_RECEIVED")){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                    }
                }
            }
        };

        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.SENT_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.REJECTED_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.ACCEPTED_REQUEST_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.BOOK_OWNER_CHANGED_DATA_RECEIVED"));

        DBHandler dbHandler = DBHandler.getInstance(this);
        dbHandler.updateAllNotificationsSeen();
        setResult(NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mMessageReceiver);

        DBHandler dbHandler = DBHandler.getInstance(this);
        dbHandler.updateAllNotificationsSeen();
        setResult(NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED);
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
