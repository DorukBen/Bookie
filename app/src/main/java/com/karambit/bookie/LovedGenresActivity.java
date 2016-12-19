package com.karambit.bookie;

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

import com.karambit.bookie.adapter.LovedGenreAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.User;

public class LovedGenresActivity extends AppCompatActivity {

    private static final String TAG = LovedGenresActivity.class.getSimpleName();

    private LovedGenreAdapter mLovedGenreAdapter;
    private DBHandler mDBHandler;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loved_genres);
        SpannableString s = new SpannableString(getResources().getString(R.string.loved_genres_title));
        s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        mDBHandler = new DBHandler(this);
        mCurrentUser = mDBHandler.getCurrentUser();

        String[] genres = getResources().getStringArray(R.array.genre_types);

        RecyclerView genreRecyclerView = (RecyclerView) findViewById(R.id.genreRecyclerView);
        genreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));

        mLovedGenreAdapter = new LovedGenreAdapter(this, genres, mDBHandler.getLovedGenresAsInteger(mCurrentUser));

        genreRecyclerView.setAdapter(mLovedGenreAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_genres_selected:
                commitSelectedGenres();
                return true;

            default:
                startActivity(new Intent(this, ProfileSettingsActivity.class));
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

        writeToLocalDatabase(selectedGenreCodes);

        postToServer(selectedGenreCodes);
    }

    private void writeToLocalDatabase(final Integer[] selectedGenreCodes) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBHandler.insertLovedGenres(mCurrentUser, selectedGenreCodes);
                progressDialog.dismiss();
            }
        }).start();

        Log.d(TAG, "Inserting loved genres to local database...");
    }

    private void postToServer(Integer[] selectedGenreCodes) {
        //TODO Post to Server
    }
}
