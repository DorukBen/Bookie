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
import android.widget.Toast;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.BookieApplication;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.HomeTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookApi;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    public static final int TAB_INDEX = 0;
    public static final int VIEW_PAGER_INDEX = 0;
    public static final String TAB_SPEC = "tab_home";
    public static final String TAB_INDICATOR = "tab0";

    private ArrayList<Book> mHeaderBooks = new ArrayList<>();
    private ArrayList<Book> mListBooks = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private HomeTimelineAdapter mHomeTimelineAdapter;
    private PullRefreshLayout mPullRefreshLayout;
    private boolean mIsBooksFetching = false;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.homeRecyclerView);

        mRecyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), TAB_INDEX));

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isLastItemVisible() && !mIsBooksFetching){
                    fetchHomePageBooks(false);
                }
            }
        });

        mHomeTimelineAdapter = new HomeTimelineAdapter(getContext());

        mRecyclerView.setAdapter(mHomeTimelineAdapter);

        mHomeTimelineAdapter.setBookClickListener(new HomeTimelineAdapter.BookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra(BookActivity.EXTRA_BOOK, book);
                startActivity(intent);
            }
        });

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                if (!mIsBooksFetching){
                    mListBooks = new ArrayList<>();
                    fetchHomePageBooks(true);
                    ((MainActivity) getActivity()).fetchNotificationMenuItemValue();
                }
            }
        });

        ((MainActivity)getActivity()).setDoubleTapHomeButtonListener(new MainActivity.DoubleTapHomeButtonListener() {
            @Override
            public void onDoubleTapHomeButton() {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mPullRefreshLayout.setRefreshing(true);
        fetchHomePageBooks(true);
        return rootView;
    }

    boolean isLastItemVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager)mRecyclerView.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = mRecyclerView.getAdapter().getItemCount() - 1;

        return (pos >= numItems);
    }

    private void fetchHomePageBooks(final boolean isFromTop) {
        mIsBooksFetching = true;
        BookApi bookApi = BookieClient.getClient().create(BookApi.class);
        String fetchedBookIds;

        int i = 0;
        if (mListBooks.size() > 0){
            StringBuilder builder = new StringBuilder();
            for (Book book: mListBooks){
                builder.append(book.getID());
                if (i < mListBooks.size() - 1){
                    builder.append("_");
                }
                i++;
            }
            fetchedBookIds = builder.toString();
        }else{
            fetchedBookIds = "-1";
        }


        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> getHomePageBooks = bookApi.getHomePageBooks(email, password, fetchedBookIds);

        getHomePageBooks.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (mListBooks.size() > 0){
                                    JSONArray listBooksArray = responseObject.getJSONArray("listBooks");
                                    ArrayList<Book> feedBooks = Book.jsonArrayToBookList(listBooksArray);

                                    if (feedBooks.size() < 20){
                                        mHomeTimelineAdapter.setProgressBarActive(false);
                                    }else {
                                        mListBooks.addAll(feedBooks);

                                        mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_NONE);
                                        mHomeTimelineAdapter.addFeedBooks(feedBooks);
                                    }
                                }else {
                                    JSONArray headerBooksArray = responseObject.getJSONArray("headerBooks");
                                    mHeaderBooks.addAll(Book.jsonArrayToBookList(headerBooksArray));

                                    JSONArray listBooksArray = responseObject.getJSONArray("listBooks");
                                    ArrayList<Book> feedBooks = Book.jsonArrayToBookList(listBooksArray);
                                    mListBooks.addAll(feedBooks);

                                    mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_NONE);
                                    mHomeTimelineAdapter.setHeaderAndFeedBooks(mHeaderBooks, feedBooks);
                                }

                                Log.i(TAG, "Home page books fetched");

                            } else {

                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Home Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Home Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Home Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Home Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Home Page Error)");
                            mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Home Page Error)");
                        mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    if (BookieApplication.hasNetwork()){
                        if(!isFromTop){
                            Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }else {
                            mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        if(!isFromTop){
                            Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                        }else {
                            mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                        }
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
                mIsBooksFetching = false;
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Home Page book fetch onFailure: " + t.getMessage());

                if (BookieApplication.hasNetwork()){
                    if(!isFromTop){
                        Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }else {
                        mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                }else {
                    if(!isFromTop){
                        Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    }else {
                        mHomeTimelineAdapter.setError(HomeTimelineAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
                mIsBooksFetching = false;
            }
        });
    }
}
