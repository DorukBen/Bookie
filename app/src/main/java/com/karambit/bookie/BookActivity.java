package com.karambit.bookie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.karambit.bookie.helper.BookTimelineAdapter;
import com.karambit.bookie.model.Book;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        Book book = getIntent().getParcelableExtra("book");

        Book.Details bookDetails = Book.GENERATOR.generateBookDetails(book);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookRecyclerView.setAdapter(new BookTimelineAdapter(this, bookDetails));
    }
}
