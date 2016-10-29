package com.karambit.bookie.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.BookActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;

/**
 * Created by orcan on 10/16/16.
 */

public class HomeTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = HomeTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DUAL_BOOK = 1;
    private static final int TYPE_SUBTITLE = 2;
    private static final int TYPE_FOOTER = 3;

    private Context mContext;
    private ArrayList<Book> mHeaderBooks;
    private ArrayList<Book> mFeedBooks;

    private boolean mProgressBarActive;

    public HomeTimelineAdapter(Context context, ArrayList<Book> headerBooks, ArrayList<Book> feedBooks) {
        mContext = context;
        mHeaderBooks = headerBooks;
        mFeedBooks = feedBooks;

        mProgressBarActive = false;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        // TODO Header

        private ImageView mBookImage;
        private TextView mBookName;
        private TextView mAuthor;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mBookImage = (ImageView) headerView.findViewById(R.id.bookImageHeaderImageView);
            mBookName = (TextView) headerView.findViewById(R.id.bookNameHeaderTextView);
            mAuthor = (TextView) headerView.findViewById(R.id.authorHeaderTextView);
        }
    }

    private static class DualBookViewHolder extends RecyclerView.ViewHolder {

        private View mPresentationalLeft;
        private ImageView mBookImageLeft;
        private TextView mBookNameLeft;
        private TextView mAuthorLeft;

        private View mPresentationalRight;
        private ImageView mBookImageRight;
        private TextView mBookNameRight;
        private TextView mAuthorRight;

        private DualBookViewHolder(View dualBookView) {
            super(dualBookView);

            mPresentationalLeft = dualBookView.findViewById(R.id.presentationalBookLeft);
            mBookImageLeft = (ImageView) mPresentationalLeft.findViewById(R.id.bookImagePresentationalImageView);
            mBookNameLeft = (TextView) mPresentationalLeft.findViewById(R.id.bookNamePresentationalTextView);
            mAuthorLeft = (TextView) mPresentationalLeft.findViewById(R.id.authorPresentationalTextView);

            mPresentationalRight = dualBookView.findViewById(R.id.presentationalBookRight);
            mBookImageRight = (ImageView) mPresentationalRight.findViewById(R.id.bookImagePresentationalImageView);
            mBookNameRight = (TextView) mPresentationalRight.findViewById(R.id.bookNamePresentationalTextView);
            mAuthorRight = (TextView) mPresentationalRight.findViewById(R.id.authorPresentationalTextView);
        }
    }

    private static class SubtitleViewHolder extends RecyclerView.ViewHolder {

        private TextView mSubtitle;

        private SubtitleViewHolder(View subtitleView) {
            super(subtitleView);

            mSubtitle = (TextView) subtitleView.findViewById(R.id.subtitleTextView);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar mProgressBar;
        private TextView mTextView;

        private FooterViewHolder(View footerView) {
            super(footerView);

            mProgressBar = (ProgressBar) footerView.findViewById(R.id.footerProgressBar);
            mTextView = (TextView) footerView.findViewById(R.id.footerTextView);
        }
    }

    @Override
    public int getItemCount() {
            return getDualRowsCount() + 2; // (Dual row count + 1 row) + Header + Footer
    }

    /*
        HEADER
        SUBTITLE
        DUAL BOOKS
        FOOTER
     */
    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return TYPE_HEADER;

        } else if (position == 1) {
            return TYPE_SUBTITLE;

        } else if (position < getDualRowsCount() + 2) { // + Header + Subtitle
            return TYPE_DUAL_BOOK;

        } else if (position == getItemCount() - 1) {
            return TYPE_FOOTER;

        } else {
            throw new IllegalArgumentException("Invalid view type at position " + position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //View inflating for view types and creating ViewHolders

        switch (viewType) {

            case TYPE_HEADER:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_home_timeline, parent, false);
                return new HeaderViewHolder(headerView);

            case TYPE_DUAL_BOOK:
                View dualBookView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dual_book_home_timeline, parent, false);
                return new DualBookViewHolder(dualBookView);

            case TYPE_SUBTITLE:
                View subtitleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtitle, parent, false);
                return new SubtitleViewHolder(subtitleView);

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

            case TYPE_HEADER: {

                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                // TODO Header

                Book book = mHeaderBooks.get(0);

                Glide.with(mContext)
                        .load(book.getThumbnailURL())
                        .crossFade()
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .into(headerViewHolder.mBookImage);

                headerViewHolder.mBookName.setText(book.getName());
                headerViewHolder.mAuthor.setText(book.getAuthor());

                break;
            }

            case TYPE_SUBTITLE: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText("SUBTITLE");

                break;
            }

            case TYPE_DUAL_BOOK: {

                DualBookViewHolder dualBookViewHolder = (DualBookViewHolder) holder;

                // Left
                int leftIndex = getLeftBookIndexForPosition(position);

                final Book bookLeft = mFeedBooks.get(leftIndex);

                dualBookViewHolder.mPresentationalLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, BookActivity.class);
                        intent.putExtra("book", bookLeft);
                        mContext.startActivity(intent);
                    }
                });

                Glide.with(mContext)
                        .load(bookLeft.getImageURL())
                        .crossFade()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_book)
                        .into(dualBookViewHolder.mBookImageLeft);

                dualBookViewHolder.mBookNameLeft.setText(bookLeft.getName());
                dualBookViewHolder.mAuthorLeft.setText(bookLeft.getAuthor());

                // Right
                final Book bookRight = mFeedBooks.get(leftIndex + 1); // Rigth

                dualBookViewHolder.mPresentationalRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, BookActivity.class);
                        intent.putExtra("book", bookRight);
                        mContext.startActivity(intent);
                    }
                });


                Glide.with(mContext)
                        .load(bookRight.getImageURL())
                        .crossFade()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_book)
                        .into(dualBookViewHolder.mBookImageRight);

                dualBookViewHolder.mBookNameRight.setText(bookRight.getName());
                dualBookViewHolder.mAuthorRight.setText(bookRight.getAuthor());



                break;
            }

            case TYPE_FOOTER: {

                FooterViewHolder footerHolder = (FooterViewHolder) holder;

                if (mProgressBarActive) {
                    footerHolder.mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.mProgressBar.setVisibility(View.GONE);
                }

                break;
            }
        }
    }

    public void setProgressBarActive(boolean progressBarActive) {
        mProgressBarActive = progressBarActive;
        notifyItemChanged(getItemCount() - 1);
    }

    private int getFeedBooksSize() {
        return mFeedBooks != null ? mFeedBooks.size() : 0;
    }

    private int getDualRowsCount() {
        if (getFeedBooksSize() > 0)
            return (getFeedBooksSize() - 1) / 2 + 1;
        else
            return 0;
    }

    private int getLeftBookIndexForPosition(int position) {
        // a = position - header - subtitle
        // b = a * 2 double books

        return ((position - 2) * 2);
    }
}
