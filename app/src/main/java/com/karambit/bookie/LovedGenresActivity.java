package com.karambit.bookie;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.karambit.bookie.adapter.LovedGenreAdapter;
import com.karambit.bookie.database.DBHelper;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
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

public class LovedGenresActivity extends AppCompatActivity {

    private static final String TAG = LovedGenresActivity.class.getSimpleName();

    private LovedGenreAdapter mLovedGenreAdapter;
    private DBManager mDbManager;
    private User mCurrentUser;
    private boolean mLocalDone = false;
    private boolean mServerDone = false;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loved_genres);

        SpannableString s = new SpannableString(getResources().getString(R.string.loved_genres_title));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        mDbManager = new DBManager(this);
        mDbManager.open();

        mCurrentUser = SessionManager.getCurrentUser(this);

        String[] genres = getResources().getStringArray(R.array.genre_types);

        RecyclerView genreRecyclerView = (RecyclerView) findViewById(R.id.genreRecyclerView);
        genreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));

        Integer[] lovedGenresAsInt = mDbManager.getLovedGenreDataSource().getGenres(mCurrentUser);

        mLovedGenreAdapter = new LovedGenreAdapter(this, genres, lovedGenresAsInt);

        genreRecyclerView.setAdapter(mLovedGenreAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                commitSelectedGenres();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void commitSelectedGenres() {
        Integer[] selectedGenreCodes = mLovedGenreAdapter.getSelectedGenreCodes();
        if (selectedGenreCodes.length < 1){
            Toast.makeText(this, R.string.select_one_genre, Toast.LENGTH_SHORT).show();
        }else{
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            writeToLocalDatabase(selectedGenreCodes);

            postToServer(selectedGenreCodes);
        }
    }

    private void writeToLocalDatabase(final Integer[] selectedGenreCodes) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHelper.class) {
                    mDbManager.getLovedGenreDataSource().insertGenres(mCurrentUser, selectedGenreCodes);
                    mLocalDone = true;

                    if (mServerDone) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        finish();
                    }
                }
            }
        }).start();

        Log.d(TAG, "Inserting loved genres to local database...");
    }

    private void postToServer(Integer[] selectedGenreCodes) {
        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();

        String lovedGenreCodes;

        int i = 0;

        StringBuilder builder = new StringBuilder();
        for (Integer genreCode: selectedGenreCodes){
            builder.append(genreCode);
            if (i < selectedGenreCodes.length - 1){
                builder.append("_");
            }
            i++;
        }
        lovedGenreCodes = builder.toString();

        Call<ResponseBody> setLovedGenres = userApi.setLovedGenres(email, password, lovedGenreCodes);

        setLovedGenres.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                mServerDone = true;

                                if (mLocalDone) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                    finish();
                                }

                            } else {

                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Loved Genres Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Loved Genres Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Loved Genres Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Loved Genres Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                mProgressDialog.dismiss();
                                Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Loved Genres Error)");
                            mProgressDialog.dismiss();
                            Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.e(TAG, "Response object is null. (Loved Genres Error)");
                        mProgressDialog.dismiss();
                        Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    mProgressDialog.dismiss();
                    Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mProgressDialog.dismiss();

                Log.e(TAG, "Loved Genres onFailure: " + t.getMessage());

                Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
