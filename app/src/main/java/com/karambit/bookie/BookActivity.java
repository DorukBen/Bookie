package com.karambit.bookie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.karambit.bookie.helper.BookTimelineAdapter;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

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
    }
}
