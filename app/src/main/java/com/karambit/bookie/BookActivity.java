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
import android.widget.Toast;

import com.karambit.bookie.adapter.BookTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

import java.util.Calendar;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = BookActivity.class.getSimpleName();

    private Book mBook;
    private BookTimelineAdapter mBookTimelineAdapter;
    private Book.Details mBookDetails;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(s);
        }

        mBook = getIntent().getParcelableExtra("book");

        mBookDetails = Book.GENERATOR.generateBookDetails(mBook);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBookTimelineAdapter = new BookTimelineAdapter(this, mBookDetails);

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
            public void onArrivedButtonClick(Book.Details details) {

                new AlertDialog.Builder(BookActivity.this)
                    .setTitle(R.string.are_you_sure)
                    .setMessage(R.string.arrived_prompt)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.i_get_the_book, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(BookActivity.this, R.string.owner_change_notification, Toast.LENGTH_SHORT).show();

                            User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                            Book.Transaction transaction = mBook.new Transaction(mBook.getOwner(), Book.TransactionType.COME_TO_HAND,
                                                                                 currentUser, Calendar.getInstance());
                            mBookDetails.getBookProcesses().add(transaction);

                            mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                            mBook.setState(Book.State.CLOSED_TO_SHARE);

                            mBook.setOwner(currentUser);

                            new AlertDialog.Builder(BookActivity.this)
                                .setMessage(R.string.start_reading_prompt)
                                .setPositiveButton(R.string.start_reading, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Book.Interaction startReading = mBook.new Interaction(Book.InteractionType.READ_START,
                                                                                              SessionManager.getCurrentUser(BookActivity.this),
                                                                                              Calendar.getInstance());
                                        mBookDetails.getBookProcesses().add(startReading);

                                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                        mBook.setState(Book.State.READING);
                                        mBookTimelineAdapter.notifyItemChanged(1);

                                        bookProcessToServer(startReading);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mBook.setState(null);
                                        new StateSelectorDialog().show();
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                        }

                    }).create().show();
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
                        .setMessage(R.string.finish_reading_prompt)
                        .setIcon(R.drawable.ic_book_timeline_read_start_stop_36dp)
                        .setPositiveButton(R.string.finished, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new StateSelectorDialog().show();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
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
                startActivity(new Intent(this, BookSettingsActivity.class));
                return true;

            default:
                startActivity(new Intent(this, BookSettingsActivity.class));
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

                    if (mBook.getState() == Book.State.READING) {
                        Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                             SessionManager.getCurrentUser(BookActivity.this),
                                                                             Calendar.getInstance());
                        mBookDetails.getBookProcesses().add(stopReading);
                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                        bookProcessToServer(stopReading);
                    }

                    Book.Interaction openToShare = mBook.new Interaction(Book.InteractionType.OPEN_TO_SHARE,
                                                                          SessionManager.getCurrentUser(BookActivity.this),
                                                                          Calendar.getInstance());
                    mBookDetails.getBookProcesses().add(openToShare);
                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                    mBook.setState(Book.State.OPENED_TO_SHARE);
                    mBookTimelineAdapter.notifyItemChanged(1);

                    dismiss();

                    bookProcessToServer(openToShare);
                }
            };

            View.OnClickListener closeToShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBookTimelineAdapter.getRequestCount() > 0) {

                        new AlertDialog.Builder(BookActivity.this)
                            .setMessage(R.string.requests_rejected_prompt)
                            .setIcon(R.drawable.ic_book_timeline_read_start_stop_36dp)
                            .setPositiveButton(R.string.reject_all, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (mBook.getState() == Book.State.READING) {
                                        Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                                             SessionManager.getCurrentUser(BookActivity.this),
                                                                                             Calendar.getInstance());
                                        mBookDetails.getBookProcesses().add(stopReading);
                                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                        bookProcessToServer(stopReading);
                                    }

                                    Book.Interaction closeToShare = mBook.new Interaction(Book.InteractionType.CLOSE_TO_SHARE,
                                                                                          SessionManager.getCurrentUser(BookActivity.this),
                                                                                          Calendar.getInstance());
                                    mBookDetails.getBookProcesses().add(closeToShare);
                                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                    mBook.setState(Book.State.CLOSED_TO_SHARE);
                                    mBookTimelineAdapter.notifyItemChanged(1);

                                    bookProcessToServer(closeToShare);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .create().show();

                    } else {
                        if (mBook.getState() == Book.State.READING) {
                            Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                                 SessionManager.getCurrentUser(BookActivity.this),
                                                                                 Calendar.getInstance());
                            mBookDetails.getBookProcesses().add(stopReading);

                            bookProcessToServer(stopReading);
                        }

                        Book.Interaction closeToShare = mBook.new Interaction(Book.InteractionType.CLOSE_TO_SHARE,
                                                                              SessionManager.getCurrentUser(BookActivity.this),
                                                                              Calendar.getInstance());
                        mBookDetails.getBookProcesses().add(closeToShare);
                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                        mBook.setState(Book.State.CLOSED_TO_SHARE);
                        mBookTimelineAdapter.notifyItemChanged(1);

                        bookProcessToServer(closeToShare);
                    }

                    dismiss();
                }
            };

            View.OnClickListener startReadingListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBook.setState(Book.State.READING);
                    Book.Interaction startReading = mBook.new Interaction(Book.InteractionType.READ_START,
                                                                          SessionManager.getCurrentUser(BookActivity.this),
                                                                          Calendar.getInstance());
                    mBookDetails.getBookProcesses().add(startReading);
                    mBookTimelineAdapter.notifyItemChanged(1);
                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());
                    dismiss();

                    bookProcessToServer(startReading);
                }
            };

            if (mBook.getState() != null) {

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

            } else {    // For first state selection for book
                setCancelable(false);
                setCanceledOnTouchOutside(false);
                setFinishOnTouchOutside(false);

                button.setVisibility(View.GONE);

                stateImage_1.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                stateText_1.setText(R.string.open_to_share);
                stateChangeContainer_1.setOnClickListener(openToShareListener);

                stateImage_2.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                stateText_2.setText(R.string.close_to_share);
                stateChangeContainer_2.setOnClickListener(closeToShareListener);
            }
        }
    }

    // TODO
    private void bookProcessToServer(Book.BookProcess bookProcess) {

        bookProcess.accept(new Book.TimelineDisplayableVisitor() {
            @Override
            public void visit(Book.Interaction interaction) {

            }

            @Override
            public void visit(Book.Transaction transaction) {

            }

            @Override
            public void visit(Book.Request request) {

            }
        });
    }
}
