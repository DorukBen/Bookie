package com.karambit.bookie.adapter;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.karambit.bookie.BookActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.infinite_viewpager.HorizontalInfiniteCycleViewPager;
import com.karambit.bookie.helper.pull_refresh_layout.SmartisanProgressBarDrawable;
import com.karambit.bookie.model.Book;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by orcan on 10/16/16.
 */

public class HomeTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = HomeTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DUAL_BOOK = 1;
    private static final int TYPE_FOOTER = 2;

    private static final int TYPE_NO_CONNECTION = 3;
    private static final int TYPE_UNKNOWN_ERROR = 4;

    public static final int ERROR_TYPE_NONE = 0;
    public static final int ERROR_TYPE_NO_CONNECTION = 1;
    public static final int ERROR_TYPE_UNKNOWN_ERROR = 2;
    private int mErrorType = ERROR_TYPE_NONE;

    private Context mContext;
    private ArrayList<Book> mHeaderBooks = new ArrayList<>();
    private ArrayList<Book> mFeedBooks = new ArrayList<>();

    private boolean mProgressBarActive;
    private HorizontalPagerAdapter mHorizontalPagerAdapter;
    private BookClickListener mBookClickListener;

    public HomeTimelineAdapter(Context context) {
        mContext = context;
        mProgressBarActive = false;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private HorizontalInfiniteCycleViewPager mCycleViewPager;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mCycleViewPager = (HorizontalInfiniteCycleViewPager) headerView.findViewById(R.id.hicvp);
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

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar mProgressBar;

        private FooterViewHolder(View footerView) {
            super(footerView);

            mProgressBar = (ProgressBar) footerView.findViewById(R.id.footerProgressBar);
        }
    }

    private static class NoConnectionViewHolder extends RecyclerView.ViewHolder {

        private ImageView mNoConnectionImageView;
        private TextView mNoConnectionTextView;

        private NoConnectionViewHolder(View noConnectionView) {
            super(noConnectionView);

            mNoConnectionImageView = (ImageView) noConnectionView.findViewById(R.id.emptyStateImageView);
            mNoConnectionTextView = (TextView) noConnectionView.findViewById(R.id.emptyStateTextView);
        }
    }

    private static class UnknownErrorViewHolder extends RecyclerView.ViewHolder {

        private ImageView mUnknownErrorImageView;
        private TextView mUnknownErrorTextView;

        private UnknownErrorViewHolder(View unknownErrorView) {
            super(unknownErrorView);

            mUnknownErrorImageView = (ImageView) unknownErrorView.findViewById(R.id.emptyStateImageView);
            mUnknownErrorTextView = (TextView) unknownErrorView.findViewById(R.id.emptyStateTextView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return getDualRowsCount() + 2; // (Dual row count + 1 row) + Header + Footer
    }

    /*
        HEADER
        DUAL BOOKS
        FOOTER
     */
    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            if (mErrorType != ERROR_TYPE_NONE){
                if (mErrorType == ERROR_TYPE_NO_CONNECTION){
                    return TYPE_NO_CONNECTION;
                }else {
                    return TYPE_UNKNOWN_ERROR;
                }
            }else {
                return TYPE_HEADER;
            }
        } else if (position < getDualRowsCount() + 1) { // + Header
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

            case TYPE_FOOTER:
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer, parent, false);
                return new FooterViewHolder(footerView);

            case TYPE_NO_CONNECTION:
                View noConnectionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_state, parent, false);
                return new NoConnectionViewHolder(noConnectionView);

            case TYPE_UNKNOWN_ERROR:
                View unknownErrorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_state, parent, false);
                return new UnknownErrorViewHolder(unknownErrorView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_HEADER: {

                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                if (mHorizontalPagerAdapter != null){
                    mHorizontalPagerAdapter.setBooks(mHeaderBooks);
                    headerViewHolder.mCycleViewPager.notifyDataSetChanged();
                    headerViewHolder.mCycleViewPager.setInfiniteCyclerManagerPagerAdapter(mHorizontalPagerAdapter);
                }else{
                    mHorizontalPagerAdapter = new HorizontalPagerAdapter(mContext, mHeaderBooks);
                    headerViewHolder.mCycleViewPager.setAdapter(mHorizontalPagerAdapter);
                }

                mHorizontalPagerAdapter.setBookClickListener(new HorizontalPagerAdapter.BookClickListener() {
                    @Override
                    public void onBookClick(Book book) {
                        mBookClickListener.onBookClick(book);
                    }
                });

                break;
            }

            case TYPE_DUAL_BOOK: {

                final DualBookViewHolder dualBookViewHolder = (DualBookViewHolder) holder;

                // Left
                int leftIndex = getLeftBookIndexForPosition(position);

                final Book bookLeft = mFeedBooks.get(leftIndex);

                dualBookViewHolder.mPresentationalLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBookClickListener.onBookClick(bookLeft);
                    }
                });

                Glide.with(mContext)
                     .load(bookLeft.getThumbnailURL())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_192dp)
                     .error(R.drawable.error_192dp)
                     .into(new SimpleTarget<Bitmap>() {
                         @Override
                         public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                             Bitmap croppedBitmap = ImageScaler.cropImage(resource, 72 / 96f);
                             dualBookViewHolder.mBookImageLeft.setImageBitmap(croppedBitmap);
                         }
                     });

                dualBookViewHolder.mBookNameLeft.setText(bookLeft.getName());
                dualBookViewHolder.mAuthorLeft.setText(bookLeft.getAuthor());

                // Right
                final Book bookRight = mFeedBooks.get(leftIndex + 1); // Rigth

                dualBookViewHolder.mPresentationalRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBookClickListener.onBookClick(bookRight);
                    }
                });


                Glide.with(mContext)
                     .load(bookRight.getThumbnailURL())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_192dp)
                     .error(R.drawable.error_192dp)
                     .into(new SimpleTarget<Bitmap>() {
                         @Override
                         public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                             Bitmap croppedBitmap = ImageScaler.cropImage(resource, 72 / 96f);
                             dualBookViewHolder.mBookImageRight.setImageBitmap(croppedBitmap);
                         }
                     });

                dualBookViewHolder.mBookNameRight.setText(bookRight.getName());
                dualBookViewHolder.mAuthorRight.setText(bookRight.getAuthor());


                break;
            }

            case TYPE_FOOTER: {

                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.mProgressBar.setIndeterminateDrawable(new SmartisanProgressBarDrawable(mContext));

                if (mProgressBarActive) {
                    footerHolder.mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.mProgressBar.setVisibility(View.INVISIBLE);
                }

                break;
            }

            case TYPE_NO_CONNECTION: {

                NoConnectionViewHolder noConnectionViewHolder = (NoConnectionViewHolder) holder;
                noConnectionViewHolder.mNoConnectionTextView.setText(mContext.getString(R.string.no_internet_connection));

                break;
            }

            case TYPE_UNKNOWN_ERROR: {

                UnknownErrorViewHolder unknownErrorViewHolder = (UnknownErrorViewHolder) holder;
                unknownErrorViewHolder.mUnknownErrorTextView.setText(mContext.getString(R.string.unknown_error));

                break;
            }
        }
    }

    public void setHeaderAndFeedBooks(ArrayList<Book> headerBooks, ArrayList<Book> feedBooks){
        mHeaderBooks = headerBooks;
        mFeedBooks = feedBooks;
        setProgressBarActive(true);
        notifyDataSetChanged();
    }

    public void addFeedBooks(ArrayList<Book> feedBooks){
        mFeedBooks.addAll(feedBooks);
        notifyDataSetChanged();
    }

    public void setProgressBarActive(boolean progressBarActive) {
        mProgressBarActive = progressBarActive;
        notifyItemChanged(getItemCount() - 1);
    }

    public boolean isAdapterHasFeedBooks(){
        return getFeedBooksSize() != 0;
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
        // a = position - header
        // b = a * 2 double books

        return ((position - 1) * 2);
    }

    public void setError(int errorType){
        mErrorType = errorType;
        if (errorType != ERROR_TYPE_NONE){
            mFeedBooks.clear();
            setProgressBarActive(false);
            notifyDataSetChanged();
        }
    }

    public interface BookClickListener {
        void onBookClick(Book book);
    }

    public BookClickListener getBookClickListener() {
        return mBookClickListener;
    }

    public void setBookClickListener(BookClickListener bookClickListener) {
        mBookClickListener = bookClickListener;
    }
}
