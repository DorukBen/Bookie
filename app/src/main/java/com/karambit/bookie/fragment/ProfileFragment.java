package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.BookActivity;
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
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String USER = "user";

    private User mUser;

    public static final int PROFILE_FRAGMENT_TAB_INEX = 3;
    private ProfileTimelineAdapter mProfileTimelineAdapter;
    private PullRefreshLayout mPullRefreshLayout;

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
                startActivity(intent);
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
                bundle.putString("image", details.getUser().getImageUrl());
                intent.putExtras(bundle);
                startActivity(intent);
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
        Call<ResponseBody> getUserProfilePageArguments = userApi.getUserProfilePageArguments(email, password, mUser.getID());

        getUserProfilePageArguments.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String json = response.body().string();

                    JSONObject responseObject = new JSONObject(json);
                    boolean error = responseObject.getBoolean("error");

                    if (!error) {
                        mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NONE);
                        User.Details userDetails ;
                        if (!responseObject.isNull("user")){
                            JSONObject userObject = responseObject.getJSONObject("user");
                            userDetails = User.jsonObjectToUserDetails(userObject);

                            if (!responseObject.isNull("currently_reading")){
                                if (userDetails != null) {
                                    userDetails.setCurrentlyReading(Book.jsonArrayToBookList(responseObject.getJSONArray("currently_reading")));
                                }

                            }
                            if (!responseObject.isNull("books_on_hand")){
                                if (userDetails != null) {
                                    userDetails.setBooksOnHand(Book.jsonArrayToBookList(responseObject.getJSONArray("books_on_hand")));
                                }

                            }
                            if (!responseObject.isNull("read_books")){
                                if (userDetails != null) {
                                    userDetails.setReadBooks(Book.jsonArrayToBookList(responseObject.getJSONArray("read_books")));
                                }
                            }
                            mProfileTimelineAdapter.setUserDetails(userDetails);
                        }

                    } else {
                        int errorCode = responseObject.getInt("error_code");

                        if(NetworkChecker.isNetworkAvailable(getContext())){
                            mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }else{
                            mProfileTimelineAdapter.setError(ProfileTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                        }
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
}
