package com.karambit.bookie.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.LayoutUtils;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.infinite_viewpager.HorizontalInfiniteCycleViewPager;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by orcan on 10/7/16.
 */

public class ProfileTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = ProfileTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CURRENTLY_READING = 1;
    private static final int TYPE_READ_BOOKS = 3;
    private static final int TYPE_BOOKS_ON_HAND = 4;
    private static final int TYPE_SUBTITLE_BOOKS_ON_HAND = 5;
    private static final int TYPE_SUBTITLE_READ_BOOKS = 6;
    private static final int TYPE_FOOTER = 7;
    private static final int TYPE_START_READING = 8;
    private static final int TYPE_EMPTY_STATE = 9;

    private Context mContext;
    private User.Details mUserDetails;

    private BookClickListener mBookClickListener;
    private HeaderClickListeners mHeaderClickListeners;
    private StartReadingClickListener mStartReadingClickListener;

    private boolean mProgressBarActive;

    public ProfileTimelineAdapter(Context context, User.Details userDetails) {
        mContext = context;
        mUserDetails = userDetails;

        mProgressBarActive = false;
    }

    public ProfileTimelineAdapter(Context context) {
        mContext = context;

        mProgressBarActive = true;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mBio;
        private TextView mLocation;
        private TextView mReadBooks;
        private TextView mPoint;
        private TextView mSharedBooks;
        private TextView mVerificationIndicator;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mProfilePicture = (CircleImageView) headerView.findViewById(R.id.profilePictureHeaderCircleImageView);
            mUserName = (TextView) headerView.findViewById(R.id.userNameHeaderTextView);
            mBio = (TextView) headerView.findViewById(R.id.bioHeaderTextView);
            mLocation = (TextView) headerView.findViewById(R.id.locationHeaderTextView);
            mReadBooks = (TextView) headerView.findViewById(R.id.readBooksHeaderTextView);
            mPoint = (TextView) headerView.findViewById(R.id.pointTextView);
            mSharedBooks = (TextView) headerView.findViewById(R.id.sharedBooksTextView);
            mVerificationIndicator = (TextView) headerView.findViewById(R.id.verificationIndicatorHeaderTextView);
        }
    }

    private static class CurrentlyReadingViewHolder extends RecyclerView.ViewHolder {

        HorizontalInfiniteCycleViewPager mCycleViewPager;

        private CurrentlyReadingViewHolder(View currentlyReadingView) {
            super(currentlyReadingView);

            mCycleViewPager = (HorizontalInfiniteCycleViewPager) currentlyReadingView.findViewById(R.id.hicvp);
        }
    }

    private static class StartReadingViewHolder extends RecyclerView.ViewHolder {

        Button mStartReadingButton;

        public StartReadingViewHolder(View itemView) {
            super(itemView);

            mStartReadingButton = (Button) itemView.findViewById(R.id.startReadingButton);
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mUserDetails != null) {

            int increment = 0;

            // Subtitle
            if (mUserDetails.getBooksOnHandCount() > 0) {
                increment++;
            }

            // Subtitle
            if (mUserDetails.getReadBooksCount() > 0) {
                increment++;
            }

            // Currently reading
            if (mUserDetails.getUser().getID() == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID() ||
                mUserDetails.getCurrentlyReadingCount() != 0 ||
                (mUserDetails.getBooksOnHandCount() < 0 && mUserDetails.getReadBooksCount() < 0)) {

                increment++;
            }

            return mUserDetails.getBooksOnHandCount() + mUserDetails.getReadBooksCount() + increment + 2; // Header + Footer

        } else {
            return 1; // Footer
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mUserDetails != null) {

            if (position == 0) {
                return TYPE_HEADER;

            } else if (mUserDetails.getCurrentlyReadingCount() > 0) {

                if (position == 1) {
                    return TYPE_CURRENTLY_READING;

                } else if (mUserDetails.getBooksOnHandCount() > 0 && mUserDetails.getReadBooksCount() > 0) {

                    if (position == 2) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_BOOKS_ON_HAND;

                    } else if (position == mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getBooksOnHandCount() + mUserDetails.getReadBooksCount() + 4) {
                        return TYPE_READ_BOOKS;
                    }

                } else if (mUserDetails.getBooksOnHandCount() > 0) {

                    if (position == 2) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_BOOKS_ON_HAND;
                    }

                } else if (mUserDetails.getReadBooksCount() > 0) {

                    if (position == 2) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getReadBooksCount() + 3) {
                        return TYPE_READ_BOOKS;
                    }
                }

            } else if (mUserDetails.getUser().getID() == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID()) { // Currently reading section

                if (position == 1) {
                    return TYPE_START_READING;

                } else if (mUserDetails.getBooksOnHandCount() > 0 && mUserDetails.getReadBooksCount() > 0) {

                    if (position == 2) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_BOOKS_ON_HAND;

                    } else if (position == mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getBooksOnHandCount() + mUserDetails.getReadBooksCount() + 4) {
                        return TYPE_READ_BOOKS;
                    }

                } else if (mUserDetails.getBooksOnHandCount() > 0) {

                    if (position == 2) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 3) {
                        return TYPE_BOOKS_ON_HAND;
                    }

                } else if (mUserDetails.getReadBooksCount() > 0) {


                    if (position == 2) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getReadBooksCount() + 3) {
                        return TYPE_READ_BOOKS;
                    }
                }

            } else {

                if (mUserDetails.getBooksOnHandCount() > 0 && mUserDetails.getReadBooksCount() > 0) {

                    if (position == 1) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 2) {
                        return TYPE_BOOKS_ON_HAND;

                    } else if (position == mUserDetails.getBooksOnHandCount() + 2) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getBooksOnHandCount() + mUserDetails.getReadBooksCount() + 3) {
                        return TYPE_READ_BOOKS;
                    }

                } else if (mUserDetails.getBooksOnHandCount() > 0) {

                    if (position == 1) {
                        return TYPE_SUBTITLE_BOOKS_ON_HAND;

                    } else if (position < mUserDetails.getBooksOnHandCount() + 2) {
                        return TYPE_BOOKS_ON_HAND;
                    }

                } else if (mUserDetails.getReadBooksCount() > 0) {

                    if (position == 1) {
                        return TYPE_SUBTITLE_READ_BOOKS;

                    } else if (position < mUserDetails.getReadBooksCount() + 2) {
                        return TYPE_READ_BOOKS;
                    }

                } else {

                    if (position == 1) {
                        return TYPE_EMPTY_STATE;
                    }
                }
            }

            if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }

        } else {
            return TYPE_FOOTER;
        }

        throw new IllegalArgumentException("Invalid type at position: " + position);
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

            case TYPE_START_READING:
                View startReadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_start_reading_profile_timeline, parent, false);
                return new StartReadingViewHolder(startReadingView);

            case TYPE_EMPTY_STATE:
                View emptyStateView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_state, parent, false);
                return new RecyclerView.ViewHolder(emptyStateView) {};

            case TYPE_BOOKS_ON_HAND: case TYPE_READ_BOOKS:
                View bookView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
                return new BookViewHolder(bookView);

            case TYPE_SUBTITLE_BOOKS_ON_HAND: case TYPE_SUBTITLE_READ_BOOKS:
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

                //
                // Listener setup
                //

                if (mHeaderClickListeners != null &&
                    !TextUtils.isEmpty(mUserDetails.getUser().getThumbnailUrl()) &&
                    !TextUtils.isEmpty(mUserDetails.getUser().getImageUrl())) {

                    headerViewHolder.mLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListeners.onLocationClick(mUserDetails);
                        }
                    });

                    headerViewHolder.mProfilePicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListeners.onProfilePictureClick(mUserDetails);
                        }
                    });
                }

                //
                // Verification indication
                //

                if (!mUserDetails.isVerified()) {
                    headerViewHolder.mVerificationIndicator.setVisibility(View.VISIBLE);
                } else {
                    headerViewHolder.mVerificationIndicator.setVisibility(View.GONE);
                }

                Glide.with(mContext)
                     .load(mUserDetails.getUser().getThumbnailUrl())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_book)
                     .centerCrop()
                     .into(headerViewHolder.mProfilePicture);

                headerViewHolder.mUserName.setText(mUserDetails.getUser().getName());

                //
                // Bio
                //

                String bio = mUserDetails.getBio();
                if (!TextUtils.isEmpty(bio)) {
                    headerViewHolder.mBio.setVisibility(View.VISIBLE);
                    headerViewHolder.mBio.setText(bio);
                } else {
                    headerViewHolder.mBio.setVisibility(View.GONE);
                }

                //
                // Location
                //

                double latitude = mUserDetails.getUser().getLatitude();
                double longitude = mUserDetails.getUser().getLongitude();

                if (latitude != -1 && longitude != -1) {

                    try {
                        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                        // Admin area equals Istanbul
                        // Subadmin are equals Bahçelievler

                        if (addresses.size() > 0) {
                            String locationString = "";

                            String subAdminArea = addresses.get(0).getSubAdminArea();
                            if (subAdminArea != null && !subAdminArea.equals("null")) {
                                locationString += subAdminArea + " / ";
                            }

                            String adminArea = addresses.get(0).getAdminArea();
                            if (adminArea != null && !adminArea.equals("null")) {
                                locationString += adminArea;
                            }

                            if (TextUtils.isEmpty(locationString)) {
                                headerViewHolder.mLocation.setVisibility(View.GONE);
                            } else {
                                headerViewHolder.mLocation.setText(locationString);
                                headerViewHolder.mLocation.setVisibility(View.VISIBLE);
                            }

                        } else {
                            headerViewHolder.mLocation.setVisibility(View.GONE);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();

                        headerViewHolder.mLocation.setVisibility(View.GONE);
                    }
                } else {
                    headerViewHolder.mLocation.setVisibility(View.GONE);
                }

                headerViewHolder.mReadBooks.setText(String.valueOf(mUserDetails.getReadBooksCount()));
                headerViewHolder.mPoint.setText(String.valueOf(mUserDetails.getPoint()));
                headerViewHolder.mSharedBooks.setText(String.valueOf(mUserDetails.getSharedBooksCount()));

                break;
            }

            case TYPE_CURRENTLY_READING: {

                CurrentlyReadingViewHolder currentlyReadingHolder = (CurrentlyReadingViewHolder) holder;

                HorizontalPagerAdapter adapter = new HorizontalPagerAdapter(mContext, mUserDetails.getCurrentlyReading());
                currentlyReadingHolder.mCycleViewPager.setAdapter(adapter);

                break;
            }

            case TYPE_START_READING: {
                StartReadingViewHolder startReadingHolder = (StartReadingViewHolder) holder;

                startReadingHolder.mStartReadingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mStartReadingClickListener != null) {
                            mStartReadingClickListener.onStartReadingClick(mUserDetails);
                        }
                    }
                });

                break;
            }

            case TYPE_SUBTITLE_BOOKS_ON_HAND: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(R.string.books_on_hand);

                break;
            }

            case TYPE_BOOKS_ON_HAND: {

                final BookViewHolder bookHolder = (BookViewHolder) holder;

                final Book book = mUserDetails.getBooksOnHand().get(position - calculateBooksOnHandOffset());

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
                     .placeholder(R.drawable.placeholder_book)
                     .into(new SimpleTarget<Bitmap>() {
                         @Override
                         public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                             Bitmap croppedBitmap = ImageScaler.cropImage(resource, 72 / 96f);
                             bookHolder.mBookImage.setImageBitmap(croppedBitmap);
                         }
                     });

                break;
            }

            case TYPE_SUBTITLE_READ_BOOKS: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(mContext.getString(R.string.read_books));

                break;
            }

            case TYPE_READ_BOOKS: {

                final BookViewHolder bookHolder = (BookViewHolder) holder;

                final Book book = mUserDetails.getReadBooks().get(position - calculateReadBooksOffset());

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
                     .placeholder(R.drawable.placeholder_book)
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

                if (mProgressBarActive) {
                    footerHolder.mTextView.setVisibility(View.VISIBLE);
                    footerHolder.mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.mTextView.setVisibility(View.GONE);
                    footerHolder.mProgressBar.setVisibility(View.GONE);
                }

                break;
            }
        }
    }

    private int calculateBooksOnHandOffset() {
        int offset = 1; // Header

        // Currently reading
        if (mUserDetails.getUser().getID() == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID() ||
            mUserDetails.getCurrentlyReadingCount() != 0 ||
            (mUserDetails.getBooksOnHandCount() < 0 && mUserDetails.getReadBooksCount() < 0)) {

            offset++;
        }

        if (mUserDetails.getBooksOnHandCount() > 0) {
            offset++;
        }

        return offset;
    }

    private int calculateReadBooksOffset() {
        int offset = 1; // Header

        // Currently reading
        if (mUserDetails.getUser().getID() == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID() ||
            mUserDetails.getCurrentlyReadingCount() != 0 ||
            (mUserDetails.getBooksOnHandCount() < 0 && mUserDetails.getReadBooksCount() < 0)) {

            offset++;
        }

        if (mUserDetails.getBooksOnHandCount() > 0) {
            offset++;
        }

        if (mUserDetails.getReadBooksCount() > 0) {
            offset++;
        }

        return offset + mUserDetails.getBooksOnHandCount();
    }

    public User.Details getUserDetails() {
        return mUserDetails;
    }

    public void setUserDetails(User.Details userDetails) {
        mUserDetails = userDetails;
        notifyDataSetChanged();
    }

    public void setProgressBar(boolean active) {
        mProgressBarActive = active;
        notifyItemChanged(getItemCount() - 1);
    }

    public interface HeaderClickListeners {
        void onProfilePictureClick(User.Details details);

        void onLocationClick(User.Details details);
    }

    public interface BookClickListener {
        void onBookClick(Book book);
    }

    public interface StartReadingClickListener {
        void onStartReadingClick(User.Details userDetails);
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

    public StartReadingClickListener getStartReadingClickListener() {
        return mStartReadingClickListener;
    }

    public void setStartReadingClickListener(StartReadingClickListener startReadingClickListener) {
        mStartReadingClickListener = startReadingClickListener;
    }
}
