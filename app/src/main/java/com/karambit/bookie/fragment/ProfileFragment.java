package com.karambit.bookie.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.karambit.bookie.AddBookActivity;
import com.karambit.bookie.BookActivity;
import com.karambit.bookie.BookieApplication;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.PhotoViewerActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.ProfileTimelineAdapter;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;
import com.karambit.bookie.service.BookieIntentFilters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.karambit.bookie.model.Book.State.CLOSED_TO_SHARE;
import static com.karambit.bookie.model.Book.State.ON_ROAD;
import static com.karambit.bookie.model.Book.State.OPENED_TO_SHARE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    public static final int TAB_INDEX = 3;
    public static final int VIEW_PAGER_INDEX = 2;
    public static final String TAB_SPEC = "tab_profile";
    public static final String TAB_INDICATOR = "tab3";

    private static final int REQUEST_CODE_ADD_BOOK_ACTIVITY = 3;

    private static final int UPDATE_PROFILE_PICTURE_REQUEST_CODE = 1;
    private static final int UPDATE_BOOK_PROCESS_REQUEST_CODE = 2;

    private static final String EXTRA_USER = "user";

    private User mUser;

    private DBManager mDbManager;
    private ProfileTimelineAdapter mProfileTimelineAdapter;
    private PullRefreshLayout mPullRefreshLayout;
    private User.Details mUserDetails;
    private BroadcastReceiver mMessageReceiver;
    private RecyclerView mProfileRecyclerView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(EXTRA_USER);
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProfileRecyclerView = (RecyclerView) rootView.findViewById(R.id.profileRecyclerView);
        mProfileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDbManager = new DBManager(getContext());
        mDbManager.open();

        User currentUser = SessionManager.getCurrentUser(getContext());

        if (mUser.equals(currentUser)){
            mProfileTimelineAdapter = new ProfileTimelineAdapter(getContext(), SessionManager.getCurrentUserDetails(getContext()));
        }else {
            mProfileTimelineAdapter = new ProfileTimelineAdapter(getContext(), mUser);
        }

        mProfileTimelineAdapter.setBookClickListener(new ProfileTimelineAdapter.BookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra(BookActivity.EXTRA_BOOK, book);
                startActivityForResult(intent, UPDATE_BOOK_PROCESS_REQUEST_CODE);
            }
        });

        mProfileTimelineAdapter.setStartReadingClickListener(new ProfileTimelineAdapter.StartReadingClickListener() {
            @Override
            public void onStartReadingClick(User.Details userDetails) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_start_reading, null);
                builder.setView(dialogView);
                final AlertDialog startReadingDialog = builder.create();

                Button addBookButton = (Button) dialogView.findViewById(R.id.addYourBook);
                Button searchBookButton = (Button) dialogView.findViewById(R.id.searchYourBook);
                Button existingBook = (Button) dialogView.findViewById(R.id.existingBook);
                View existingBookDivider = dialogView.findViewById(R.id.existingBookDivider);

                addBookButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startReadingDialog.dismiss();
                        startActivityForResult(new Intent(getActivity(), AddBookActivity.class), REQUEST_CODE_ADD_BOOK_ACTIVITY);
                    }
                });

                searchBookButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startReadingDialog.dismiss();
                        ((MainActivity) getActivity()).setCurrentPage(1);
                    }
                });

                if (mUserDetails.getBooksOnHandCount() > 0) {

                    existingBook.setVisibility(View.VISIBLE);
                    existingBookDivider.setVisibility(View.VISIBLE);

                    existingBook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startReadingDialog.dismiss();

                            int firstBookOnHandIndex = mProfileTimelineAdapter.getFirstBookOnHandIndex();
                            LinearLayoutManager layoutManager = (LinearLayoutManager) mProfileRecyclerView.getLayoutManager();

                            // scrollToPositionWithOffset() is scrolling to position and aligns item to top unlike other scroll methods
                            layoutManager.scrollToPositionWithOffset(firstBookOnHandIndex - 1, 0);
                        }
                    });

                } else {
                    existingBook.setVisibility(View.GONE);
                    existingBookDivider.setVisibility(View.GONE);
                }

                startReadingDialog.show();
            }
        });

        mProfileTimelineAdapter.setHeaderClickListeners(new ProfileTimelineAdapter.HeaderClickListeners() {
            @Override
            public void onProfilePictureClick(User.Details details) {
                Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(PhotoViewerActivity.EXTRA_USER, details.getUser());
                bundle.putString(PhotoViewerActivity.EXTRA_IMAGE, details.getUser().getImageUrl());
                intent.putExtras(bundle);
                startActivityForResult(intent, UPDATE_PROFILE_PICTURE_REQUEST_CODE);
            }

            @Override
            public void onLocationClick(User.Details details) {

            }
        });

        mProfileRecyclerView.setAdapter(mProfileTimelineAdapter);

        if (mUser.equals(currentUser)){
            mProfileRecyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), TAB_INDEX));
        }else{
            mProfileRecyclerView.setOnScrollListener(new ElevationScrollListener((ProfileActivity) getActivity()));
        }

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                fetchProfilePageArguments();
            }
        });

        mPullRefreshLayout.setRefreshing(true);
        fetchProfilePageArguments();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);

                        int onRoadBookCountBeforeAdding = mUserDetails.getOnRoadBooks().size();

                        mUserDetails.getOnRoadBooks().add(0, notification.getBook());

                        mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstOnRoadBookIndex());

                        Log.i(TAG, "Accepted Request Received: notifyItemRemoved called to position " +
                            mProfileTimelineAdapter.getFirstOnRoadBookIndex() + " for OnRoadBooks");

                        if (onRoadBookCountBeforeAdding == 0) {
                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(false));

                            Log.i(TAG, "Accepted Request Received: notifyItemInserted called to position " +
                                mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(false) + " for OnRoadBooks Subtitle");
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);

                        int removedBookIndex = mUserDetails.getBooksOnHand().indexOf(notification.getBook());
                        int firstBookOnHandIndexBeforeRemoving = mProfileTimelineAdapter.getFirstBookOnHandIndex();

                        mUserDetails.getBooksOnHand().remove(notification.getBook());
                        mUserDetails.setPoint(mUserDetails.getPoint() + User.POINT_SHARE_BOOK);
                        mUserDetails.setSharedPoint(mUserDetails.getSharedPoint() + 1);

                        mProfileTimelineAdapter.notifyItemRemoved(firstBookOnHandIndexBeforeRemoving + removedBookIndex);

                        Log.i(TAG, "Owner changed: notifyItemRemoved called to position " +
                            (firstBookOnHandIndexBeforeRemoving + removedBookIndex) + " for BooksOnHand");

                        mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getHeaderIndex());

                        Log.i(TAG, "Owner changed: notifyItemChanged called to position " +
                            mProfileTimelineAdapter.getHeaderIndex() + " for Header");

                        if (mUserDetails.getBooksOnHand().size() == 0){
                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(true));
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_LOST)){
                    if (intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION) != null){
                        Notification notification = intent.getParcelableExtra(BookieIntentFilters.EXTRA_NOTIFICATION);

                        int removedBookIndex = mUserDetails.getBooksOnHand().indexOf(notification.getBook());
                        int firstBookOnHandIndexBeforeRemoving = mProfileTimelineAdapter.getFirstBookOnHandIndex();

                        mUserDetails.getOnRoadBooks().remove(notification.getBook());
                        mUserDetails.setPoint(mUserDetails.getPoint() + User.POINT_LOST);

                        mProfileTimelineAdapter.notifyItemRemoved(firstBookOnHandIndexBeforeRemoving + removedBookIndex);

                        Log.i(TAG, "Book lost: notifyItemRemoved called to position " +
                            (firstBookOnHandIndexBeforeRemoving + removedBookIndex) + " for BooksOnHand");

                        mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getHeaderIndex());

                        Log.i(TAG, "Book lost: notifyItemChanged called to position " +
                            mProfileTimelineAdapter.getHeaderIndex() + " for Header");

                        if (mUserDetails.getBooksOnHand().size() == 0){
                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(true));
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_STATE_CHANGED)){
                    if (mUserDetails != null){
                        Book book = intent.getParcelableExtra(BookieIntentFilters.EXTRA_BOOK);
                        if (book != null) {
                            if (mUserDetails.getBooksOnHand().contains(book)){

                                switch (book.getState()){
                                    case READING:{
                                        int removedBookIndex = mUserDetails.getBooksOnHand().indexOf(book);
                                        int firstBookOnHandIndexBeforeRemoving = mProfileTimelineAdapter.getFirstBookOnHandIndex();

                                        mUserDetails.getBooksOnHand().remove(book);
                                        mUserDetails.getCurrentlyReading().add(book);

                                        mProfileTimelineAdapter.notifyItemRemoved(firstBookOnHandIndexBeforeRemoving + removedBookIndex);

                                        Log.i(TAG, "State change: (OpenedToShare-ClosedToShare) -> Reading. notifyItemRemoved called to position " +
                                            (firstBookOnHandIndexBeforeRemoving + removedBookIndex) + " for BooksOnHand");

                                        if (mUserDetails.getBooksOnHand().size() == 0) {
                                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(true));

                                            Log.i(TAG, "State change: (OpenedToShare-ClosedToShare) -> Reading. notifyItemRemoved called to position " +
                                                mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(true) + " for BooksOnHand Subtitle");
                                        }

                                        break;
                                    }

                                    case OPENED_TO_SHARE:{
                                        int changedBookIndex = mUserDetails.getBooksOnHand().indexOf(book);

                                        mUserDetails.getBooksOnHand().get(changedBookIndex).setState(OPENED_TO_SHARE);

                                        mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex);

                                        Log.i(TAG, "State change: ClosedToShare -> OpenedToShare. notifyItemChanged called to position " +
                                            (mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex) + " for BooksOnHand");

                                        break;
                                    }

                                    case CLOSED_TO_SHARE:{
                                        int changedBookIndex = mUserDetails.getBooksOnHand().indexOf(book);

                                        mUserDetails.getBooksOnHand().get(changedBookIndex).setState(CLOSED_TO_SHARE);

                                        mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex);

                                        Log.i(TAG, "State change: OpenedToShare -> ClosedToShare. notifyItemChanged called to position " +
                                            (mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex) + " for BooksOnHand");

                                        break;
                                    }

                                    case ON_ROAD:{
                                        int changedBookIndex = mUserDetails.getBooksOnHand().indexOf(book);

                                        mUserDetails.getBooksOnHand().get(changedBookIndex).setState(ON_ROAD);

                                        mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex);

                                        Log.i(TAG, "State change: OpenedToShare -> OnRoad. notifyItemChanged called to position " +
                                            (mProfileTimelineAdapter.getFirstBookOnHandIndex() + changedBookIndex) + " for BooksOnHand");

                                        break;
                                    }
                                }
                            } else if (mUserDetails.getCurrentlyReading().contains(book)){

                                // Finished reading
                                mUserDetails.setPoint(mUserDetails.getPoint() + User.POINT_READ_FINISHED);

                                mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getHeaderIndex());

                                switch (book.getState()){

                                    case OPENED_TO_SHARE:{

                                        int booksOnHandCountBeforeAdding = mUserDetails.getBooksOnHand().size();
                                        int firstBookOnHandIndexBeforeRemoving = mProfileTimelineAdapter.getFirstBookOnHandIndex();

                                        mUserDetails.getCurrentlyReading().remove(book);

                                        mUserDetails.getBooksOnHand().add(0, book);

                                        mProfileTimelineAdapter.notifyItemInserted(firstBookOnHandIndexBeforeRemoving);

                                        Log.i(TAG, "State change: Reading -> OpenedToShare. notifyItemInserted at position " +
                                            firstBookOnHandIndexBeforeRemoving + " for BooksOnHand");

                                        if (booksOnHandCountBeforeAdding == 0) {
                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false));

                                            Log.i(TAG, "State change: Reading -> OpenedToShare. notifyItemInserted at position " +
                                                mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false) + " for BooksOnHand Subtitle");
                                        }

                                        if (!mUserDetails.getReadBooks().contains(book)){

                                            int readBooksCountBeforeAdding = mUserDetails.getReadBooks().size();

                                            mUserDetails.getReadBooks().add(0, book);

                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstReadBookIndex());

                                            Log.i(TAG, "State change: Reading -> OpenedToShare. notifyItemInserted at position " +
                                                mProfileTimelineAdapter.getFirstReadBookIndex() + " for ReadBooks");

                                            if (readBooksCountBeforeAdding == 0) {
                                                mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getReadBooksSubtitleIndex(false));

                                                Log.i(TAG, "State change: Reading -> OpenedToShare. notifyItemInserted at position " +
                                                    mProfileTimelineAdapter.getReadBooksSubtitleIndex(false) + " for ReadBooks Subtitle");
                                            }
                                        }

                                        break;
                                    }

                                    case CLOSED_TO_SHARE:{
                                        int booksOnHandCountBeforeAdding = mUserDetails.getBooksOnHand().size();

                                        mUserDetails.getCurrentlyReading().remove(book);
                                        mUserDetails.getBooksOnHand().add(0, book);

                                        mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstBookOnHandIndex());

                                        Log.i(TAG, "State change: Reading -> ClosedToShare. notifyItemInserted at position " +
                                            mProfileTimelineAdapter.getFirstBookOnHandIndex() + " for BooksOnHand");


                                        if (booksOnHandCountBeforeAdding == 0) {
                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false));

                                            Log.i(TAG, "State change: Reading -> ClosedToShare. notifyItemInserted at position " +
                                                (mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false)) + " for BooksOnHand Subtitle");
                                        }

                                        if (!mUserDetails.getReadBooks().contains(book)){

                                            int readBooksCountBeforeAdding = mUserDetails.getReadBooks().size();

                                            mUserDetails.getReadBooks().add(0, book);

                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstReadBookIndex());

                                            Log.i(TAG, "State change: Reading -> ClosedToShare. notifyItemInserted at position " +
                                                (mProfileTimelineAdapter.getFirstReadBookIndex() + readBooksCountBeforeAdding) + " for ReadBooks");

                                            if (readBooksCountBeforeAdding == 0) {
                                                mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getReadBooksSubtitleIndex(false));

                                                Log.i(TAG, "State change: Reading -> ClosedToShare. notifyItemInserted at position " +
                                                    (mProfileTimelineAdapter.getReadBooksSubtitleIndex(false)) + " for ReadBooks");
                                            }
                                        }
                                        break;
                                    }

                                    case ON_ROAD:{
                                        int onRoadBooksCountBeforeAdding = mUserDetails.getOnRoadBooks().size();

                                        mUserDetails.getCurrentlyReading().remove(book);
                                        mUserDetails.getOnRoadBooks().add(0, book);

                                        mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstOnRoadBookIndex());

                                        Log.i(TAG, "State change: Reading -> OnRoad. notifyItemInserted at position " +
                                            mProfileTimelineAdapter.getFirstOnRoadBookIndex() + " for OnRoadBooks");

                                        if (onRoadBooksCountBeforeAdding == 0) {
                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(false));

                                            Log.i(TAG, "State change: Reading -> OnRoad. notifyItemInserted at position " +
                                                mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(false) + " for OnRoadBooks Subtitle");
                                        }

                                        if (!mUserDetails.getReadBooks().contains(book)){
                                            int readBooksCountBeforeAdding = mUserDetails.getReadBooks().size();

                                            mUserDetails.getReadBooks().add(0, book);

                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getFirstReadBookIndex());

                                            Log.i(TAG, "State change: Reading -> OnRoad. notifyItemInserted at position " +
                                                mProfileTimelineAdapter.getFirstReadBookIndex() + " for ReadBooks");

                                            if (readBooksCountBeforeAdding == 0) {
                                                mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getReadBooksSubtitleIndex(false));

                                                Log.i(TAG, "State change: Reading -> OnRoad. notifyItemInserted at position " +
                                                    mProfileTimelineAdapter.getReadBooksSubtitleIndex(false) + " for ReadBooks Subtitle");
                                            }
                                        }

                                        break;
                                    }
                                }
                            }else if (mUserDetails.getOnRoadBooks().contains(book)){

                                mUserDetails.setPoint(mUserDetails.getPoint() + User.POINT_BOOK_COME_TO_HAND);

                                switch (book.getState()){

                                    case READING:{
                                        int removedBookIndex = mUserDetails.getOnRoadBooks().indexOf(book);

                                        int firstOnRoadBookIndexBeforeRemoving = mProfileTimelineAdapter.getFirstOnRoadBookIndex();

                                        mUserDetails.getOnRoadBooks().remove(book);
                                        mUserDetails.getCurrentlyReading().add(0, book);

                                        mProfileTimelineAdapter.notifyItemRemoved(firstOnRoadBookIndexBeforeRemoving + removedBookIndex);

                                        Log.i(TAG, "State change: OnRoad -> Reading. notifyItemRemoved at position " +
                                            (firstOnRoadBookIndexBeforeRemoving + removedBookIndex) + " for OnRoadBooks");

                                        if (mUserDetails.getOnRoadBooks().size() == 0){
                                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true));

                                            Log.i(TAG, "State change: OnRoad -> Reading. notifyItemRemoved at position " +
                                                (mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true)) + " for OnRoadBooks Subtitle");
                                        }

                                        break;
                                    }

                                    case OPENED_TO_SHARE:{
                                        int removedBookIndex = mUserDetails.getOnRoadBooks().indexOf(book);
                                        int booksOnHandCountBeforeAdding = mUserDetails.getBooksOnHand().size();
                                        int firstOnRoadBookIndexBeforeRemoving = mProfileTimelineAdapter.getFirstOnRoadBookIndex();

                                        mUserDetails.getOnRoadBooks().remove(book);
                                        mUserDetails.getBooksOnHand().add(0, book);

                                        mProfileTimelineAdapter.notifyItemMoved(firstOnRoadBookIndexBeforeRemoving + removedBookIndex,
                                                                                mProfileTimelineAdapter.getFirstBookOnHandIndex());


                                        Log.i(TAG, "State change: OnRoad -> OpenToShare. notifyItemMoved from position " +
                                            (firstOnRoadBookIndexBeforeRemoving + removedBookIndex) + " to position " +
                                            mProfileTimelineAdapter.getFirstBookOnHandIndex() + " for OnRoadBooks -> BooksOnHand");

                                        if (mUserDetails.getOnRoadBooks().size() == 0){
                                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true));

                                            Log.i(TAG, "State change: OnRoad -> OpenToShare. notifyItemRemoved at position " +
                                                (mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true)) + " for OnRoadBooks Subtitle");
                                        }

                                        if (booksOnHandCountBeforeAdding == 0){
                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false));

                                            Log.i(TAG, "State change: OnRoad -> OpenToShare. notifyItemInserted at position " +
                                                (mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false)) + " for BooksOnHand Subtitle");
                                        }

                                        break;
                                    }

                                    case CLOSED_TO_SHARE:{
                                        int removedBookIndex = mUserDetails.getOnRoadBooks().indexOf(book);
                                        int booksOnHandCountBeforeAdding = mUserDetails.getBooksOnHand().size();
                                        int firstOnRoadBookIndexBeforeRemoving = mProfileTimelineAdapter.getFirstOnRoadBookIndex();

                                        mUserDetails.getOnRoadBooks().remove(book);
                                        mUserDetails.getBooksOnHand().add(book);

                                        mProfileTimelineAdapter.notifyItemMoved(firstOnRoadBookIndexBeforeRemoving + removedBookIndex,
                                                                                mProfileTimelineAdapter.getFirstBookOnHandIndex() + booksOnHandCountBeforeAdding);

                                        Log.i(TAG, "State change: OnRoad -> CloseToShare. notifyItemMoved from position " +
                                            (firstOnRoadBookIndexBeforeRemoving + removedBookIndex) + " to position " +
                                            (mProfileTimelineAdapter.getFirstBookOnHandIndex() + booksOnHandCountBeforeAdding) + " for OnRoadBooks -> BooksOnHand");

                                        if (mUserDetails.getOnRoadBooks().size() == 0){
                                            mProfileTimelineAdapter.notifyItemRemoved(mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true));

                                            Log.i(TAG, "State change: OnRoad -> OpenToShare. notifyItemRemoved at position " +
                                                (mProfileTimelineAdapter.getOnRoadBookSubtitleIndex(true)) + " for OnRoadBooks Subtitle");
                                        }

                                        if (booksOnHandCountBeforeAdding == 0){
                                            mProfileTimelineAdapter.notifyItemInserted(mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false));

                                            Log.i(TAG, "State change: OnRoad -> OpenToShare. notifyItemInserted at position " +
                                                (mProfileTimelineAdapter.getBooksOnHandSubtitleIndex(false)) + " for BooksOnHand Subtitle");
                                        }

                                        break;
                                    }
                                }
                            } else {
                                Log.e(TAG, "Invalid book state on state changing!");
                            }
                        } else {
                            Log.e(TAG, "Null book in intent extra");
                        }
                    }
                }else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_USER_VERIFIED)){
                    //TODO When user verified
                }
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_STATE_CHANGED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_USER_VERIFIED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_LOST));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    private void fetchProfilePageArguments() {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> getUserProfilePageComponents = userApi.getUserProfilePageComponents(email, password, mUser.getID());

        getUserProfilePageComponents.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NONE);

                                if (!responseObject.isNull("userDetails")){
                                    JSONObject userObject = responseObject.getJSONObject("userDetails");
                                    mUserDetails = User.jsonObjectToUserDetails(userObject);

                                    // Updating local database
                                    User currentUser = SessionManager.getCurrentUser(getContext());
                                    if (mUser.equals(currentUser)) {
                                        // Update user info from fetched user
                                        mUser = mUserDetails.getUser();
                                        mDbManager.getUserDataSource().updateUserDetails(mUserDetails);
                                        SessionManager.updateCurrentUser(mUserDetails);
                                    }

                                    if (!responseObject.isNull("currentlyReading")){
                                        if (mUserDetails != null) {
                                            mUserDetails.setCurrentlyReading(Book.jsonArrayToBookList(responseObject.getJSONArray("currentlyReading")));
                                        }

                                    }
                                    if (!responseObject.isNull("booksOnHand")){
                                        if (mUserDetails != null) {
                                            mUserDetails.setBooksOnHand(Book.jsonArrayToBookList(responseObject.getJSONArray("booksOnHand")));
                                        }

                                    }
                                    if (!responseObject.isNull("readBooks")){
                                        if (mUserDetails != null) {
                                            mUserDetails.setReadBooks(Book.jsonArrayToBookList(responseObject.getJSONArray("readBooks")));
                                        }
                                    }

                                    if (!responseObject.isNull("onRoadBooks")){
                                        if (mUserDetails != null) {
                                            mUserDetails.setOnRoadBooks(Book.jsonArrayToBookList(responseObject.getJSONArray("onRoadBooks")));
                                        }
                                    }

                                    mProfileTimelineAdapter.setUserDetails(mUserDetails);
                                    mProfileTimelineAdapter.notifyDataSetChanged();
                                }

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Profile Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Profile Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Profile Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Profile Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Profile Page Error)");
                            mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Profile Page Error)");
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    if(BookieApplication.hasNetwork()){
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else{
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                if(BookieApplication.hasNetwork()){
                    mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                }else{
                    mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                }

                mPullRefreshLayout.setRefreshing(false);
                Log.e(TAG, "ProfilePage onFailure: " + t.getMessage());
            }
        });
    }

    public void refreshProfilePage(){
        fetchProfilePageArguments();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_PROFILE_PICTURE_REQUEST_CODE){
            if (resultCode == PhotoViewerActivity.RESULT_PROFILE_PICTURE_UPDATED){
                refreshProfilePage();
//                TODO mProfileTimelineAdapter.notifyItemChanged(mProfileTimelineAdapter.getHeaderIndex());
            }
        } else if (resultCode == AddBookActivity.RESULT_BOOK_CREATED) {
            refreshProfilePage();
        }
    }
}
