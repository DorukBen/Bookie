package com.karambit.bookie.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.LayoutUtils;
import com.karambit.bookie.helper.pull_refresh_layout.SmartisanProgressBarDrawable;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;

/**
 * Created by orcan on 10/19/16.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_BOOK = 0;
    public static final int TYPE_FOOTER = 1;

    private Context mContext;
    private ArrayList<Book> mSearchResults;

    private boolean mProgressBarActive;

    private BookClickListener mBookClickListener;

    public SearchAdapter(Context context, ArrayList<Book> searchResults) {
        mContext = context;
        mSearchResults = searchResults;

        mProgressBarActive = false;
    }

    private static class BookViewHolder extends RecyclerView.ViewHolder {

        private View mElevatedSection;
        private ImageView mBookImage;
        private CardView mBookImageCard;
        private TextView mBookName;
        private TextView mBookAuthor;

        private BookViewHolder(View itemBookView) {
            super(itemBookView);

            mElevatedSection = itemBookView.findViewById(R.id.itemBookElevatedSectionRelativeLayout);
            ViewCompat.setElevation(mElevatedSection, LayoutUtils.DP * 2);

            mBookImageCard = (CardView) itemBookView.findViewById(R.id.itemBookImageCardView);
            mBookImageCard.setCardElevation(LayoutUtils.DP * 4);

            mBookImage = (ImageView) itemBookView.findViewById(R.id.itemBookImageView);
            mBookName = (TextView) itemBookView.findViewById(R.id.itemBookNameTextView);
            mBookAuthor = (TextView) itemBookView.findViewById(R.id.itemBookAuthorTextView);

        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar mProgressBar;

        private FooterViewHolder(View footerView) {
            super(footerView);

            mProgressBar = (ProgressBar) footerView.findViewById(R.id.footerProgressBar);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return getSearchResultsCount() + 1; // Footer
    }

    /*
       RESULT BOOK
       FOOTER
    */
    @Override
    public int getItemViewType(int position) {

        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;

        } else {
            return TYPE_BOOK;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //View inflating for view types and creating ViewHolders

        switch (viewType) {

            case TYPE_BOOK:
                View bookView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
                return new BookViewHolder(bookView);

            case TYPE_FOOTER:
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer, parent, false);
                return new FooterViewHolder(footerView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_BOOK: {

                final BookViewHolder bookHolder = (BookViewHolder) holder;

                final Book book = mSearchResults.get(position);

                if (mBookClickListener != null) {
                    bookHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mBookClickListener.onBookClick(book);
                        }
                    });
                }

                bookHolder.mBookName.setText(book.getName());

                bookHolder.mBookAuthor.setText(book.getAuthor());

                Glide.with(mContext)
                        .load(book.getThumbnailURL())
                        .asBitmap()
                        .placeholder(R.drawable.placeholder_88dp)
                        .error(R.drawable.error_88dp)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                Bitmap croppedBitmap = ImageScaler.cropImage(resource, 72 / 96f);
                                bookHolder.mBookImage.setImageBitmap(croppedBitmap);
                            }
                        });

                break;
            }


            case TYPE_FOOTER: {

                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.mProgressBar.setIndeterminateDrawable(new SmartisanProgressBarDrawable(mContext));

                if (mProgressBarActive) {
                    footerHolder.mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.mProgressBar.setVisibility(View.GONE);
                }

                break;
            }
        }
    }

    private int getSearchResultsCount() {
        return mSearchResults != null ? mSearchResults.size() : 0;
    }

    public boolean isProgressBarActive() {
        return mProgressBarActive;
    }

    public void setProgressBarActive(boolean progressBarActive) {
        mProgressBarActive = progressBarActive;
        notifyItemChanged(getItemCount() - 1);
    }

    public interface BookClickListener {
        void onBookClick(Book book);
    }

    public void setBookClickListener(BookClickListener bookClickListener) {
        mBookClickListener = bookClickListener;
    }

}
