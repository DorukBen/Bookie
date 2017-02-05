package com.karambit.bookie;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(120), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(s);
            getSupportActionBar().setElevation(0);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));


        DBHandler dbHandler = new DBHandler(getApplicationContext());
        ArrayList<Notification> notifications = Notification.GENERATOR.generateNotificationList(15);
        for (Notification notification: notifications){
            dbHandler.saveNotificationToDatabase(notification);
        }

        NotificationAdapter notificationAdapter = new NotificationAdapter(this, dbHandler.getAllNotifications(dbHandler.getAllNotificationUsers(), dbHandler.getAllNotificationBooks(dbHandler.getAllBookUsers())));

        notificationAdapter.setSpanTextClickListeners(new NotificationAdapter.SpanTextClickListeners() {
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

        notificationAdapter.setHasStableIds(true);

        recyclerView.setAdapter(notificationAdapter);

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
}
