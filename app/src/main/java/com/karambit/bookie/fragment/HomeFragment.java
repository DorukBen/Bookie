package com.karambit.bookie.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.HomeTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.NetworkChecker;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookApi;
import com.karambit.bookie.rest_api.BookieClient;

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

    public static final int HOME_FRAGMENT_TAB_INEX = 0;

    public static final int REFRESH_INTERVAL_HOURS = 12;

    private ArrayList<Book> mSuggestedBooks;
    private ArrayList<Book> mListBooks;
    private RecyclerView mRecyclerView;

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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NetworkChecker.isNetworkAvailable(getContext())) {

            User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());
            String email = currentUserDetails.getEmail();
            String password = currentUserDetails.getPassword();
            int userID = currentUserDetails.getUser().getID();

            BookApi bookApi = BookieClient.getClient().create(BookApi.class);
            Call<ResponseBody> homePageCall = bookApi.fetchHomePage(email, password, userID);
            homePageCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {
                        JSONObject responseObject = new JSONObject(response.body().string());

                        if (!responseObject.getBoolean("error")) {

                            // Suggested books
                            ArrayList<Book> suggestedBooks =
                                    Book.jsonArrayToBookList(responseObject.getJSONArray("suggested_books"));

                            // List books
                            ArrayList<Book> listBooks =
                                    Book.jsonArrayToBookList(responseObject.getJSONArray("book_list"));

                            // TODO List content control

                            mSuggestedBooks = suggestedBooks;
                            mListBooks = listBooks;

                            // Update RecyclerView
                            if (mRecyclerView.getAdapter() == null) {
                                mRecyclerView.setAdapter(new HomeTimelineAdapter(getContext(), mSuggestedBooks, mListBooks));
                            } else {
                                HomeTimelineAdapter adapter = (HomeTimelineAdapter) mRecyclerView.getAdapter();
                                adapter.setHeaderBooks(mSuggestedBooks);
                                adapter.setFeedBooks(mListBooks);
                                adapter.notifyDataSetChanged();
                            }

                        } else {
                            int errorCode = responseObject.getInt("error_code");

                            //TODO Error handling
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();

                        //TODO Error handling
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //TODO Error handling
                }
            });
        } else {

            //TODO Error handling

        }
    }
}
