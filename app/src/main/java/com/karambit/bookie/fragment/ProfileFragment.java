package com.karambit.bookie.fragment;


import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.LoginRegisterActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.PhotoViewerActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.ProfileTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.karambit.bookie.model.Book.State.READING;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final int UPDATE_PROFILE_PICTURE_REQUEST_CODE = 1;
    private static final int UPDATE_BOOK_PROCESS_REQUEST_CODE = 2;

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String USER = "user";

    private User mUser;

    public static final int PROFILE_FRAGMENT_TAB_INEX = 3;
    private ProfileTimelineAdapter mProfileTimelineAdapter;
    private PullRefreshLayout mPullRefreshLayout;
    private User.Details mUserDetails;

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

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mUser.getID() == SessionManager.getCurrentUser(getContext()).getID()){
            mProfileTimelineAdapter = new ProfileTimelineAdapter(getContext(), SessionManager.getCurrentUserDetails(getContext()));
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
                // TODO Start Reading Button
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

        recyclerView.setAdapter(mProfileTimelineAdapter);

        if (mUser.getID() == SessionManager.getCurrentUser(getContext()).getID()){
            recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), PROFILE_FRAGMENT_TAB_INEX));
        }else{
            recyclerView.setOnScrollListener(new ElevationScrollListener((ProfileActivity) getActivity()));
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
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mPullRefreshLayout.setRefreshing(true);
        fetchProfilePageArguments();
        return rootView;
    }

    private void fetchProfilePageArguments() {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);
        String email = SessionManager.getCurrentUserDetails(getContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getContext()).getPassword();
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
            if (resultCode == BookActivity.BOOK_PROCESS_CHANGED_RESULT_CODE){
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
                                    break;
                                }

                                case CLOSED_TO_SHARE:{
                                    break;
                                }
                            }
                        }
                    }else if (mUserDetails.getCurrentlyReading().contains(book)){
                        if (book != null) {
                            switch (book.getState()){
                                case READING:{
                                    break;
                                }

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

                    mProfileTimelineAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
