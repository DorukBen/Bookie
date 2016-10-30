package com.karambit.bookie;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;

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
            getSupportActionBar().setElevation(0);
        }

        Book book = getIntent().getParcelableExtra("book");

        Book.Details bookDetails = Book.GENERATOR.generateBookDetails(book);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        BookTimelineAdapter adapter = new BookTimelineAdapter(this, bookDetails);

        adapter.setHeaderClickListeners(new BookTimelineAdapter.HeaderClickListeners() {
            @Override
            public void onRequestButtonClick(Book.Details details) {

                // TODO Request
            }

            @Override
            public void onOwnerClick(User owner) {
                
                // TODO Owner
            }

            @Override
            public void onBookPictureClick(Book.Details details) {

                // TODO Book picture
            }
        });

        bookRecyclerView.setAdapter(adapter);

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
}
