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

import com.karambit.bookie.adapter.NotificationAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;
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
        ArrayList<Notification> notifications = Notification.GENERATOR.generateNotificationList(15);

        NotificationAdapter notificationAdapter = new NotificationAdapter(this, notifications);

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

        recyclerView.setDrawingCacheEnabled(false);

    }
}
