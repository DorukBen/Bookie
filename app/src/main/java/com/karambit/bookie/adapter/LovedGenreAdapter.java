package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.karambit.bookie.R;
import com.karambit.bookie.helper.LayoutUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by orcan on 12/17/16.
 */

public class LovedGenreAdapter extends RecyclerView.Adapter<LovedGenreAdapter.GenreViewHolder> {

    private Context mContext;
    private SelectableGenre[] mSelectableGenres;

    public LovedGenreAdapter(Context context, String[] genres, Integer[] selectedGenreCodes) {
        mContext = context;
        mSelectableGenres = new SelectableGenre[genres.length];

        for (int i = 0; i < genres.length; i++) {
            boolean selected = Arrays.asList(selectedGenreCodes).contains(i);
            mSelectableGenres[i] = new SelectableGenre(genres[i], i, selected);
        }
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {

        private TextView mGenreName;
        private ImageView mSelectedIndicator;
        private CardView mGenreCard;

        public GenreViewHolder(View genreView) {
            super(genreView);

            mGenreName = (TextView) genreView.findViewById(R.id.genreNameTextView);
            mSelectedIndicator = (ImageView) genreView.findViewById(R.id.genreSelectedIndicatorImageView);
            mGenreCard = (CardView) genreView.findViewById(R.id.genreCardView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mSelectableGenres.length;
    }

    @Override
    public GenreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View genreView = LayoutInflater.from(mContext).inflate(R.layout.item_loved_genre, parent, false);
        return new GenreViewHolder(genreView);
    }

    @Override
    public void onBindViewHolder(final GenreViewHolder holder, int position) {

        final SelectableGenre selectableGenre = mSelectableGenres[position];

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectableGenre.mSelected = !selectableGenre.mSelected;

                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.mGenreName.setText(selectableGenre.mGenreName);

        if (selectableGenre.mSelected) {
            holder.mSelectedIndicator.setImageResource(R.drawable.ic_favorite_black_24dp);
            holder.mSelectedIndicator.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
            holder.mGenreName.setTextColor(ContextCompat.getColor(mContext, R.color.primaryTextColor));
            holder.mGenreCard.setCardElevation(LayoutUtils.DP * 3);
        } else {
            holder.mSelectedIndicator.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            holder.mSelectedIndicator.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
            holder.mGenreName.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
            holder.mGenreCard.setCardElevation(LayoutUtils.DP * 1);
        }
    }

    private class SelectableGenre {
        private String mGenreName;
        private int mOrder;
        private boolean mSelected;

        private SelectableGenre(String genreName, int order, boolean selected) {
            mGenreName = genreName;
            mOrder = order;
            mSelected = selected;
        }
    }

    public Integer[] getSelectedGenreCodes() {
        ArrayList<Integer> selectedGenreCodes = new ArrayList<>(mSelectableGenres.length);

        for (SelectableGenre selGen : mSelectableGenres) {
            if (selGen.mSelected) {
                selectedGenreCodes.add(selGen.mOrder);
            }
        }

        return selectedGenreCodes.toArray(new Integer[0]);
    }
}
