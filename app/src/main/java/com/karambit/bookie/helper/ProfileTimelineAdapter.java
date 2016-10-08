package com.karambit.bookie.helper;

import android.content.Context;
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
import com.karambit.bookie.R;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

/**
 * Created by orcan on 10/7/16.
 */

public class ProfileTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = ProfileTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CURRENTLY_READING = 1;
    private static final int TYPE_READED_BOOKS = 3;
    private static final int TYPE_BOOKS_ON_HAND = 4;
    private static final int TYPE_SUBTITLE_BOOKS_ON_HAND = 5;
    private static final int TYPE_SUBTITLE_READED_BOOKS = 6;
    private static final int TYPE_FOOTER = 7;

    private Context mContext;
    private User.Details mUserDetails;

    private BookClickListener mBookClickListener;
    private HeaderClickListeners mHeaderClickListeners;

    private boolean mProgressBarActive;

    public ProfileTimelineAdapter(Context context, User.Details userDetails) {
        mContext = context;
        mUserDetails = userDetails;

        mProgressBarActive = false;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mBio;
        private TextView mLocation;
        private TextView mReadedBooks;
        private TextView mPoint;
        private TextView mSharedBooks;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mProfilePicture = (CircleImageView) headerView.findViewById(R.id.profilePictureHeaderCircleImageView);
            mUserName = (TextView) headerView.findViewById(R.id.userNameHeaderTextView);
            mBio = (TextView) headerView.findViewById(R.id.bioHeaderTextView);
            mLocation = (TextView) headerView.findViewById(R.id.locationHeaderTextView);
            mReadedBooks = (TextView) headerView.findViewById(R.id.readedBooksHeaderTextView);
            mPoint = (TextView) headerView.findViewById(R.id.pointTextView);
            mSharedBooks = (TextView) headerView.findViewById(R.id.sharedBooksTextView);
        }
    }

    // TODO Currently reading setup with ViewPager
    private static class CurrentlyReadingViewHolder extends RecyclerView.ViewHolder {

        private CurrentlyReadingViewHolder(View currentlyReadingView) {
            super(currentlyReadingView);

        }
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
        return mUserDetails.getBooksOnHandCount() + mUserDetails.getReadedBooksCount() + 5; // + Header + Currently Reading + Subtitle + Subtitle + Footer)
    }

    /*
        HEADER
        CURRENTLY READING
        SUBTITLE BOOKS ON HAND
        BOOKS ON HAND
        SUBTITLE READED BOOKS
        READED BOOKS
        FOOTER
     */
    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return TYPE_HEADER;

        } else if (position == 1) {
            return TYPE_CURRENTLY_READING;

        } else if (position == 2) {
            return TYPE_SUBTITLE_BOOKS_ON_HAND;

        } else if (position < mUserDetails.getBooksOnHandCount() + 3) {
            return TYPE_BOOKS_ON_HAND;

        } else if (position == mUserDetails.getBooksOnHandCount() + 3) {
            return TYPE_SUBTITLE_READED_BOOKS;

        } else if (position < mUserDetails.getBooksOnHandCount() + mUserDetails.getReadedBooksCount() + 4) {
            return TYPE_READED_BOOKS;

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
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_profile_timeline, parent, false);
                return new HeaderViewHolder(headerView);

            case TYPE_CURRENTLY_READING:
                View currentlyReadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currently_reading_profile_timeline, parent, false);
                return new CurrentlyReadingViewHolder(currentlyReadingView);

            case TYPE_BOOKS_ON_HAND:case TYPE_READED_BOOKS:
                View bookView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
                return new BookViewHolder(bookView);

            case TYPE_SUBTITLE_BOOKS_ON_HAND:case TYPE_SUBTITLE_READED_BOOKS:
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

                if (mHeaderClickListeners != null) {
                    headerViewHolder.mProfilePicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListeners.onProfilePictureClick(mUserDetails);
                        }
                    });
                }

                Glide.with(mContext)
                        .load(mUserDetails.getUser().getThumbnailUrl())
                        .asBitmap()
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .into(headerViewHolder.mProfilePicture);
                headerViewHolder.mUserName.setText(mUserDetails.getUser().getName());
                headerViewHolder.mBio.setText(mUserDetails.getBio());

                headerViewHolder.mLocation.setText("Location"); //TODO Location

                if (mHeaderClickListeners != null) {
                    headerViewHolder.mProfilePicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListeners.onLocationClick(mUserDetails);
                        }
                    });
                }

                headerViewHolder.mReadedBooks.setText(String.valueOf(mUserDetails.getReadedBooksCount()));
                headerViewHolder.mPoint.setText(String.valueOf(mUserDetails.getPoint()));
                headerViewHolder.mSharedBooks.setText(String.valueOf(mUserDetails.getSharedBooksCount()));

                break;
            }

            case TYPE_CURRENTLY_READING: {

                // TODO Currently Reading ViewPager

                break;
            }

            case TYPE_SUBTITLE_BOOKS_ON_HAND: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(mContext.getString(R.string.x_books_on_hand, String.valueOf(mUserDetails.getBooksOnHandCount())));

                break;
            }

            case TYPE_BOOKS_ON_HAND: {

                BookViewHolder bookHolder = (BookViewHolder) holder;

                final Book book = mUserDetails.getBooksOnHand().get(position - 3); // - Header - Currently Reading - Subtitle

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
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .crossFade()
                        .into(bookHolder.mBookImage);

                break;
            }

            case TYPE_SUBTITLE_READED_BOOKS: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(mContext.getString(R.string.x_books_readed, String.valueOf(mUserDetails.getReadedBooksCount())));

                break;
            }

            case TYPE_READED_BOOKS: {

                BookViewHolder bookHolder = (BookViewHolder) holder;

                int arrayPosition = position - mUserDetails.getBooksOnHandCount() - 4; // - Header - Currently Reading - Subtitle - Subtitle
                final Book book = mUserDetails.getReadedBooks().get(arrayPosition);

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
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .crossFade()
                        .into(bookHolder.mBookImage);

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

    public void setProgressBar(boolean active) {
        mProgressBarActive = active;
        notifyDataSetChanged();
    }

    public BookClickListener getBookClickListener() {
        return mBookClickListener;
    }

    public void setBookClickListener(BookClickListener bookClickListener) {
        mBookClickListener = bookClickListener;
    }

    public HeaderClickListeners getHeaderClickListeners() {
        return mHeaderClickListeners;
    }

    public void setHeaderClickListeners(HeaderClickListeners headerClickListeners) {
        mHeaderClickListeners = headerClickListeners;
    }

    public interface HeaderClickListeners {
        void onProfilePictureClick(User.Details details);
        void onLocationClick(User.Details details);
    }

    public interface BookClickListener {
        void onBookClick(Book book);
    }


}
