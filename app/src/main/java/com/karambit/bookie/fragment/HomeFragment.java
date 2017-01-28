package com.karambit.bookie.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
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

import com.karambit.bookie.LoginRegisterActivity;
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
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final int HOME_FRAGMENT_TAB_INEX = 0;
    private static final String TAG = HomeFragment.class.getSimpleName();

    private ArrayList<Book> mHeaderBooks = new ArrayList<>();
    private ArrayList<Book> mListBooks = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private HomeTimelineAdapter mHomeTimelineAdapter;
    private PullRefreshLayout mPullRefreshLayout;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.homeRecyclerView);

        mRecyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), HOME_FRAGMENT_TAB_INEX));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mHomeTimelineAdapter = new HomeTimelineAdapter(
                getContext()
        );

        mHomeTimelineAdapter.setHasStableIds(true);

        mRecyclerView.setAdapter(mHomeTimelineAdapter);

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                mListBooks = new ArrayList<Book>();
                fetchHomePageBooks();
            }
        });

        //For improving recyclerviews performance
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        ((MainActivity)getActivity()).setDoubleTapHomeButtonListener(new MainActivity.DoubleTapHomeButtonListener() {
            @Override
            public void onDoubleTapHomeButton() {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mPullRefreshLayout.setRefreshing(true);
        fetchHomePageBooks();
        return rootView;
    }

    private void fetchHomePageBooks() {
        BookApi bookApi = BookieClient.getClient().create(BookApi.class);
        int[] fetchedBookIds = new int[mListBooks.size()];
        int i = 0;
        for (Book book: mListBooks){
            fetchedBookIds[i] = book.getID();
            i++;
        }

        String email = SessionManager.getCurrentUserDetails(getContext().getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getContext().getApplicationContext()).getPassword();
        Call<ResponseBody> register = bookApi.getHomePageBooks(email, password, fetchedBookIds);

        register.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String json = response.body().string();

                    JSONObject responseObject = new JSONObject(json);
                    boolean error = responseObject.getBoolean("error");

                    if (!error) {
                        if (mListBooks.size() > 0){
                            JSONArray listBooksArray = responseObject.getJSONArray("list_books");
                            mListBooks.addAll(Book.jsonArrayToBookList(listBooksArray));

                            mHomeTimelineAdapter.addFeedBooks(mListBooks);
                        }else {
                            JSONArray headerBooksArray = responseObject.getJSONArray("header_books");
                            mHeaderBooks.addAll(Book.jsonArrayToBookList(headerBooksArray));

                            JSONArray listBooksArray = responseObject.getJSONArray("list_books");
                            mListBooks.addAll(Book.jsonArrayToBookList(listBooksArray));

                            mHomeTimelineAdapter.setHeaderAndFeedBooks(mHeaderBooks, mListBooks);
                        }

                        Log.i(TAG, "Home page books fetched");

                    } else {

                        int errorCode = responseObject.getInt("error_code");

                        if (errorCode == ErrorCodes.EMAIL_TAKEN) {
                            //TODO: Error controls
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Home Page book fetch onFailure: " + t.getMessage());

                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();

                mPullRefreshLayout.setRefreshing(false);
            }
        });
    }
}
