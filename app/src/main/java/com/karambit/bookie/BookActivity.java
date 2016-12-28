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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.karambit.bookie.adapter.BookTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "autograf.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(120), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(s);
        }

        Book book = getIntent().getParcelableExtra("book");

        Book.Details bookDetails = Book.GENERATOR.generateBookDetails(book);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        BookTimelineAdapter adapter = new BookTimelineAdapter(this, bookDetails);

        adapter.setHeaderClickListeners(new BookTimelineAdapter.HeaderClickListeners() {

            @Override
            public void onBookPictureClick(Book.Details details) {
                Intent intent = new Intent(BookActivity.this, PhotoViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("image", details.getBook().getImageURL());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        adapter.setOtherUserClickListeners(new BookTimelineAdapter.StateOtherUserClickListeners() {
            @Override
            public void onRequestButtonClick(Book.Details details) {
                // TODO Request button
            }

            @Override
            public void onOwnerClick(User owner) {
                Intent intent = new Intent(BookActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, owner);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        adapter.setCurrentUserClickListeners(new BookTimelineAdapter.StateCurrentUserClickListeners() {
            @Override
            public void onStateClick(Book.Details bookDetails) {
                // TODO State Click
            }

            @Override
            public void onRequestCountClick(Book.Details bookDetails) {
                // TODO Request Count Click
            }
        });

        adapter.setHasStableIds(true);

        adapter.setSpanTextClickListener(new BookTimelineAdapter.SpanTextClickListeners() {
            @Override
            public void onUserNameClick(User user) {
                Intent intent = new Intent(BookActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        bookRecyclerView.setAdapter(adapter);

        //For improving recyclerviews performance
        bookRecyclerView.setItemViewCacheSize(20);
        bookRecyclerView.setDrawingCacheEnabled(true);
        bookRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        bookRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

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
                startActivity(new Intent(this,BookSettingsActivity.class));
                return true;

            default:
                startActivity(new Intent(this,BookSettingsActivity.class));
                return super.onOptionsItemSelected(item);

        }
    }
}
