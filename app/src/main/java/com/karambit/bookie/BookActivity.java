package com.karambit.bookie;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.karambit.bookie.adapter.BookTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

public class BookActivity extends AppCompatActivity {

    private Book mBook;
    private BookTimelineAdapter mBookTimelineAdapter;

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

        mBook = getIntent().getParcelableExtra("book");

        Book.Details bookDetails =  Book.GENERATOR.generateBookDetails(mBook);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBookTimelineAdapter = new BookTimelineAdapter(this, bookDetails);

        mBookTimelineAdapter.setHeaderClickListeners(new BookTimelineAdapter.HeaderClickListeners() {

            @Override
            public void onBookPictureClick(Book.Details details) {
                Intent intent = new Intent(BookActivity.this, PhotoViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("image", details.getBook().getImageURL());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mBookTimelineAdapter.setOtherUserClickListeners(new BookTimelineAdapter.StateOtherUserClickListeners() {
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

        mBookTimelineAdapter.setCurrentUserClickListeners(new BookTimelineAdapter.StateCurrentUserClickListeners() {
            @Override
            public void onStateClick(Book.Details bookDetails) {

                if (mBook.getState() == Book.State.READING) {

                    new AlertDialog.Builder(BookActivity.this)
                        .setMessage("Has your reading finished?")
                        .setIcon(R.drawable.ic_book_timeline_read_start_stop_36dp)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new StateSelectorDialog().show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create().show();

                } else {
                    new StateSelectorDialog().show();
                }
            }

            @Override
            public void onRequestCountClick(Book.Details bookDetails) {
                // TODO Request Count Click
            }
        });

        mBookTimelineAdapter.setHasStableIds(true);

        mBookTimelineAdapter.setSpanTextClickListener(new BookTimelineAdapter.SpanTextClickListeners() {
            @Override
            public void onUserNameClick(User user) {
                Intent intent = new Intent(BookActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        bookRecyclerView.setAdapter(mBookTimelineAdapter);

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

    private class StateSelectorDialog extends Dialog {

        public StateSelectorDialog() {
            super(BookActivity.this);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.state_selector_dialog);

            Button button = (Button) findViewById(R.id.cancelButton);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            View stateChangeContainer_1 = findViewById(R.id.stateChangeContainer_1);
            ImageView stateImage_1 = (ImageView) findViewById(R.id.stateChangeImage_1);
            TextView stateText_1 = (TextView) findViewById(R.id.stateChangeText_1);

            View stateChangeContainer_2 = findViewById(R.id.stateChangeContainer_2);
            ImageView stateImage_2 = (ImageView) findViewById(R.id.stateChangeImage_2);
            TextView stateText_2 = (TextView) findViewById(R.id.stateChangeText_2);

            View.OnClickListener openToShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBook.setState(Book.State.OPENED_TO_SHARE);
                    mBookTimelineAdapter.notifyItemChanged(1);
                    dismiss();
                    stateChangeToServer(Book.State.OPENED_TO_SHARE);
                }
            };

            View.OnClickListener closeToShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBookTimelineAdapter.getRequestCount() > 0) {
                        new AlertDialog.Builder(BookActivity.this)
                            .setMessage("All requests for this book will be rejected!")
                            .setIcon(R.drawable.ic_book_timeline_read_start_stop_36dp)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBook.setState(Book.State.CLOSED_TO_SHARE);
                                    mBookTimelineAdapter.notifyItemChanged(1);
                                    stateChangeToServer(Book.State.CLOSED_TO_SHARE);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .create().show();

                    } else {
                        mBook.setState(Book.State.CLOSED_TO_SHARE);
                        mBookTimelineAdapter.notifyItemChanged(1);
                        stateChangeToServer(Book.State.CLOSED_TO_SHARE);
                    }

                    dismiss();
                }
            };

            View.OnClickListener startReadingListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBook.setState(Book.State.READING);
                    mBookTimelineAdapter.notifyItemChanged(1);
                    dismiss();
                    stateChangeToServer(Book.State.READING);
                }
            };

            switch (mBook.getState()) {

                case READING:
                    stateImage_1.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                    stateText_1.setText(R.string.open_to_share);
                    stateChangeContainer_1.setOnClickListener(openToShareListener);

                    stateImage_2.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                    stateText_2.setText(R.string.close_to_share);
                    stateChangeContainer_2.setOnClickListener(closeToShareListener);
                    break;
                case OPENED_TO_SHARE:
                    stateImage_1.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                    stateText_1.setText(R.string.start_reading);
                    stateChangeContainer_1.setOnClickListener(startReadingListener);

                    stateImage_2.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                    stateText_2.setText(R.string.close_to_share);
                    stateChangeContainer_2.setOnClickListener(closeToShareListener);

                    break;

                case CLOSED_TO_SHARE:
                    stateImage_1.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                    stateText_1.setText(R.string.start_reading);
                    stateChangeContainer_1.setOnClickListener(startReadingListener);

                    stateImage_2.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                    stateText_2.setText(R.string.open_to_share);
                    stateChangeContainer_2.setOnClickListener(openToShareListener);

                    break;
            }
        }
    }

    private void stateChangeToServer(Book.State state) {
        // TODO
    }
}
