package com.karambit.bookie.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.LocationActivity;
import com.karambit.bookie.LoginRegisterActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.SearchAdapter;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.SearchApi;
import com.karambit.bookie.rest_api.UserApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {


    private static final String TAG = SearchFragment.class.getSimpleName();

    private ArrayList<Integer> mGenreCodes = new ArrayList<>();
    private ArrayList<Book> mBooks = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();

    private int mFetchGenreCode = -1;
    private int mFetchSearchButtonPressed = 0;
    private SearchAdapter mSearchAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSearchAdapter = new SearchAdapter(getContext(), mGenreCodes, mBooks, mUsers);
        mSearchAdapter.setBookClickListener(new SearchAdapter.SearchItemClickListener() {
            @Override
            public void onGenreClick(int genreCode) {
                mFetchGenreCode = genreCode;
            }

            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra("book", book);
                getContext().startActivity(intent);
            }

            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mSearchAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mSearchAdapter);

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        final EditText searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);

        rootView.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SessionManager.getCurrentUser(getContext().getApplicationContext()).getLatitude() != 0.0d && SessionManager.getCurrentUser(getContext().getApplicationContext()).getLongitude() != 0.0d ){
                    mFetchSearchButtonPressed = 1;
                    getSearchResults(searchEditText.getText().toString());
                }else{
                    startActivity(new Intent(getContext(), LocationActivity.class));
                }
            }
        });


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                String string = searchEditText.getText().toString();
                getSearchResults(string);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return rootView;
    }

    private void getSearchResults(String searchString) {
        SearchApi searchApi = BookieClient.getClient().create(SearchApi.class);
        String email = SessionManager.getCurrentUserDetails(getContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getContext()).getPassword();
        Call<ResponseBody> register = searchApi.getSearchResults(email, password, searchString, mFetchGenreCode, mFetchSearchButtonPressed);

        register.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String json = response.body().string();

                    JSONObject responseObject = new JSONObject(json);
                    boolean error = responseObject.getBoolean("error");

                    if (!error) {
                        JSONArray booksArray = responseObject.getJSONArray("books");
                        JSONArray usersArray = responseObject.getJSONArray("users");

                        mBooks.clear();
                        mUsers.clear();

                        mBooks.addAll(Book.jsonArrayToBookList(booksArray));
                        mUsers.addAll(User.jsonArrayToUserList(usersArray));

                        mSearchAdapter.setItems(new ArrayList<Integer>(), mBooks, mUsers);
                    } else {

                        int errorCode = responseObject.getInt("error_code");

                        if (errorCode == ErrorCodes.EMAIL_TAKEN) {
                        } else {
                            Log.e(TAG, "onResponse: errorCode = " + errorCode);
                        }
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.e(TAG, "Register onFailure: " + t.getMessage());
            }
        });
    }

}
