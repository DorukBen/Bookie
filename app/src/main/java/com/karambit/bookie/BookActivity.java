package com.karambit.bookie;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.karambit.bookie.helper.InformationDialog;
import com.karambit.bookie.helper.IntentHelper;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Interaction;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.Request;
import com.karambit.bookie.model.Transaction;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookApi;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = BookActivity.class.getSimpleName();

    public static final int RESULT_BOOK_PROCESS_CHANGED = 1;
    private static final int REQUEST_CODE_REQUEST_CHANGED = 2;
    private static final int REQUEST_CODE_EDIT_BOOK = 3;
    private static final int REQUEST_CODE_LOCATION = 4;

    public static final String EXTRA_BOOK = "book";

    private Book mBook;
    private BookTimelineAdapter mBookTimelineAdapter;
    private Book.Details mBookDetails;
    private PullRefreshLayout mPullRefreshLayout;
    private BroadcastReceiver mMessageReceiver;
    private InformationDialog mLocationInfoDialog;

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
                intent.putExtra(PhotoViewerActivity.EXTRA_IMAGE, book.getImageURL());
                if (mBookDetails != null){
                    if (mBookDetails.getAddedBy().getID() == SessionManager.getCurrentUser(BookActivity.this).getID() &&
                            mBookDetails.getBook().getOwner().getID() == SessionManager.getCurrentUser(BookActivity.this).getID()){
                        intent.putExtra(PhotoViewerActivity.EXTRA_CAN_EDIT_BOOK_IMAGE, true);
                        intent.putExtra(PhotoViewerActivity.EXTRA_BOOK_ID, mBook.getID());
                    }
                }
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
                            Request request = new Request(mBookDetails.getBook(),
                                                          SessionManager.getCurrentUser(BookActivity.this),
                                                          mBookDetails.getBook().getOwner(),
                                                          Request.Type.SEND,
                                                          Calendar.getInstance());

                            bookProcessToServer(request);
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

                            Transaction transaction = new Transaction(mBook,
                                                                      mBook.getOwner(),
                                                                      currentUser,
                                                                      Transaction.Type.COME_TO_HAND,
                                                                      Calendar.getInstance());

                            mBookDetails.getBookProcesses().add(transaction);

                            mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                            mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                            mBook.setState(Book.State.CLOSED_TO_SHARE);

                            mBook.setOwner(currentUser);

                            Logger.d("Book owned by current user: " + transaction);

                            new AlertDialog.Builder(BookActivity.this)
                                .setMessage(R.string.start_reading_prompt)
                                .setPositiveButton(R.string.start_reading, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Interaction startReading = new Interaction(mBook,
                                                                                   currentUser,
                                                                                   Interaction.Type.READ_START,
                                                                                   Calendar.getInstance());

                                        mBookDetails.getBookProcesses().add(startReading);

                                        mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                                        mBookDetails.getBook().setState(Book.State.READING);
                                        mBook.setState(Book.State.READING);
                                        mBookTimelineAdapter.notifyItemChanged(1);

                                        bookProcessToServer(startReading);
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
                bundle.putParcelable(ProfileActivity.EXTRA_USER, owner);
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
                // TODO When another user who owns a book that I requested, clicks accept to another users request. My rejected does not display in th page and the counter will wrong

                ArrayList<Request> sendRequests = new ArrayList<>();
                ArrayList<Request> answeredRequests = new ArrayList<>();

                int i = mBookDetails.getBookProcesses().size();
                Book.BookProcess bookProcess;
                do {

                    i--;

                    bookProcess = mBookDetails.getBookProcesses().get(i);

                    if (bookProcess instanceof Request) {
                        if (((Request) bookProcess).getType() == Request.Type.SEND){
                            sendRequests.add((Request) bookProcess);
                        }else {
                            answeredRequests.add((Request) bookProcess);
                        }
                    }

                } while
                    (!(bookProcess instanceof Transaction &&
                    ((Transaction) bookProcess).getType() == Transaction.Type.COME_TO_HAND &&
                    ((Transaction) bookProcess).getTaker().equals(SessionManager.getCurrentUser(BookActivity.this))) &&
                    i > 0);

                for (Request answeredRequest: answeredRequests){
                    Iterator<Request> iterator = sendRequests.iterator();
                    while (iterator.hasNext()){
                        Request sendRequest = iterator.next();
                        if (sendRequest.getRequester().equals(answeredRequest.getRequester()) &&
                            sendRequest.getCreatedAt().getTimeInMillis() < answeredRequest.getCreatedAt().getTimeInMillis()){

                            iterator.remove();
                        }
                    }
                }
                ArrayList<Request> allRequests = new ArrayList<Request>();
                allRequests.addAll(sendRequests);
                allRequests.addAll(answeredRequests);

                Intent intent = new Intent(BookActivity.this, RequestActivity.class);
                intent.putExtra(RequestActivity.EXTRA_BOOK, mBook);
                intent.putExtra(RequestActivity.EXTRA_REQUESTS, allRequests);
                startActivityForResult(intent, REQUEST_CODE_REQUEST_CHANGED);
            }
        });

        mBookTimelineAdapter.setSpanTextClickListener(new BookTimelineAdapter.SpanTextClickListeners() {
            @Override
            public void onUserNameClick(User user) {
                Intent intent = new Intent(BookActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.EXTRA_USER, user);
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

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){

                                Request request = new Request(
                                    mBookDetails.getBook(),
                                    notification.getOppositeUser(),
                                    SessionManager.getCurrentUser(context),
                                    Request.Type.SEND,
                                    notification.getCreatedAt());

                                mBookDetails.getBookProcesses().add(request);

                                mBookTimelineAdapter.notifyItemInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex());
                                // Top book process must be refreshed for timeline divider.
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 1);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Sent request received from FCM: " + notification +
                                             "\n\n Request added to adapter: " + request);
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){

                                Request request = new Request(
                                    mBookDetails.getBook(),
                                    // (To user) is current user because current user is not the owner of the book yet
                                    // and the broadcast arrives for the processes that only
                                    // between current user and the opposite user
                                    SessionManager.getCurrentUser(context),
                                    // Requester is current user
                                    notification.getOppositeUser(),
                                    Request.Type.REJECT,
                                    notification.getCreatedAt());

                                mBookDetails.getBookProcesses().add(request);

                                mBookTimelineAdapter.notifyItemInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex());
                                // Top book process must be refreshed for timeline divider.
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 1);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Rejected request received from FCM: " + notification +
                                             "\n\n Request added to adapter: " + request);
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){

                                Request request = new Request(
                                    mBookDetails.getBook(),
                                    // (To user) is current user because current user is not the owner of the book yet
                                    // and the broadcast arrives for the processes that only
                                    // between current user and the opposite user
                                    SessionManager.getCurrentUser(context),
                                    // Requester is current user
                                    notification.getOppositeUser(),
                                    Request.Type.ACCEPT,
                                    notification.getCreatedAt());

                                mBookDetails.getBookProcesses().add(request);

                                Transaction transaction = new Transaction(
                                    mBookDetails.getBook(),
                                    mBookDetails.getBook().getOwner(),
                                    SessionManager.getCurrentUser(context),
                                    Transaction.Type.DISPACTH,
                                    notification.getCreatedAt());

                                mBookDetails.getBookProcesses().add(transaction);

                                mBookDetails.getBook().setState(Book.State.ON_ROAD);
                                mBook.setState(Book.State.ON_ROAD);

                                mBookTimelineAdapter.notifyItemRangeInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex(), 2);
                                // Top book process must be refreshed for timeline divider.
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 2);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Rejected request received from FCM: " + notification +
                                             "\n\nRequest added to adapter: " + request +
                                             "\n\nTransaction added to adapter:" + transaction +
                                             "\n\nBook state changed to: " + Book.State.ON_ROAD);
                            }
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);
                        if (mBookDetails != null){

                            // TODO notification.getBook().getState is always ON_ROAD

                            Transaction transaction = new Transaction(
                                mBookDetails.getBook(),
                                // Giver is current user
                                SessionManager.getCurrentUser(context),
                                notification.getOppositeUser(),
                                Transaction.Type.COME_TO_HAND,
                                notification.getCreatedAt());

                            mBookDetails.getBookProcesses().add(transaction);

                            Interaction.Type addedInteractionType;
                            switch (notification.getBook().getState()) {

                                case READING:
                                    addedInteractionType = Interaction.Type.READ_START;
                                    break;

                                case OPENED_TO_SHARE:
                                    addedInteractionType = Interaction.Type.OPEN_TO_SHARE;
                                    break;

                                case CLOSED_TO_SHARE:
                                    addedInteractionType = Interaction.Type.CLOSE_TO_SHARE;
                                    break;

                                default: // In case of WTF
                                    addedInteractionType = Interaction.Type.CLOSE_TO_SHARE;
                            }

                            Interaction interaction = new Interaction(
                                mBookDetails.getBook(),
                                notification.getOppositeUser(),
                                addedInteractionType,
                                notification.getCreatedAt());

                            mBookDetails.getBookProcesses().add(interaction);

                            mBookDetails.getBook().setOwner(notification.getOppositeUser());
                            mBook.setOwner(notification.getOppositeUser());

                            mBookDetails.getBook().setState(notification.getBook().getState());
                            mBook.setState(notification.getBook().getState());

                            mBookTimelineAdapter.notifyItemRangeInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex(), 2);
                            mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 2);
                            mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                            Logger.d("Book owner changed received from FCM: " + notification +
                                         "\n\nTransaction added to adapter: " + transaction +
                                         "\n\nInteraction added to adapter: " + interaction +
                                         "\n\nOwner changed to: " + notification.getOppositeUser() +
                                         "\n\nBook state changed to: " + notification.getBook().getState());
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_LOST)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);
                        if (mBookDetails != null){
                            if (notification.getBook().equals(mBookDetails.getBook())){

                                Transaction transaction = new Transaction(
                                    mBookDetails.getBook(),
                                    // (To user) is current user because current user is not the owner of the book yet
                                    // and the broadcast arrives for the processes that only
                                    // between current user and the opposite user
                                    notification.getOppositeUser(),
                                    SessionManager.getCurrentUser(context),
                                    Transaction.Type.LOST,
                                    notification.getCreatedAt());

                                mBookDetails.getBookProcesses().add(transaction);

                                mBookDetails.getBook().setState(Book.State.LOST);
                                mBook.setState(Book.State.LOST);

                                mBookTimelineAdapter.notifyItemInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex());
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 1);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Book lost received from FCM: " + notification +
                                             "\n\nTransaction added to adapter: " + transaction +
                                             "\n\nBook state changed to: " + Book.State.LOST);
                            }
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_REQUEST) != null){
                        Request request = intent.getParcelableExtra(BookieIntentFilters.EXTRA_REQUEST);
                        if (mBookDetails != null){
                            if (request.getBook().equals(mBookDetails.getBook())){

                                mBookDetails.getBookProcesses().add(request);

                                Transaction transaction = new Transaction(
                                    mBookDetails.getBook(),
                                    // (To user) is current user because current user is not the owner of the book yet
                                    // and the broadcast arrives for the processes that only
                                    // between current user and the opposite user
                                    SessionManager.getCurrentUser(context),
                                    request.getRequester(),
                                    Transaction.Type.DISPACTH,
                                    request.getCreatedAt());

                                mBookDetails.getBookProcesses().add(transaction);

                                mBookTimelineAdapter.notifyItemRangeInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex(), 2);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 2);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Accepted request received from Local Broadcast: " + request +
                                             "\n\nTransaction added to adapter: " + transaction);

                                // Generally updates book object in ProfileFragment
                                Intent newIntent = new Intent(BookieIntentFilters.INTENT_FILTER_BOOK_STATE_CHANGED);
                                Book book = mBookDetails.getBook();
                                book.setState(Book.State.ON_ROAD);
                                newIntent.putExtra(BookieIntentFilters.EXTRA_BOOK, book);
                                LocalBroadcastManager.getInstance(BookActivity.this).sendBroadcast(newIntent);
                            }
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_REQUEST) != null){
                        Request request = intent.getParcelableExtra(BookieIntentFilters.EXTRA_REQUEST);
                        if (mBookDetails != null){
                            if (request.getBook().equals(mBookDetails.getBook())){

                                mBookDetails.getBookProcesses().add(request);

                                mBookTimelineAdapter.notifyItemInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex());
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 1);
                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());

                                Logger.d("Book owner changed received from Local Broadcast: " + request);
                            }
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_UPDATED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_BOOK) != null){
                        Book book = intent.getParcelableExtra(BookieIntentFilters.EXTRA_BOOK);
                        if (mBookDetails != null){
                            if (book.equals(mBookDetails.getBook())){

                                mBookDetails.getBook().setName(book.getName());
                                mBookDetails.getBook().setAuthor(book.getAuthor());
                                mBookDetails.getBook().setGenreCode(book.getGenreCode());

                                mBook.setName(book.getName());
                                mBook.setAuthor(book.getAuthor());
                                mBook.setGenreCode(book.getGenreCode());

                                mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getHeaderIndex());

                                Logger.d("Book updated received from Local Broadcast: " + book);
                            }
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_LOST)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_BOOK) != null){
                        Book book = intent.getParcelableExtra(BookieIntentFilters.EXTRA_BOOK);
                        if (mBookDetails != null){
                            if (book.equals(mBookDetails.getBook())){

                                int i = mBookDetails.getBookProcesses().size();
                                Transaction lastDispatch = null;
                                do {

                                    i--;

                                    if (mBookDetails.getBookProcesses().get(i) instanceof Transaction) {
                                        lastDispatch = (Transaction) mBookDetails.getBookProcesses().get(i);
                                    }

                                } while (!(mBookDetails.getBookProcesses().get(i) instanceof Transaction) && i > 0);

                                if (lastDispatch != null && lastDispatch.getType() == Transaction.Type.DISPACTH) {
                                    Transaction transaction = new Transaction(
                                        mBookDetails.getBook(),
                                        // (To user) is current user because current user is not the owner of the book yet
                                        // and the broadcast arrives for the processes that only
                                        // between current user and the opposite user
                                        SessionManager.getCurrentUser(context),
                                        lastDispatch.getTaker(),
                                        Transaction.Type.LOST,
                                        Calendar.getInstance());

                                    mBookDetails.getBookProcesses().add(transaction);

                                    mBookDetails.getBook().setState(Book.State.LOST);
                                    mBook.setState(Book.State.LOST);

                                    Logger.d("Book lost received from Local Broadcast: " + book +
                                                 "\n\nTransaction added to adapter: " + transaction);

                                    mBookTimelineAdapter.notifyItemInserted(mBookTimelineAdapter.getBeginningOfBookProcessesIndex());
                                    mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBeginningOfBookProcessesIndex() + 1);
                                    mBookTimelineAdapter.notifyItemChanged(mBookTimelineAdapter.getBookStateIndex());
                                }
                            }
                        }
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_LOST));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_LOST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
                    intent.putExtra(BookSettingsActivity.EXTRA_BOOK, mBook);
                    User currentUser = SessionManager.getCurrentUser(this);
                    boolean isAdder = mBookDetails.getAddedBy().equals(currentUser);
                    intent.putExtra(BookSettingsActivity.EXTRA_IS_ADDER, isAdder);
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

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == LocationActivity.RESULT_LOCATION_UPDATED) {
                if (mLocationInfoDialog != null && mLocationInfoDialog.isShowing()) {
                    mLocationInfoDialog.dismiss();
                }
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

                    Interaction openToShare = new Interaction(mBook,
                                                              currentUser,
                                                              Interaction.Type.OPEN_TO_SHARE,
                                                              Calendar.getInstance());

                    if (mBook.getState() == Book.State.READING) {

                        Interaction stopReading = new Interaction(mBook,
                                                                  currentUser,
                                                                  Interaction.Type.READ_STOP,
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
                }
            };

            View.OnClickListener closeToShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                    Interaction closeToShare = new Interaction(mBook,
                                                               currentUser,
                                                               Interaction.Type.CLOSE_TO_SHARE,
                                                               Calendar.getInstance());

                    if (mBook.getState() == Book.State.READING) {

                        Interaction stopReading = new Interaction(mBook,
                                                                  currentUser,
                                                                  Interaction.Type.READ_STOP,
                                                                  Calendar.getInstance());

                        mBookDetails.getBookProcesses().add(stopReading);
                    }

                    mBookDetails.getBookProcesses().add(closeToShare);
                    mBookTimelineAdapter.notifyItemRangeChanged(3, mBookDetails.getBookProcesses().size());

                    mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                    mBook.setState(Book.State.CLOSED_TO_SHARE);
                    mBookTimelineAdapter.notifyItemChanged(1);

                    bookProcessToServer(closeToShare);

                    dismiss();
                }
            };

            View.OnClickListener startReadingListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBookDetails.getBook().setState(Book.State.READING);
                    mBook.setState(Book.State.READING);

                    Interaction startReading = new Interaction(mBook,
                                                               SessionManager.getCurrentUser(BookActivity.this),
                                                               Interaction.Type.READ_START,
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

    private void bookProcessToServer(Book.BookProcess bookProcess) {

        bookProcess.accept(new Book.TimelineDisplayableVisitor() {
            @Override
            public void visit(Interaction interaction) {addBookInteractionToServer(interaction);}

            @Override
            public void visit(Transaction transaction) {}

            @Override
            public void visit(Request request) {
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

        Logger.d("getBookPageArguments() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tbookID=" + mBook.getID());

        getBookPageArguments.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

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

                                    Logger.d("Book page fetched: " + "\n\n" + mBook + "\n\n" +
                                                 "BookProcesses:\n" + mBookDetails.getBookProcesses());
                                }
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Logger.e("Response body is null on fetchBookPageArguments(). (Book Page Error)");
                            mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Logger.e("Response object is null on fetchBookPageArguments. (Book Page Error)");
                        mBookTimelineAdapter.setError(BookTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    if(BookieApplication.hasNetwork()){
                        mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else{
                        mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Logger.e("getBookPageArguments Failure: " + t.getMessage());

                if(BookieApplication.hasNetwork()){
                    mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                }else{
                    mBookTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                }

                mPullRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void addBookInteractionToServer(final Interaction interaction) {

        final ComfortableProgressDialog comfortableProgressDialog = new ComfortableProgressDialog(BookActivity.this);
        comfortableProgressDialog.setMessage(getString(R.string.updating_process));
        comfortableProgressDialog.show();

        final BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> addBookInteraction = bookApi.addBookInteraction(email,
                                                                           password,
                                                                           interaction.getBook().getID(),
                                                                           interaction.getType().getInteractionCode());

        Logger.d("addBookInteraction() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tbookID=" + interaction.getBook().getID() +
                     ", \n\tinteractionCode=" + interaction.getType().getInteractionCode());


        addBookInteraction.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                                Logger.d("Interaction added to server: " + interaction);

                                Intent data = new Intent(BookieIntentFilters.INTENT_FILTER_BOOK_STATE_CHANGED);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(BookieIntentFilters.EXTRA_BOOK,mBookDetails.getBook());
                                data.putExtras(bundle);
                                LocalBroadcastManager.getInstance(BookActivity.this).sendBroadcast(data);

                                User currentUser = SessionManager.getCurrentUser(BookActivity.this);

                                if (interaction.getBook().getOwner().equals(currentUser)){
                                    mBookDetails.getBook().setOwner(currentUser);
                                    if (interaction.getType() == Interaction.Type.CLOSE_TO_SHARE){
                                        mBook.setState(Book.State.CLOSED_TO_SHARE);
                                        mBookDetails.getBook().setState(Book.State.CLOSED_TO_SHARE);
                                    }else if (interaction.getType() == Interaction.Type.OPEN_TO_SHARE){
                                        mBook.setState(Book.State.OPENED_TO_SHARE);
                                        mBookDetails.getBook().setState(Book.State.OPENED_TO_SHARE);
                                    }else if (interaction.getType() == Interaction.Type.READ_START){
                                        mBook.setState(Book.State.READING);
                                        mBookDetails.getBook().setState(Book.State.READING);
                                    }
                                    mBookTimelineAdapter.notifyDataSetChanged();
                                }

                                comfortableProgressDialog.dismiss();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                comfortableProgressDialog.dismiss();
                                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null addBookInteractionToServer(). (Book Page Error)");
                            comfortableProgressDialog.dismiss();
                            Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null addBookInteractionToServer(). (Book Page Error)");
                        comfortableProgressDialog.dismiss();
                        Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                    comfortableProgressDialog.dismiss();
                    Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("addBookInteraction onFailure: " + t.getMessage());
                comfortableProgressDialog.dismiss();
                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookRequestToServer(final Request request) {

        final ComfortableProgressDialog comfortableProgressDialog = new ComfortableProgressDialog(BookActivity.this);
        comfortableProgressDialog.setMessage(getString(R.string.updating_process));
        comfortableProgressDialog.show();

        final BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> addBookRequest = bookApi.addBookRequest(email,
                                                                   password,
                                                                   request.getBook().getID(),
                                                                   request.getRequester().getID(),
                                                                   request.getResponder().getID(),
                                                                   request.getType().getRequestCode());

        Logger.d("addBookRequest() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tbookID=" + request.getBook().getID() +
                     ", \n\trequesterID=" + request.getRequester().getID() + ", \n\tresponderID=" + request.getResponder().getID() +
                     ", \n\trequestType=" + request.getType().getRequestCode());

        addBookRequest.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (mBookDetails != null){
                                    mBookDetails.getBookProcesses().add(request);

                                    Logger.d("Request added to server and to Book Timeline:\n" + request);

                                    mBookTimelineAdapter.notifyDataSetChanged();
                                }
                                comfortableProgressDialog.dismiss();
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.USER_NOT_VERIFIED){
                                    Logger.w("User not valid. (Book Page Error)");

                                    // Unverified email information dialog

                                    final InformationDialog informationDialog = new InformationDialog(BookActivity.this);
                                    informationDialog.setCancelable(true);
                                    informationDialog.setPrimaryMessage(R.string.unverified_email_info_short);
                                    informationDialog.setSecondaryMessage(R.string.unverified_email_request_info);
                                    informationDialog.setDefaultClickListener(new InformationDialog.DefaultClickListener() {
                                        @Override
                                        public void onOkClick() {
                                            informationDialog.dismiss();
                                            finish();
                                        }

                                        @Override
                                        public void onMoreInfoClick() {
                                            Intent intent = new Intent(BookActivity.this, InfoActivity.class);
                                            // TODO Put related header extras array
                                            startActivity(intent);
                                        }
                                    });
                                    informationDialog.setExtraButtonClickListener(R.string.check_email, new InformationDialog.ExtraButtonClickListener() {
                                        @Override
                                        public void onExtraButtonClick() {
                                            IntentHelper.openEmailClient(BookActivity.this);
                                        }
                                    });

                                    informationDialog.show();

                                }else if (errorCode == ErrorCodes.USER_BLOCKED){
                                    Logger.w("User blocked. (Book Page Error)");
                                    Toast.makeText(BookActivity.this, R.string.blocked_request_info, Toast.LENGTH_SHORT).show();

                                }else if (errorCode == ErrorCodes.LOCATION_NOT_FOUND){
                                    Logger.w("Location not found. (Book Page Error)");

                                    // Null location information dialog

                                    mLocationInfoDialog = new InformationDialog(BookActivity.this);
                                    mLocationInfoDialog.setCancelable(true);
                                    mLocationInfoDialog.setPrimaryMessage(R.string.null_location_info_short);
                                    mLocationInfoDialog.setSecondaryMessage(R.string.null_location_request_info);
                                    mLocationInfoDialog.setDefaultClickListener(new InformationDialog.DefaultClickListener() {
                                        @Override
                                        public void onOkClick() {
                                            mLocationInfoDialog.dismiss();
                                            finish();
                                        }

                                        @Override
                                        public void onMoreInfoClick() {
                                            Intent intent = new Intent(BookActivity.this, InfoActivity.class);
                                            // TODO Put related header extras array
                                            startActivity(intent);
                                        }
                                    });
                                    mLocationInfoDialog.setExtraButtonClickListener(R.string.set_location, new InformationDialog.ExtraButtonClickListener() {
                                        @Override
                                        public void onExtraButtonClick() {
                                            Intent intent = new Intent(BookActivity.this, LocationActivity.class);
                                            startActivityForResult(intent, REQUEST_CODE_LOCATION);                                        }
                                    });

                                    mLocationInfoDialog.show();

                                }else if (errorCode == ErrorCodes.BOOK_COUNT_INSUFFICIENT){
                                    Logger.w("Book count insufficient. (Book Page Error)");

                                    // Insufficient book count git

                                    final InformationDialog informationDialog = new InformationDialog(BookActivity.this);
                                    informationDialog.setCancelable(true);
                                    informationDialog.setPrimaryMessage(R.string.insufficient_book_count_info_short);
                                    informationDialog.setSecondaryMessage(R.string.insufficient_book_count_info);
                                    informationDialog.setDefaultClickListener(new InformationDialog.DefaultClickListener() {
                                        @Override
                                        public void onOkClick() {
                                            informationDialog.dismiss();
                                            finish();
                                        }

                                        @Override
                                        public void onMoreInfoClick() {
                                            Intent intent = new Intent(BookActivity.this, InfoActivity.class);
                                            // TODO Put related header extras array
                                            startActivity(intent);
                                        }
                                    });

                                    informationDialog.show();

                                }else {
                                    Logger.e("Error true in response: errorCode = " + errorCode);
                                }

                                comfortableProgressDialog.dismiss();
                                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Logger.e("Response body is null addBookRequestToServer(). (Book Page Error)");
                            comfortableProgressDialog.dismiss();
                            Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Logger.e("Response object is null addBookRequestToServer(). (Book Page Error)");
                        comfortableProgressDialog.dismiss();
                        Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                    comfortableProgressDialog.dismiss();
                    Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("addBookRequest Failure: " + t.getMessage());
                comfortableProgressDialog.dismiss();
                Toast.makeText(BookActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
