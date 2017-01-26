package com.karambit.bookie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.karambit.bookie.adapter.LovedGenreAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class LovedGenresActivity extends AppCompatActivity {

    private static final String TAG = LovedGenresActivity.class.getSimpleName();

    private LovedGenreAdapter mLovedGenreAdapter;
    private DBHandler mDBHandler;
    private User mCurrentUser;
    private boolean mLocalDone = false;
    private boolean mServerDone = false;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loved_genres);

        SpannableString s = new SpannableString(getResources().getString(R.string.loved_genres_title));
        s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        mDBHandler = new DBHandler(getApplicationContext());
        mCurrentUser = SessionManager.getCurrentUser(getApplicationContext());

        String[] genres = getResources().getStringArray(R.array.genre_types);

        RecyclerView genreRecyclerView = (RecyclerView) findViewById(R.id.genreRecyclerView);
        genreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));

        mLovedGenreAdapter = new LovedGenreAdapter(this, genres, mDBHandler.getLovedGenresAsInt(mCurrentUser));

        genreRecyclerView.setAdapter(mLovedGenreAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_genres_selected:
                commitSelectedGenres();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loved_genres_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void commitSelectedGenres() {
        Integer[] selectedGenreCodes = mLovedGenreAdapter.getSelectedGenreCodes();
        if (selectedGenreCodes.length < 1){
            Toast.makeText(this, "Please select at least one genre", Toast.LENGTH_SHORT).show();
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
                mDBHandler.insertLovedGenres(mCurrentUser, selectedGenreCodes);
                mLocalDone = true;

                if (mServerDone) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    finish();
                }
            }
        }).start();

        Log.d(TAG, "Inserting loved genres to local database...");
    }

    private void postToServer(Integer[] selectedGenreCodes) {
        //TODO Post to Server

        mServerDone = true;

        if (mLocalDone) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            finish();
        }
    }
}
