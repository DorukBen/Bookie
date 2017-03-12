package com.karambit.bookie;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.adapter.BookTimelineAdapter;
import com.karambit.bookie.adapter.ProfileTimelineAdapter;
import com.karambit.bookie.helper.ComfortableProgressDialog;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookApi;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.service.BookieIntentFilters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = BookActivity.class.getSimpleName();

    public static final int RESULT_BOOK_PROCESS_CHANGED = 1;
    private static final int REQUEST_CODE_REQUEST_CHANGED = 2;
    private static final int REQUEST_CODE_EDIT_BOOK = 3;

    public static final String EXTRA_BOOK = "book";

    private Book mBook;
    private BookTimelineAdapter mBookTimelineAdapter;
    private Book.Details mBookDetails;
    private PullRefreshLayout mPullRefreshLayout;
    private BroadcastReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

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

        mBook = getIntent().getParcelableExtra(EXTRA_BOOK);

        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBookTimelineAdapter = new BookTimelineAdapter(this, mBook);

        mBookTimelineAdapter.setHeaderClickListeners(new BookTimelineAdapter.HeaderClickListeners() {

            @Override
            public void onBookPictureClick(Book book) {
                Intent intent = new Intent(BookActivity.this, PhotoViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("image", book.getImageURL());
                if (mBookDetails != null){
                    if (mBookDetails.getAddedBy().getID() == SessionManager.getCurrentUser(BookActivity.this).getID() &&
                            mBookDetails.getBook().getOwner().getID() == SessionManager.getCurrentUser(BookActivity.this).getID()){
                        bundle.putBoolean("canEditBookImage", true);
                        bundle.putInt("bookID", mBook.getID());
                    }
                }
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mBookTimelineAdapter.setOtherUserClickListeners(new BookTimelineAdapter.StateOtherUserClickListeners() {
            @Override
            public void onRequestButtonClick(Book.Details details) {
                new android.app.AlertDialog.Builder(BookActivity.this)
                        .setMessage(getString(R.string.send_request_to_x, mBook.getOwner().getName()))
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bookProcessToServer(mBookDetails.getBook().new Request(Book.RequestType.SEND,
                                                                                       mBookDetails.getBook().getOwner(),
                                                                                       SessionManager.getCurrentUser(BookActivity.this),
                                                                                       Calendar.getInstance()));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                        .show();
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

                            final User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                            Book.Transaction transaction = mBook.new Transaction(mBook.getOwner(), Book.TransactionType.COME_TO_HAND,
                                                                                 currentUser, Calendar.getInstance());
                            mBookDetails.getBookProcesses().add(transaction);

                            mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                            mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                            mBook.setState(Book.State.CLOSED_TO_SHARE);

                            mBook.setOwner(currentUser);

                            new AlertDialog.Builder(BookActivity.this)
                                .setMessage(R.string.start_reading_prompt)
                                .setPositiveButton(R.string.start_reading, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Book.Interaction startReading = mBook.new Interaction(Book.InteractionType.READ_START,
                                                                                              currentUser,
                                                                                              Calendar.getInstance());
                                        mBookDetails.getBookProcesses().add(startReading);

                                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                        mBookDetails.getBook().setState(Book.State.READING);
                                        mBook.setState(Book.State.READING);
                                        mBookTimelineAdapter.notifyItemChanged(1);

                                        bookProcessToServer(startReading);

                                        Intent data = new Intent();
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("book",mBookDetails.getBook());
                                        data.putExtras(bundle);
                                        setResult(RESULT_BOOK_PROCESS_CHANGED, data);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mBookDetails.getBook().setState(null);
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
            public void onRequestButtonClick(Book.Details bookDetails) {
                Intent intent = new Intent(BookActivity.this, RequestActivity.class);
                intent.putExtra("book", mBook);
                startActivityForResult(intent, REQUEST_CODE_REQUEST_CHANGED);
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

        mPullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                fetchBookPageArguments();
            }
        });

        //For improving recyclerviews performance
        bookRecyclerView.setItemViewCacheSize(20);
        bookRecyclerView.setDrawingCacheEnabled(true);
        bookRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        bookRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int totalScrolled = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalScrolled += dy;
                totalScrolled = Math.abs(totalScrolled);

                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(totalScrolled));
            }
        });

        fetchBookPageArguments();
    }

    @Override
    public void onResume() {
        super.onResume();


        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_SENT_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){
                                mBookDetails.getBookProcesses().add(mBookDetails.getBook().new Request(
                                        Book.RequestType.SEND,
                                        mBookDetails.getBook().getOwner(),
                                        notification.getOppositeUser(),
                                        notification.getCreatedAt()));
                                mBookTimelineAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){
                                mBookDetails.getBookProcesses().add(mBookDetails.getBook().new Request(
                                        Book.RequestType.REJECT,
                                        notification.getOppositeUser(),
                                        mBookDetails.getBook().getOwner(),
                                        notification.getCreatedAt()));
                                mBookTimelineAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){
                                mBookDetails.getBookProcesses().add(mBookDetails.getBook().new Request(
                                        Book.RequestType.ACCEPT,
                                        notification.getOppositeUser(),
                                        mBookDetails.getBook().getOwner(),
                                        notification.getCreatedAt()));

                                mBookDetails.getBookProcesses().add(mBookDetails.getBook().new Transaction(
                                        mBookDetails.getBook().getOwner(),
                                        Book.TransactionType.DISPACTH,
                                        SessionManager.getCurrentUser(context),
                                        notification.getCreatedAt()));

                                mBookDetails.getBook().setState(Book.State.ON_ROAD);
                                mBook.setState(Book.State.ON_ROAD);
                                mBookTimelineAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        if (mBookDetails != null){
                            fetchBookPageArguments();
                        }
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_SENT_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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

                if (mBookDetails != null) {
                    Intent intent = new Intent(this, BookSettingsActivity.class);
                    intent.putExtra("book", mBook);
                    User currentUser = SessionManager.getCurrentUser(this);
                    boolean isAdder = mBookDetails.getAddedBy().equals(currentUser);
                    intent.putExtra("is_adder", isAdder);
                    startActivityForResult(intent, REQUEST_CODE_EDIT_BOOK);
                    return true;

                } else {
                    return false;
                }

            default:
                startActivity(new Intent(this, BookSettingsActivity.class));
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_REQUEST_CHANGED){
            if (resultCode == RequestActivity.RESULT_REQUESTS_MODIFIED){
                fetchBookPageArguments();

            } else if (resultCode == RequestActivity.RESULT_REQUEST_ACCEPTED){
                fetchBookPageArguments();

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                Book book = mBookDetails.getBook();
                book.setState(Book.State.ON_ROAD);
                bundle.putParcelable("book",book);
                intent.putExtras(bundle);
                setResult(RESULT_BOOK_PROCESS_CHANGED, intent);
            }
        } else if (requestCode == REQUEST_CODE_EDIT_BOOK) {
            if (resultCode == BookSettingsActivity.RESULT_BOOK_UPDATED) {
                fetchBookPageArguments();
            }
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
                    User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                    Book.Interaction openToShare = mBook.new Interaction(Book.InteractionType.OPEN_TO_SHARE,
                                                                         currentUser,
                                                                         Calendar.getInstance());

                    if (mBook.getState() == Book.State.READING) {
                        Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                             currentUser,
                                                                             Calendar.getInstance());
                        mBookDetails.getBookProcesses().add(stopReading);
                    }

                    mBookDetails.getBookProcesses().add(openToShare);
                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                    mBookDetails.getBook().setState(Book.State.OPENED_TO_SHARE);
                    mBook.setState(Book.State.OPENED_TO_SHARE);
                    mBookTimelineAdapter.notifyItemChanged(1);

                    dismiss();

                    bookProcessToServer(openToShare);

                    Intent data = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("book",mBookDetails.getBook());
                    data.putExtras(bundle);
                    setResult(RESULT_BOOK_PROCESS_CHANGED, data);
                }
            };

            View.OnClickListener closeToShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                    if (mBookTimelineAdapter.getRequestCount() > 0) {

                        new AlertDialog.Builder(BookActivity.this)
                            .setMessage(R.string.requests_rejected_prompt)
                            .setIcon(R.drawable.ic_book_timeline_read_start_stop_36dp)
                            .setPositiveButton(R.string.reject_all, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Book.Interaction closeToShare = mBook.new Interaction(Book.InteractionType.CLOSE_TO_SHARE,
                                                                                          currentUser,
                                                                                          Calendar.getInstance());
                                    if (mBook.getState() == Book.State.READING) {
                                        Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                                             currentUser,
                                                Calendar.getInstance());
                                        mBookDetails.getBookProcesses().add(stopReading);
                                    }

                                    mBookDetails.getBookProcesses().add(closeToShare);
                                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                    mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                                    mBook.setState(Book.State.CLOSED_TO_SHARE);
                                    mBookTimelineAdapter.notifyItemChanged(1);

                                    bookProcessToServer(closeToShare);

                                    Intent data = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("book",mBookDetails.getBook());
                                    data.putExtras(bundle);
                                    setResult(RESULT_BOOK_PROCESS_CHANGED, data);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .create().show();

                    } else {
                        Book.Interaction closeToShare = mBook.new Interaction(Book.InteractionType.CLOSE_TO_SHARE,
                                                                              currentUser,
                                                                              Calendar.getInstance());

                        if (mBook.getState() == Book.State.READING) {
                            Book.Interaction stopReading = mBook.new Interaction(Book.InteractionType.READ_STOP,
                                                                                 currentUser,
                                    Calendar.getInstance());
                            mBookDetails.getBookProcesses().add(stopReading);
                        }

                        mBookDetails.getBookProcesses().add(closeToShare);
                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                        mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                        mBook.setState(Book.State.CLOSED_TO_SHARE);
                        mBookTimelineAdapter.notifyItemChanged(1);

                        bookProcessToServer(closeToShare);

                        Intent data = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("book",mBookDetails.getBook());
                        data.putExtras(bundle);
                        setResult(RESULT_BOOK_PROCESS_CHANGED, data);
                    }

                    dismiss();
                }
            };

            View.OnClickListener startReadingListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBookDetails.getBook().setState(Book.State.READING);
                    mBook.setState(Book.State.READING);
                    Book.Interaction startReading = mBook.new Interaction(Book.InteractionType.READ_START,
                                                                          SessionManager.getCurrentUser(BookActivity.this),
                                                                          Calendar.getInstance());
                    mBookDetails.getBookProcesses().add(startReading);
                    mBookTimelineAdapter.notifyItemChanged(1);
                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());
                    dismiss();

                    bookProcessToServer(startReading);
                    Intent data = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("book",mBookDetails.getBook());
                    data.putExtras(bundle);
                    setResult(RESULT_BOOK_PROCESS_CHANGED, data);
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

    private void bookProcessToServer(Book.BookProcess bookProcess) {

        bookProcess.accept(new Book.TimelineDisplayableVisitor() {
            @Override
            public void visit(Book.Interaction interaction) {addBookInteractionToServer(interaction);}

            @Override
            public void visit(Book.Transaction transaction) {}

            @Override
            public void visit(Book.Request request) {
                addBookRequestToServer(request);
            }
        });
    }

    private void fetchBookPageArguments() {
        final BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> getBookPageArguments = bookApi.getBookPageArguments(email, password, mBook.getID());

        getBookPageArguments.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NONE);
                                if (!responseObject.isNull("bookDetails")){
                                    JSONObject bookDetailsJson = responseObject.getJSONObject("bookDetails");
                                    mBookDetails = Book.jsonObjectToBookDetails(bookDetailsJson);

                                    if (mBookDetails != null) {
                                        mBook = mBookDetails.getBook();
                                    }
                                    mBookTimelineAdapter.setBookDetails(mBookDetails);
                                }


                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Book Page Error)");
                            mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Book Page Error)");
                        mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    if(NetworkChecker.isNetworkAvailable(BookActivity.this)){
                        mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else{
                        mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                if(NetworkChecker.isNetworkAvailable(BookActivity.this)){
                    mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                }else{
                    mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                }

                mPullRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Book Page onFailure: " + t.getMessage());
            }
        });
    }

    private void addBookInteractionToServer(final Book.Interaction interaction) {

        final ComfortableProgressDialog comfortableProgressDialog = new ComfortableProgressDialog(BookActivity.this);
        comfortableProgressDialog.setMessage(getString(R.string.updating_process));
        comfortableProgressDialog.show();

        final BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> addBookInteraction = bookApi.addBookInteraction(email, password, interaction.getBook().getID(), interaction.getInteractionType().getInteractionCode());

        addBookInteraction.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Intent data = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("book",mBookDetails.getBook());
                                data.putExtras(bundle);
                                setResult(RESULT_BOOK_PROCESS_CHANGED, data);

                                User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                                if (interaction.getBook().getOwner().equals(currentUser)){
                                    mBookDetails.getBook().setOwner(currentUser);
                                    if (interaction.getInteractionType() == Book.InteractionType.CLOSE_TO_SHARE){
                                        mBook.setState(Book.State.CLOSED_TO_SHARE);
                                        mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                                    }else if (interaction.getInteractionType() == Book.InteractionType.OPEN_TO_SHARE){
                                        mBook.setState(Book.State.OPENED_TO_SHARE);
                                        mBookDetails.getBook().setState(Book.State.OPENED_TO_SHARE);
                                    }else if (interaction.getInteractionType() == Book.InteractionType.READ_START){
                                        mBook.setState(Book.State.READING);
                                        mBookDetails.getBook().setState(Book.State.READING);
                                    }
                                    mBookTimelineAdapter.notifyDataSetChanged();
                                }

                                comfortableProgressDialog.dismiss();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                comfortableProgressDialog.dismiss();
                                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Book Page Error)");
                            comfortableProgressDialog.dismiss();
                            Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Book Page Error)");
                        comfortableProgressDialog.dismiss();
                        Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    comfortableProgressDialog.dismiss();
                    Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Book Page onFailure: " + t.getMessage());
                comfortableProgressDialog.dismiss();
                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookRequestToServer(final Book.Request request) {

        final ComfortableProgressDialog comfortableProgressDialog = new ComfortableProgressDialog(BookActivity.this);
        comfortableProgressDialog.setMessage(getString(R.string.updating_process));
        comfortableProgressDialog.show();

        final BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> addBookRequest = bookApi.addBookRequests(email, password, request.getBook().getID(), request.getFromUser().getID(), request.getToUser().getID(), request.getRequestType().getRequestCode());

        addBookRequest.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (mBookDetails != null){
                                    mBookDetails.getBookProcesses().add(request);
                                    mBookTimelineAdapter.notifyDataSetChanged();
                                }
                                comfortableProgressDialog.dismiss();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Book Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                comfortableProgressDialog.dismiss();
                                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Book Page Error)");
                            comfortableProgressDialog.dismiss();
                            Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Book Page Error)");
                        comfortableProgressDialog.dismiss();
                        Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    comfortableProgressDialog.dismiss();
                    Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Book Page onFailure: " + t.getMessage());
                comfortableProgressDialog.dismiss();
                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
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
