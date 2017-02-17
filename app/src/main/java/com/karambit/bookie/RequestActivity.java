package com.karambit.bookie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.adapter.HomeTimelineAdapter;
import com.karambit.bookie.adapter.RequestAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.LayoutUtils;
import com.karambit.bookie.helper.NetworkChecker;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestActivity extends AppCompatActivity {

    private static final String TAG = RequestActivity.class.getSimpleName();

    private static final int REQUESTS_MODIFIED = 0;

    private Book mBook;
    private ArrayList<Book.Request> mRequests = new ArrayList<>();
    private PullRefreshLayout mPullRefreshLayout;
    private RequestAdapter mRequestAdapter;
    private RecyclerView mRequestRecyclerView;
    private TextView mAcceptedRequestTextView;
    private Hashtable<Book.Request, String> mLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.request_activity_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_messaging_cancel_selection);
            actionBar.setElevation(0);
        }

        mBook = getIntent().getParcelableExtra("book");

        mPullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mPullRefreshLayout.setRefreshing(true);
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchRequests();
            }
        });

        mAcceptedRequestTextView = (TextView) findViewById(R.id.acceptedRequestText);

        mRequestRecyclerView = (RecyclerView) findViewById(R.id.requestRecyclerView);
        mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRequestAdapter = new RequestAdapter(this);

        mRequestRecyclerView.setAdapter(mRequestAdapter);

        mRequestRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int totalScrolled = 0;
            ActionBar actionBar = getSupportActionBar();

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalScrolled += dy;
                totalScrolled = Math.abs(totalScrolled);

                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(totalScrolled));
            }
        });

        mRequestAdapter.setRequestClickListeners(new RequestAdapter.RequestClickListeners() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(RequestActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.USER, user);
                startActivity(intent);
            }

            @Override
            public void onAcceptClick(final Book.Request request) {
                new AlertDialog.Builder(RequestActivity.this)
                    .setMessage(getString(R.string.accept_request_prompt, request.getFromUser().getName()))
                    .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // TODO Server

                            int index = mRequests.indexOf(request);
                            mRequests.remove(index);
                            mRequestAdapter.notifyItemRemoved(index);

                            for (Book.Request r : mRequests) {
                                r.setRequestType(Book.RequestType.REJECT);
                            }

                            mRequestAdapter.notifyDataSetChanged();

                            setAcceptedRequestText(request);

                            setResult(REQUESTS_MODIFIED); // for refreshing previous page
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
            }

            @Override
            public void onRejectClick(final Book.Request request) {
                new AlertDialog.Builder(RequestActivity.this)
                    .setMessage(getString(R.string.reject_request_prompt, request.getFromUser().getName()))
                    .setPositiveButton(R.string.reject, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // TODO Server

                            int indexBefore = mRequests.indexOf(request);
                            request.setRequestType(Book.RequestType.REJECT);
                            Collections.sort(mRequests);
                            int indexAfter = mRequests.indexOf(request) + 1; // Subtitle
                            mRequestAdapter.notifyItemMoved(indexBefore, indexAfter);
                            mRequestAdapter.notifyItemChanged(mRequestAdapter.getSentRequestCount() - 1);

                            setResult(REQUESTS_MODIFIED); // for refreshing previous page
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
            }
        });

        fetchRequests();
    }

    private void fetchRequests() {
        BookApi bookApi = BookieClient.getClient().create(BookApi.class);

        String email = SessionManager.getCurrentUserDetails(getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getApplicationContext()).getPassword();
        final Call<ResponseBody> getBookRequests = bookApi.getBookRequests(email, password, mBook.getID());

        getBookRequests.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("bookRequests")){
                                    mRequests.clear();
                                    mRequests.addAll(Book.jsonObjectToBookRequests(responseObject, mBook));

                                    mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_NONE);
                                    mRequestAdapter.setRequests(mRequests);
                                    mRequestAdapter.notifyDataSetChanged();

                                    mLocations = new Hashtable<>(mRequests.size());
                                    mRequestAdapter.setLocations(mLocations);

                                    mPullRefreshLayout.setRefreshing(false);

                                    fetchLocations();

                                    Log.i(TAG, "Book requests fetched");
                                }else {
                                    Log.e(TAG, "bookRequests is empty. (Request Page Error)");
                                    mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                                }


                            } else {

                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Request Page Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Request Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Request Page Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Request Page Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Home Page Error)");
                            mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Home Page Error)");
                        mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    if (NetworkChecker.isNetworkAvailable(RequestActivity.this)){
                        mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                    }else {
                        mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_NO_CONNECTION);
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Home Page book fetch onFailure: " + t.getMessage());
                if (NetworkChecker.isNetworkAvailable(RequestActivity.this)){
                    mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_UNKNOWN_ERROR);
                }else {
                    mRequestAdapter.setError(RequestAdapter.ERROR_TYPE_NO_CONNECTION);
                }

                mPullRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setAcceptedRequestText(final Book.Request request) {
        mAcceptedRequestTextView.setVisibility(View.VISIBLE);

        String fromUserName = request.getFromUser().getName();
        String acceptRequestString = getString(R.string.you_accepted_request, fromUserName);
        SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);
        int startIndex = acceptRequestString.indexOf(fromUserName);
        int endIndex = startIndex + fromUserName.length();

        spanAcceptRequest.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RequestActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.USER, request.getFromUser());
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)),
                                  0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mAcceptedRequestTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mAcceptedRequestTextView.setHighlightColor(Color.TRANSPARENT);
        mAcceptedRequestTextView.setText(spanAcceptRequest);

        getSupportActionBar().setElevation(0);
        mRequestRecyclerView.setOnScrollListener(null);

        ViewCompat.setElevation(mAcceptedRequestTextView, 8f * LayoutUtils.DP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchLocations() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < mRequests.size(); i++) {
                    final Book.Request r = mRequests.get(i);
                    User fromUser = r.getFromUser();

                    if (fromUser.getLocation() != null) {

                        final double latitude = fromUser.getLocation().latitude;
                        final double longitude = fromUser.getLocation().longitude;

                        try {
                            List<Address> addresses;
                            Geocoder geocoder = new Geocoder(RequestActivity.this, Locale.getDefault());
                            if (latitude > -90 && latitude < 90 && longitude > -90 && longitude < 90) {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            } else {
                                addresses = new ArrayList<>();
                            }

                            // Admin area equals Istanbul
                            // Subadmin are equals Bahçelievler

                            if (addresses.size() > 0) {
                                String locationString = "";

                                String subAdminArea = addresses.get(0).getSubAdminArea();
                                if (!TextUtils.isEmpty(subAdminArea) && !subAdminArea.equals("null")) {
                                    locationString += subAdminArea + " / ";
                                }

                                String adminArea = addresses.get(0).getAdminArea();
                                if (!TextUtils.isEmpty(adminArea) && !adminArea.equals("null")) {
                                    locationString += adminArea;
                                }

                                if (!TextUtils.isEmpty(locationString)) {
                                    mLocations.put(r, locationString);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRequestAdapter.notifyItemChanged(mRequests.indexOf(r));
                                        }
                                    });
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}