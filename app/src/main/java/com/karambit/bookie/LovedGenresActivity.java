package com.karambit.bookie;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.adapter.LovedGenreAdapter;
import com.karambit.bookie.database.DBHelper;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.UserApi;
import com.orhanobut.logger.Logger;

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
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            float elevation = getResources().getDimension(R.dimen.actionbar_max_elevation);
            actionBar.setElevation(elevation);

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commitSelectedGenres();
                }
            });
        }

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

    private void commitSelectedGenres() {
        if (BookieApplication.hasNetwork()) {
            Integer[] selectedGenreCodes = mLovedGenreAdapter.getSelectedGenreCodes();
            if (selectedGenreCodes.length < 1) {
                Toast.makeText(this, R.string.select_one_genre, Toast.LENGTH_SHORT).show();
            } else {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                writeToLocalDatabase(selectedGenreCodes);

                postToServer(selectedGenreCodes);
            }
        } else {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeToLocalDatabase(final Integer[] selectedGenreCodes) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHelper.class) {
                    mDbManager.Threaded(mDbManager.getLovedGenreDataSource().cInsertGenres(mCurrentUser, selectedGenreCodes));

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

        Logger.d("Inserting loved genres to local database...");
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

        Logger.d("setLovedGenres() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tgenreCodes=" + lovedGenreCodes);

        setLovedGenres.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

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

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                mProgressDialog.dismiss();
                                Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            Logger.e("Response body is null. (Loved Genres Error)");
                            mProgressDialog.dismiss();
                            Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Logger.e("Response object is null. (Loved Genres Error)");
                        mProgressDialog.dismiss();
                        Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());

                    mProgressDialog.dismiss();
                    Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mProgressDialog.dismiss();

                Logger.e("Loved Genres onFailure: " + t.getMessage());

                Toast.makeText(LovedGenresActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
