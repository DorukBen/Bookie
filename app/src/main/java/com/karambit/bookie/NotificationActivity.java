package com.karambit.bookie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;

import com.karambit.bookie.adapter.NotificationAdapter;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    public static final int RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED = 1009;
    private BroadcastReceiver mMessageReceiver;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mNotifications = new ArrayList<>();

    private DBManager mDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_APP_NAME_TITLE), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(s);
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            actionBar.setElevation(elevation);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));

        mDbManager = new DBManager(this);
        mDbManager.open();

        mNotifications = mDbManager.getNotificationDataSource().getAllNotifications();
        mNotificationAdapter = new NotificationAdapter(this, mNotifications);

        Logger.d("Notifications fetch from Local DB: " + mNotifications);

        mNotificationAdapter.setSpanTextClickListeners(new NotificationAdapter.SpanTextClickListeners() {
            @Override
            public void onUserNameClick(User user) {
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.EXTRA_USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onBookNameClick(Book book) {
                Intent intent = new Intent(NotificationActivity.this, BookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(BookActivity.EXTRA_BOOK, book);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onUserPhotoClick(User user) {
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.EXTRA_USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onBookImageClick(Book book) {
                Intent intent = new Intent(NotificationActivity.this, BookActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(BookActivity.EXTRA_BOOK, book);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(mNotificationAdapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int totalScrolled = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalScrolled += dy;
                totalScrolled = Math.abs(totalScrolled);

                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(totalScrolled));
            }
        });

        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                mNotifications = mDbManager.getNotificationDataSource().getAllNotifications();
                mNotificationAdapter.notifyDataSetChanged();
                layout.setRefreshing(false);

                Logger.d("Notifications fetch from Local DB: " + mNotifications);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED)) {
                    if (intent.getParcelableExtra("notification") != null) {
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                        Logger.d("Sent request received from FCM: " + notification);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED)) {
                    if (intent.getParcelableExtra("notification") != null) {
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                        Logger.d("Rejected request received from FCM: " + notification);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)) {
                    if (intent.getParcelableExtra("notification") != null) {
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                        Logger.d("Accepted request received from FCM: " + notification);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)) {
                    if (intent.getParcelableExtra("notification") != null) {
                        Notification notification = intent.getParcelableExtra("notification");
                        mNotifications.add(notification);
                        mNotificationAdapter.setNotifications(mNotifications);
                        Logger.d("Book owner changed received from FCM: " + notification);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));

        mDbManager.Threaded(mDbManager.getNotificationDataSource().cUpdateAllNotificationsSeen());

        setResult(NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        mDbManager.Threaded(mDbManager.getNotificationDataSource().cUpdateAllNotificationsSeen());

        setResult(NotificationActivity.RESULT_CODE_ALL_NOTIFICATION_SEENS_DELETED);
    }
}
