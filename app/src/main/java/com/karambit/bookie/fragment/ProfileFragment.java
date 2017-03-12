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
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.PhotoViewerActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.ProfileTimelineAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.NetworkChecker;
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

    private static final String USER = "user";

    private User mUser;

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
        args.putParcelable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(USER);
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProfileRecyclerView = (RecyclerView) rootView.findViewById(R.id.profileRecyclerView);
        mProfileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        User currentUser = SessionManager.getCurrentUser(getContext());

        if (mUser.equals(currentUser)){
            mProfileTimelineAdapter = new ProfileTimelineAdapter(getContext(),  SessionManager.getCurrentUserDetails(getContext()));
        }else {
            mProfileTimelineAdapter = new ProfileTimelineAdapter(getContext());
        }

        mProfileTimelineAdapter.setBookClickListener(new ProfileTimelineAdapter.BookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra("book", book);
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
                }

                startReadingDialog.show();
            }
        });

        mProfileTimelineAdapter.setHeaderClickListeners(new ProfileTimelineAdapter.HeaderClickListeners() {
            @Override
            public void onProfilePictureClick(User.Details details) {
                Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", details.getUser());
                bundle.putString("image", details.getUser().getImageUrl());
                intent.putExtras(bundle);
                startActivityForResult(intent, UPDATE_PROFILE_PICTURE_REQUEST_CODE);
            }

            @Override
            public void onLocationClick(User.Details details) {

            }
        });

        mProfileTimelineAdapter.setHasStableIds(true);

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

        //For improving recyclerviews performance
        mProfileRecyclerView.setItemViewCacheSize(20);
        mProfileRecyclerView.setDrawingCacheEnabled(true);
        mProfileRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mPullRefreshLayout.setRefreshing(true);
        fetchProfilePageArguments();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_SENT_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mUserDetails.getOnRoadBooks().add(notification.getBook());
                        mProfileTimelineAdapter.setUserDetails(mUserDetails);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED)){
                    if (intent.getParcelableExtra("notification") != null){
                        Notification notification = intent.getParcelableExtra("notification");
                        mUserDetails.getBooksOnHand().remove(notification.getBook());
                        mProfileTimelineAdapter.setUserDetails(mUserDetails);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_SENT_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_REJECTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED));

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
        Call<ResponseBody> getUserProfilePageComponents = userApi.getUserProfilePageComponents(email, password, mUser.getID());

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

                                    // Updating local database
                                    User currentUser = SessionManager.getCurrentUser(getContext());
                                    if (mUser.equals(currentUser)) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                synchronized (DBHandler.class) { // REQUIRED
                                                    DBHandler dbHandler = DBHandler.getInstance(getContext());
                                                    dbHandler.updateCurrentUser(mUserDetails);
                                                    SessionManager.updateCurrentUserFromDB(getContext());
                                                }
                                            }
                                        }).start();
                                    }

                                    if (!responseObject.isNull("onRoadBooks")){
                                        if (mUserDetails != null) {
                                            mUserDetails.setOnRoadBooks(Book.jsonArrayToBookList(responseObject.getJSONArray("onRoadBooks")));
                                        }
                                    }

                                    mProfileTimelineAdapter.setUserDetails(mUserDetails);
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

                    if(NetworkChecker.isNetworkAvailable(getContext())){
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else{
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                if(NetworkChecker.isNetworkAvailable(getContext())){
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
            }
        } else if (requestCode == UPDATE_BOOK_PROCESS_REQUEST_CODE){
            if (resultCode == BookActivity.RESULT_BOOK_PROCESS_CHANGED){
                if (mUserDetails != null){
                    Book book = data.getExtras().getParcelable("book");
                    if (mUserDetails.getBooksOnHand().contains(book)){
                        if (book != null) {
                            switch (book.getState()){
                                case READING:{
                                    mUserDetails.getBooksOnHand().remove(book);
                                    mUserDetails.getCurrentlyReading().add(book);
                                    break;
                                }

                                case OPENED_TO_SHARE:{
                                    mUserDetails.getBooksOnHand().get(mUserDetails.getBooksOnHand().indexOf(book)).setState(OPENED_TO_SHARE);
                                    break;
                                }

                                case CLOSED_TO_SHARE:{
                                    mUserDetails.getBooksOnHand().get(mUserDetails.getBooksOnHand().indexOf(book)).setState(CLOSED_TO_SHARE);
                                    break;
                                }

                                case ON_ROAD:{
                                    mUserDetails.getBooksOnHand().get(mUserDetails.getBooksOnHand().indexOf(book)).setState(ON_ROAD);
                                    break;
                                }
                            }
                        }
                    }else if (mUserDetails.getCurrentlyReading().contains(book)){
                        if (book != null) {
                            switch (book.getState()){

                                case OPENED_TO_SHARE:{
                                    mUserDetails.getCurrentlyReading().remove(book);
                                    mUserDetails.getBooksOnHand().add(book);
                                    if (!mUserDetails.getReadBooks().contains(book)){
                                        mUserDetails.getReadBooks().add(book);
                                    }
                                    break;
                                }

                                case CLOSED_TO_SHARE:{
                                    mUserDetails.getCurrentlyReading().remove(book);
                                    mUserDetails.getBooksOnHand().add(book);
                                    if (!mUserDetails.getReadBooks().contains(book)){
                                        mUserDetails.getReadBooks().add(book);
                                    }
                                    break;
                                }

                                case ON_ROAD:{
                                    mUserDetails.getCurrentlyReading().remove(book);
                                    mUserDetails.getBooksOnHand().add(book);
                                    if (!mUserDetails.getReadBooks().contains(book)){
                                        mUserDetails.getReadBooks().add(book);
                                    }
                                    break;
                                }
                            }
                        }
                    }else if (mUserDetails.getOnRoadBooks().contains(book)){
                        if (book != null) {
                            switch (book.getState()){
                                case READING:{
                                    mUserDetails.getOnRoadBooks().remove(book);
                                    mUserDetails.getCurrentlyReading().add(book);
                                    break;
                                }

                                case OPENED_TO_SHARE:{
                                    mUserDetails.getOnRoadBooks().remove(book);
                                    mUserDetails.getBooksOnHand().add(book);
                                    break;
                                }

                                case CLOSED_TO_SHARE:{
                                    mUserDetails.getOnRoadBooks().remove(book);
                                    mUserDetails.getBooksOnHand().add(book);
                                    break;
                                }
                            }
                        }
                    }
                    mProfileTimelineAdapter.setUserDetails(mUserDetails);
                }
            }
        } else if (resultCode == AddBookActivity.RESULT_BOOK_CREATED) {
            refreshProfilePage();
        }
    }
}
