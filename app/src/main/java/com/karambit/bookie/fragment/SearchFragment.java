package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.BookieApplication;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.SearchAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.string_similarity.JaroWinkler;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.SearchApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getSimpleName();

    public static final int TAB_INDEX = 1;
    public static final int VIEW_PAGER_INDEX = 1;
    public static final String TAB_SPEC = "tab_search";
    public static final String TAB_INDICATOR = "tab1";

    private String[] mAllGenres ;
    private ArrayList<Integer> mGenreCodes = new ArrayList<>();
    private ArrayList<Book> mBooks = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();

    private int mFetchGenreCode = -1;
    private int mFetchSearchButtonPressed = 0; // TODO Change type to boolean
    private SearchAdapter mSearchAdapter;
    private DBHandler mDbHandler;

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
        final EditText searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);

        mDbHandler = DBHandler.getInstance(getContext());

        mAllGenres  = getContext().getResources().getStringArray(R.array.genre_types);

        mSearchAdapter = new SearchAdapter(getContext(), mGenreCodes, mBooks, mUsers);
        mSearchAdapter.setBookClickListener(new SearchAdapter.SearchItemClickListener() {
            @Override
            public void onGenreClick(int genreCode) {
                mFetchGenreCode = genreCode;
                searchEditText.setText(mAllGenres[genreCode]);
                getSearchResults("");
            }

            @Override
            public void onBookClick(Book book) {
                addBookToSearchHistory(book);

                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra("book", book);
                getContext().startActivity(intent);
            }

            @Override
            public void onUserClick(User user) {
                addUserToSearchHistory(user);

                Intent intent = new Intent(getContext(), ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ProfileActivity.EXTRA_USER, user);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onClearHistoryClick() {
                mDbHandler.clearSearchHistory();
                mSearchAdapter.setWarning(SearchAdapter.WARNING_TYPE_NOTHING_TO_SHOW);
            }
        });

        mSearchAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mSearchAdapter);

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        rootView.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mFetchSearchButtonPressed = 1;
            getSearchResults(searchEditText.getText().toString());
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

        showSearchHistory();
        return rootView;
    }

    private void getSearchResults(final String searchString) {
        final SearchApi searchApi = BookieClient.getClient().create(SearchApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> searchResults = searchApi.getSearchResults(email, password, searchString, mFetchGenreCode, mFetchSearchButtonPressed);

        searchResults.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_NONE);
                                mSearchAdapter.setWarning(SearchAdapter.WARNING_TYPE_NONE);

                                mBooks.clear();
                                mUsers.clear();

                                if (!responseObject.isNull("listBooks")){
                                    JSONArray booksArray = responseObject.getJSONArray("listBooks");
                                    mBooks.addAll(Book.jsonArrayToBookList(booksArray));
                                }
                                if (!responseObject.isNull("listUsers")){
                                    JSONArray usersArray = responseObject.getJSONArray("listUsers");
                                    mUsers.addAll(User.jsonArrayToUserList(usersArray));
                                }

                                if (mFetchGenreCode > -1 && !TextUtils.isEmpty(searchString)){
                                    mSearchAdapter.setItems(new ArrayList<Integer>(), mBooks, mUsers);
                                }else {
                                    mSearchAdapter.setItems(getMostSimilarGenreIndex(searchString), mBooks, mUsers);
                                }

                                if (mBooks.size() < 1 && mUsers.size() < 1){
                                    if (TextUtils.isEmpty(searchString)){
                                        showSearchHistory();
                                    }else {
                                        mSearchAdapter.setWarning(SearchAdapter.WARNING_TYPE_NO_RESULT_FOUND);
                                    }
                                }

                                mFetchGenreCode = -1;
                                mFetchSearchButtonPressed = 0;
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

                                mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Search Page Error)");
                            mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Search Page Error)");
                        mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    if(BookieApplication.hasNetwork()){
                        mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else{
                        mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_NO_CONNECTION);
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(BookieApplication.hasNetwork()){
                    mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                }else{
                    mSearchAdapter.setError(SearchAdapter.ERROR_TYPE_NO_CONNECTION);
                }

                Log.e(TAG, "Search onFailure: " + t.getMessage());
            }
        });
    }

    private ArrayList<Integer> getMostSimilarGenreIndex(String searchString){
        final HashMap<Integer, Double> searchGenreSimilarityMap = new HashMap<>();
        for (int i = 0; i < mAllGenres.length; i++){
            double similarity = getStringSimilarity(mAllGenres[i], searchString);
            if (similarity > 0.7){
                searchGenreSimilarityMap.put(i, similarity);
                if (similarity > 0.9){
                    ArrayList<Integer> finalResult = new ArrayList<>();
                    finalResult.add(i);
                    return finalResult;
                }
            }
        }

        ArrayList<Integer> sortedGenres = new ArrayList<>(searchGenreSimilarityMap.keySet());
        Collections.sort(sortedGenres, new Comparator<Integer>() {
            @Override
            public int compare(Integer s1, Integer s2) {
                Double similarity1 = searchGenreSimilarityMap.get(s1);
                Double similarity2 = searchGenreSimilarityMap.get(s2);
                return similarity2.compareTo(similarity1);
            }
        });


        ArrayList<Integer> finalResults = new ArrayList<>();
        if (sortedGenres.size() > 1){
            finalResults.add(sortedGenres.get(0));
        }
        if (sortedGenres.size()> 2){
            finalResults.add(sortedGenres.get(1));
        }

        return finalResults;
    }

    private double getStringSimilarity(String string1, String string2){
        string1 = string1.toLowerCase();
        string2 = string2.toLowerCase();

        JaroWinkler jaroWinkler = new JaroWinkler();

        return jaroWinkler.similarity(string1, string2);
    }

    private void showSearchHistory(){
        ArrayList<Book> historyBooks = mDbHandler.getAllSearchBooks(mDbHandler.getAllSearchBookUsers());
        ArrayList<User> historyUsers = mDbHandler.getAllSearchUsers();

        if (historyBooks.size() + historyUsers.size() > 0){
            mSearchAdapter.setItems(new ArrayList<Integer>(), historyBooks, historyUsers);
            mSearchAdapter.setShowHistory(true);
        } else {
            mSearchAdapter.setWarning(SearchAdapter.WARNING_TYPE_NOTHING_TO_SHOW);
        }
    }

    private void addBookToSearchHistory(Book book) {
        ArrayList<Book> historyBooks = mDbHandler.getAllSearchBooks(mDbHandler.getAllSearchBookUsers());

        if (historyBooks.size() > 2){
            mDbHandler.deleteAllSearchBooks();

            historyBooks.remove(0);

            for (Book historyBook: historyBooks){
                mDbHandler.insertSearchBookUser(historyBook.getOwner());
                mDbHandler.insertSearchBook(historyBook);
            }
        }

        if (!mDbHandler.isSearchBookUserExists(book.getOwner())){
            mDbHandler.insertSearchBookUser(book.getOwner());
        }
        if (!mDbHandler.isSearchBookExists(book)){
            mDbHandler.insertSearchBook(book);
        }
    }

    private void addUserToSearchHistory(User user) {
        ArrayList<User> historyUsers = mDbHandler.getAllSearchUsers();

        if (historyUsers.size() > 2){
            mDbHandler.deleteAllSearchUsers();

            historyUsers.remove(0);

            for (User historyUser: historyUsers){
                mDbHandler.insertSearchUser(historyUser);
            }
        }

        if (!mDbHandler.isSearchUserExists(user)){
            mDbHandler.insertSearchUser(user);
        }
    }
}
