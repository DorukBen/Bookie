package com.karambit.bookie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;

import com.karambit.bookie.adapter.LovedGenreAdapter;
import com.karambit.bookie.helper.TypefaceSpan;

public class LovedGenresActivity extends AppCompatActivity {

    private OnGenresSelectedListener mOnGenresSelectedListener;
    private LovedGenreAdapter mLovedGenreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loved_genres);
        SpannableString s = new SpannableString(getResources().getString(R.string.loved_genres_title));
        s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        String[] genres = getResources().getStringArray(R.array.genre_types);

        RecyclerView genreRecyclerView = (RecyclerView) findViewById(R.id.genreRecyclerView);

        genreRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));

        mLovedGenreAdapter = new LovedGenreAdapter(this, genres);

        genreRecyclerView.setAdapter(mLovedGenreAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_genres_selected:
                postSelectedGenres();
                return true;

            default:
                startActivity(new Intent(this,ProfileSettingsActivity.class));
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loved_genres_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void postSelectedGenres() {
        mLovedGenreAdapter.getSelectedGenreCodes();
    }

    public interface OnGenresSelectedListener {
        void onGenresSelected(Integer[] selectedGenreCodes);
    }
}
