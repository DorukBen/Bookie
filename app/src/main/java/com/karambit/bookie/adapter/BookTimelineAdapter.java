package com.karambit.bookie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.DurationTextUtils;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.SmartisanProgressBarDrawable;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by orcan on 10/12/16.
 */

public class BookTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = BookTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STATE_SECTION_CURRENT_USER = 1;
    private static final int TYPE_STATE_SECTION_OTHER_USER = 2;
    private static final int TYPE_BOOK_PROCESS = 3;
    private static final int TYPE_SUBTITLE = 4;
    private static final int TYPE_FOOTER = 5;
    private static final int TYPE_NO_CONNECTION = 6;
    private static final int TYPE_UNKNOWN_ERROR = 7;

    public static final int ERROR_TYPE_NONE = 0;
    public static final int ERROR_TYPE_NO_CONNECTION = 1;
    public static final int ERROR_TYPE_UNKNOWN_ERROR = 2;
    private int mErrorType = ERROR_TYPE_NONE;

    private Context mContext;
    private Book mBook;
    private Book.Details mBookDetails;

    private HeaderClickListeners mHeaderClickListeners;
    private StateCurrentUserClickListeners mCurrentUserClickListeners;
    private StateOtherUserClickListeners mOtherUserClickListeners;

    private boolean mProgressBarActive;
    private SpanTextClickListeners mSpanTextClickListener;

    public BookTimelineAdapter(Context context, Book book) {
        mContext = context;
        mBook = book;
        mBookDetails = null;

        mProgressBarActive = true;
    }

    public BookTimelineAdapter(Context context) {
        mContext = context;

        mProgressBarActive = true;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private ImageView mBookPicture;
        private TextView mBookName;
        private TextView mAuthor;
        private TextView mGenre;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mBookPicture = (ImageView) headerView.findViewById(R.id.bookPictureHeaderImageView);
            mBookName = (TextView) headerView.findViewById(R.id.bookNameHeaderTextView);
            mAuthor = (TextView) headerView.findViewById(R.id.authorHeaderTextView);
            mGenre = (TextView) headerView.findViewById(R.id.genreHeaderTextView);
        }
    }

    private static class StateSectionOtherUserViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView mOwnerPicture;
        private TextView mOwnerName;
        private View mOwnerClickArea;
        private TextView mBookState;
        private Button mRequest;

        private StateSectionOtherUserViewHolder(View stateSectionView) {
            super(stateSectionView);

            mOwnerPicture = (CircleImageView) stateSectionView.findViewById(R.id.ownerPictureCircleImageView);
            mOwnerName = (TextView) stateSectionView.findViewById(R.id.ownerNameTextView);
            mOwnerClickArea = stateSectionView.findViewById(R.id.ownerClickAreaRelativeLayout);
            mBookState = (TextView) stateSectionView.findViewById(R.id.bookStateTextView);
            mRequest = (Button) stateSectionView.findViewById(R.id.requestButton);
        }
    }

    private static class StateSectionCurrentUserViewHolder extends RecyclerView.ViewHolder {
        private View mStateClickArea;
        private ImageView mStateIcon;
        private TextView mStateText;
        private View mRequestClickArea;
        private TextView mRequestCount;
        private TextView mStateDuration;

        private StateSectionCurrentUserViewHolder(View stateSectionView) {
            super(stateSectionView);

            mStateClickArea = stateSectionView.findViewById(R.id.stateClickArea);
            mStateIcon = (ImageView) stateSectionView.findViewById(R.id.bookStateImageView);
            mStateText = (TextView) stateSectionView.findViewById(R.id.bookStateTextView);
            mRequestClickArea = stateSectionView.findViewById(R.id.requestsClickArea);
            mRequestCount = (TextView) stateSectionView.findViewById(R.id.requestCountTextView);
            mStateDuration = (TextView) stateSectionView.findViewById(R.id.stateDurationTextView);
        }
    }

    private static class BookProcessViewHolder extends RecyclerView.ViewHolder {
        private ImageView mProcessImage;
        private TextView mProcessChange;
        private View mTopLine;
        private View mBottomLine;
        private TextView mCreatedAt;

        private BookProcessViewHolder(View itemView) {
            super(itemView);
            mProcessImage = (ImageView) itemView.findViewById(R.id.bookProcessImageView);
            mProcessChange = (TextView) itemView.findViewById(R.id.bookProcessChangeTextView);
            mTopLine = itemView.findViewById(R.id.topLineView);
            mBottomLine = itemView.findViewById(R.id.bottomLineView);
            mCreatedAt = (TextView) itemView.findViewById(R.id.createdAtBookProcess);
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

        private FooterViewHolder(View footerView) {
            super(footerView);

            mProgressBar = (ProgressBar) footerView.findViewById(R.id.footerProgressBar);
        }
    }

    private static class NoConnectionViewHolder extends RecyclerView.ViewHolder {

        private TextView mNoConnectionTextView;

        private NoConnectionViewHolder(View noConnectionView) {
            super(noConnectionView);

            mNoConnectionTextView = (TextView) noConnectionView.findViewById(R.id.errorTextView);
        }
    }

    private static class UnknownErrorViewHolder extends RecyclerView.ViewHolder {

        private TextView mUnknownErrorTextView;

        private UnknownErrorViewHolder(View unkonwnErrorView) {
            super(unkonwnErrorView);

            mUnknownErrorTextView = (TextView) unkonwnErrorView.findViewById(R.id.errorTextView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        if (mBookDetails != null) {
            int size = mBookDetails.getBookProcesses().size();
            return size + 4; // Header + State + Subtitle + Footer
        } else {
            if (mBook != null){
                return 3; // Header + State + Footer
            }else{
                return 1; // Error
            }
        }
    }

    /*
    if (mBookDetails == null)

        if (mBookDetails.getBookProcesses().size() > 0)
            HEADER
            STATE
            SUBTITLE
            BOOK PROCESSES
            FOOTER

        else
            HEADER
            EMPTY STATE
            FOOTER

     else
        FOOTER
     */
    @Override
    public int getItemViewType(int position) {

        if (mBookDetails != null) {

            if (position == 0) {
                return TYPE_HEADER;

            } else if (position == 1) {

                if (SessionManager.getCurrentUser(mContext).getID() == mBookDetails.getBook().getOwner().getID()) {
                    return TYPE_STATE_SECTION_CURRENT_USER;

                } else {
                    return TYPE_STATE_SECTION_OTHER_USER;
                }

            } else if (position == 2) {
                return TYPE_SUBTITLE;

            } else if (position < mBookDetails.getBookProcesses().size() + 3) { // + Header + State + Subtitle
                return TYPE_BOOK_PROCESS;

            } else if (position == getItemCount() - 1) {
                return TYPE_FOOTER;

            } else {
                throw new IllegalArgumentException("Invalid view type at position " + position);
            }

        }else if (mBook != null){

            if (position == 0) {
                return TYPE_HEADER;

            } else if (position == 1) {

                if (SessionManager.getCurrentUser(mContext).getID() == mBook.getOwner().getID()) {
                    return TYPE_STATE_SECTION_CURRENT_USER;

                } else {
                    return TYPE_STATE_SECTION_OTHER_USER;
                }

            }else if (position == getItemCount() - 1) {
                if (mErrorType != ERROR_TYPE_NONE){
                    if (mErrorType == ERROR_TYPE_NO_CONNECTION){
                        return TYPE_NO_CONNECTION;
                    }else {
                        return TYPE_UNKNOWN_ERROR;
                    }
                }else {
                    return TYPE_FOOTER;
                }

            } else {
                throw new IllegalArgumentException("Invalid view type at position " + position);
            }

        } else {
            return TYPE_FOOTER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //View inflating for view types and creating ViewHolders

        switch (viewType) {

            case TYPE_HEADER:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_book_timeline, parent, false);
                return new HeaderViewHolder(headerView);

            case TYPE_STATE_SECTION_CURRENT_USER:
                View stateCurrentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_state_section_current_user_book_timeline, parent, false);
                return new StateSectionCurrentUserViewHolder(stateCurrentView);

            case TYPE_STATE_SECTION_OTHER_USER:
                View stateOtherView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_state_section_other_user_book_timeline, parent, false);
                return new StateSectionOtherUserViewHolder(stateOtherView);

            case TYPE_BOOK_PROCESS:
                View bookProcessView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_process_book_timeline, parent, false);
                return new BookProcessViewHolder(bookProcessView);

            case TYPE_SUBTITLE:
                View subtitleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtitle, parent, false);
                return new SubtitleViewHolder(subtitleView);

            case TYPE_FOOTER:
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer, parent, false);
                return new FooterViewHolder(footerView);

            case TYPE_NO_CONNECTION:
                View noConnectionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_error, parent, false);
                return new NoConnectionViewHolder(noConnectionView);

            case TYPE_UNKNOWN_ERROR:
                View unknownErrorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_error, parent, false);
                return new UnknownErrorViewHolder(unknownErrorView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_HEADER: {

                if (mBookDetails != null){
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                    // Header click listeners setup
                    if (mHeaderClickListeners != null) {

                        // Book picture click listener setup
                        headerViewHolder.mBookPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mHeaderClickListeners.onBookPictureClick(mBook);
                            }
                        });
                    }

                    Glide.with(mContext)
                            .load(mBookDetails.getBook().getThumbnailURL())
                            .asBitmap()
                            .placeholder(R.drawable.placeholder_192dp)
                            .centerCrop()
                            .error(R.drawable.error_56dp)
                            .into(headerViewHolder.mBookPicture);

                    headerViewHolder.mBookName.setText(mBookDetails.getBook().getName());
                    headerViewHolder.mAuthor.setText(mBookDetails.getBook().getAuthor());

                    String[] genres = mContext.getResources().getStringArray(R.array.genre_types);

                    if (mBookDetails.getBook().getGenreCode() >= genres.length) { // TODO Remove this section
                        mBookDetails.getBook().setGenreCode(new Random().nextInt(genres.length));
                    }

                    headerViewHolder.mGenre.setText(genres[mBookDetails.getBook().getGenreCode()]);
                }else if(mBook != null){
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                    // Header click listeners setup
                    if (mHeaderClickListeners != null) {

                        // Book picture click listener setup
                        headerViewHolder.mBookPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mHeaderClickListeners.onBookPictureClick(mBook);
                            }
                        });
                    }

                    Glide.with(mContext)
                            .load(mBook.getThumbnailURL())
                            .asBitmap()
                            .placeholder(R.drawable.placeholder_192dp)
                            .centerCrop()
                            .error(R.drawable.error_56dp)
                            .into(headerViewHolder.mBookPicture);

                    headerViewHolder.mBookName.setText(mBook.getName());
                    headerViewHolder.mAuthor.setText(mBook.getAuthor());


                    String[] genres = mContext.getResources().getStringArray(R.array.genre_types);

                    if (mBook.getGenreCode() >= genres.length) { // TODO Remove this section
                        mBook.setGenreCode(new Random().nextInt(genres.length));
                    }

                    headerViewHolder.mGenre.setText(genres[mBook.getGenreCode()]);
                }
                break;
            }

            case TYPE_STATE_SECTION_CURRENT_USER: {

                if (mBookDetails != null){
                    StateSectionCurrentUserViewHolder stateCurrentHolder = (StateSectionCurrentUserViewHolder) holder;

                    if (mBookDetails.getBook().getState() != Book.State.LOST && mBookDetails.getBook().getState() != Book.State.ON_ROAD) {
                        if (mCurrentUserClickListeners != null) {
                            stateCurrentHolder.mStateClickArea.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mCurrentUserClickListeners.onStateClick(mBookDetails);
                                }
                            });
                        }
                    }

                    /////////////////////////////////////////////////////////////////////////////////////////////

                    stateCurrentHolder.mStateDuration.setVisibility(View.VISIBLE);
                    stateCurrentHolder.mStateDuration.setText(getStateDurationText());

                    /////////////////////////////////////////////////////////////////////////////////////////////

                    int requestCount = getRequestCount();

                    if (requestCount > 0) {

                        stateCurrentHolder.mRequestCount.setVisibility(View.VISIBLE);

                        if (requestCount <= 9) {
                            stateCurrentHolder.mRequestCount.setText(String.valueOf(requestCount));
                        } else {
                            stateCurrentHolder.mRequestCount.setText("9+");
                        }

                        if (mCurrentUserClickListeners != null) {
                            stateCurrentHolder.mRequestCount.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mCurrentUserClickListeners.onRequestButtonClick(mBookDetails);
                                }
                            });
                        }

                    } else {
                        stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                    }

                    switch (mBookDetails.getBook().getState()) {

                        case READING:
                            stateCurrentHolder.mStateText.setText(R.string.reading);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                            break;

                        case OPENED_TO_SHARE:
                            stateCurrentHolder.mStateText.setText(R.string.opened_to_share);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                            break;

                        case CLOSED_TO_SHARE:
                            stateCurrentHolder.mStateText.setText(R.string.closed_to_share);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;

                        case ON_ROAD:
                            stateCurrentHolder.mStateText.setText(R.string.on_road);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;

                        case LOST:
                            stateCurrentHolder.mStateText.setText(R.string.lost);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_close_white_24dp);
                            stateCurrentHolder.mStateIcon.setColorFilter(Color.RED);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;
                    }
                } else if (mBook != null){
                    StateSectionCurrentUserViewHolder stateCurrentHolder = (StateSectionCurrentUserViewHolder) holder;

                    /////////////////////////////////////////////////////////////////////////////////////////////

                    stateCurrentHolder.mStateDuration.setVisibility(View.GONE);

                    /////////////////////////////////////////////////////////////////////////////////////////////


                    stateCurrentHolder.mRequestCount.setVisibility(View.GONE);


                    switch (mBook.getState()) {

                        case READING:
                            stateCurrentHolder.mStateText.setText(R.string.reading);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.VISIBLE);
                            break;

                        case OPENED_TO_SHARE:
                            stateCurrentHolder.mStateText.setText(R.string.opened_to_share);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.VISIBLE);
                            break;

                        case CLOSED_TO_SHARE:
                            stateCurrentHolder.mStateText.setText(R.string.closed_to_share);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;

                        case ON_ROAD:
                            stateCurrentHolder.mStateText.setText(R.string.on_road);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;

                        case LOST:
                            stateCurrentHolder.mStateText.setText(R.string.lost);
                            stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_close_white_24dp);
                            stateCurrentHolder.mStateIcon.setColorFilter(Color.RED);
                            stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                            break;
                    }
                }
                break;
            }

            case TYPE_STATE_SECTION_OTHER_USER: {

                if (mBookDetails != null){
                    StateSectionOtherUserViewHolder stateOtherHolder = (StateSectionOtherUserViewHolder) holder;

                    Book.State state = mBookDetails.getBook().getState();

                    if (mOtherUserClickListeners != null) {

                        stateOtherHolder.mOwnerClickArea.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOtherUserClickListeners.onOwnerClick(mBookDetails.getBook().getOwner());
                            }
                        });

                        stateOtherHolder.mRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOtherUserClickListeners.onRequestButtonClick(mBookDetails);
                            }
                        });


                        // If book is on road state and book has sent to me. The button changes to arrive button
                        if (state == Book.State.ON_ROAD) {
                            ArrayList<Book.BookProcess> bookProcesses = mBookDetails.getBookProcesses();
                            Book.BookProcess lastProcess = bookProcesses.get(bookProcesses.size() - 1);

                            if (lastProcess instanceof Book.Transaction) {
                                Book.Transaction sentTransaction = (Book.Transaction) lastProcess;

                                if (sentTransaction.getToUser() == SessionManager.getCurrentUser(mContext)) {
                                    stateOtherHolder.mRequest.setText(R.string.arrived);
                                    stateOtherHolder.mRequest.setEnabled(true);
                                    stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                                    stateOtherHolder.mRequest.setAlpha(1f);
                                    stateOtherHolder.mRequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mOtherUserClickListeners.onArrivedButtonClick(mBookDetails);
                                        }
                                    });

                                } else {
                                    stateOtherHolder.mRequest.setText(R.string.request_button);
                                    stateOtherHolder.mRequest.setEnabled(true);
                                    stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                                    stateOtherHolder.mRequest.setAlpha(1f);
                                }
                            }

                        } else if (state == Book.State.OPENED_TO_SHARE || state == Book.State.READING) {
                            stateOtherHolder.mRequest.setText(R.string.request_button);
                            stateOtherHolder.mRequest.setEnabled(true);
                            stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                            stateOtherHolder.mRequest.setAlpha(1f);
                        } else {
                            stateOtherHolder.mRequest.setText(R.string.request_button);
                            stateOtherHolder.mRequest.setEnabled(false);
                            stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                            stateOtherHolder.mRequest.setAlpha(0.5f);
                        }
                    }

                    Glide.with(mContext)
                            .load(mBookDetails.getBook().getOwner().getThumbnailUrl())
                            .asBitmap()
                            .placeholder(R.drawable.placeholder_56dp)
                            .centerCrop()
                            .error(R.drawable.error_56dp)
                            .into(stateOtherHolder.mOwnerPicture);

                    stateOtherHolder.mOwnerName.setText(mBookDetails.getBook().getOwner().getName());


                    String durationText = getStateDurationText();
                    String stateAndDuration = bookStateToString(state) + " " + durationText;
                    stateOtherHolder.mBookState.setVisibility(View.VISIBLE);
                    stateOtherHolder.mBookState.setText(stateAndDuration);

                    // Enable or disable book request button
                }else if (mBook != null){
                    StateSectionOtherUserViewHolder stateOtherHolder = (StateSectionOtherUserViewHolder) holder;

                    Book.State state = mBook.getState();

                    if (mOtherUserClickListeners != null) {

                        stateOtherHolder.mOwnerClickArea.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOtherUserClickListeners.onOwnerClick(mBook.getOwner());
                            }
                        });

                        // If book is on road state and book has sent to me. The button changes to arrive button
                        if (state == Book.State.ON_ROAD) {
                            ArrayList<Book.BookProcess> bookProcesses = mBookDetails.getBookProcesses();
                            Book.BookProcess lastProcess = bookProcesses.get(bookProcesses.size() - 1);

                            if (lastProcess instanceof Book.Transaction) {
                                Book.Transaction sentTransaction = (Book.Transaction) lastProcess;

                                if (sentTransaction.getToUser() == SessionManager.getCurrentUser(mContext)) {
                                    stateOtherHolder.mRequest.setText(R.string.arrived);
                                    stateOtherHolder.mRequest.setEnabled(true);
                                    stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                                    stateOtherHolder.mRequest.setAlpha(1f);
                                    stateOtherHolder.mRequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mOtherUserClickListeners.onArrivedButtonClick(mBookDetails);
                                        }
                                    });

                                } else {
                                    stateOtherHolder.mRequest.setText(R.string.request_button);
                                    stateOtherHolder.mRequest.setEnabled(true);
                                    stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                                    stateOtherHolder.mRequest.setAlpha(1f);
                                }
                            }

                        } else if (state == Book.State.OPENED_TO_SHARE || state == Book.State.READING) {
                            stateOtherHolder.mRequest.setText(R.string.request_button);
                            stateOtherHolder.mRequest.setEnabled(true);
                            stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                            stateOtherHolder.mRequest.setAlpha(1f);
                        } else {
                            stateOtherHolder.mRequest.setText(R.string.request_button);
                            stateOtherHolder.mRequest.setEnabled(false);
                            stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                            stateOtherHolder.mRequest.setAlpha(0.5f);
                        }
                    }

                    Glide.with(mContext)
                            .load(mBook.getOwner().getThumbnailUrl())
                            .asBitmap()
                            .placeholder(R.drawable.placeholder_56dp)
                            .centerCrop()
                            .error(R.drawable.error_56dp)
                            .into(stateOtherHolder.mOwnerPicture);

                    stateOtherHolder.mOwnerName.setText(mBook.getOwner().getName());

                    stateOtherHolder.mBookState.setVisibility(View.GONE);

                    // Enable or disable book request button
                }
                break;
            }

            case TYPE_SUBTITLE: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(mContext.getString(R.string.pass_through));

                break;
            }

            case TYPE_BOOK_PROCESS: {

                final BookProcessViewHolder bookProcessHolder = (BookProcessViewHolder) holder;

                // TopLine BottomLine setup
                if (mBookDetails.getBookProcesses().size() == 1) {
                    bookProcessHolder.mTopLine.setVisibility(View.INVISIBLE);
                    bookProcessHolder.mBottomLine.setVisibility(View.INVISIBLE);
                } else {
                    if (position == 3) {
                        bookProcessHolder.mTopLine.setVisibility(View.INVISIBLE);
                        bookProcessHolder.mBottomLine.setVisibility(View.VISIBLE);
                    } else if (position == getItemCount() - 2) {
                        bookProcessHolder.mTopLine.setVisibility(View.VISIBLE);
                        bookProcessHolder.mBottomLine.setVisibility(View.INVISIBLE);
                    } else {
                        bookProcessHolder.mTopLine.setVisibility(View.VISIBLE);
                        bookProcessHolder.mBottomLine.setVisibility(View.VISIBLE);
                    }
                }

                ArrayList<Book.BookProcess> bookProcesses = mBookDetails.getBookProcesses();
                final Book.BookProcess item = bookProcesses.get(bookProcesses.size() - position + 2); // - Header - State - Subtitle

                /**
                 * Decide which Book process. Visitor pattern takes care this.
                 */
                item.accept(new Book.TimelineDisplayableVisitor() { // Visitor interface

                    @Override
                    public void visit(final Book.Interaction interaction) { // If BookProcess is a Book.Interaction object

                        bookProcessHolder.mCreatedAt.setText(DurationTextUtils.getShortDurationString(mContext, interaction.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.VISIBLE);

                        if (interaction.getUser().getID() != SessionManager.getCurrentUser(mContext).getID()) {

                            String userName = interaction.getUser().getName();

                            ClickableSpan clickableSpanUserName = new ClickableSpan() {
                                @Override
                                public void onClick(View textView) {
                                    mSpanTextClickListener.onUserNameClick(interaction.getUser());
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                }
                            };

                            switch (interaction.getInteractionType()) {

                                case ADD: {
                                    String addBookString = mContext.getString(R.string.x_added, userName);
                                    SpannableString spanAddBook = new SpannableString(addBookString);
                                    int startIndex = addBookString.indexOf(userName);
                                    int endIndex = startIndex + userName.length();

                                    spanAddBook.setSpan(clickableSpanUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAddBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAddBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                        0, spanAddBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAddBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_add_book_outline_36dp);

                                    break;
                                }

                                case READ_START: {
                                    String readStartString = mContext.getString(R.string.x_started_to_read_this_book, userName);
                                    SpannableString spanReadStart = new SpannableString(readStartString);
                                    int startIndex = readStartString.indexOf(userName);
                                    int endIndex = startIndex + userName.length();

                                    spanReadStart.setSpan(clickableSpanUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanReadStart.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanReadStart.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                          0, spanReadStart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanReadStart);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);

                                    break;
                                }
                                
                                case READ_STOP: {
                                    String readStopString = mContext.getString(R.string.x_finished_to_read, userName);
                                    SpannableString spanReadStop = new SpannableString(readStopString);
                                    int startIndex = readStopString.indexOf(userName);
                                    int endIndex = startIndex + userName.length();

                                    spanReadStop.setSpan(clickableSpanUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanReadStop.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanReadStop.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanReadStop.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanReadStop);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);

                                    break;
                                }
                                
                                case CLOSE_TO_SHARE: {
                                    String closeToShareString = mContext.getString(R.string.x_closed_sharing, userName);
                                    SpannableString spanCloseToShare = new SpannableString(closeToShareString);
                                    int startIndex = closeToShareString.indexOf(userName);
                                    int endIndex = startIndex + userName.length();

                                    spanCloseToShare.setSpan(clickableSpanUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanCloseToShare.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanCloseToShare.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                             0, spanCloseToShare.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanCloseToShare);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);

                                    break;
                                }

                                case OPEN_TO_SHARE: {
                                    String openToShareString = mContext.getString(R.string.x_opened_sharing_for_this_book, userName);
                                    SpannableString spanOpenToShare = new SpannableString(openToShareString);
                                    int startIndex = openToShareString.indexOf(userName);
                                    int endIndex = startIndex + userName.length();

                                    spanOpenToShare.setSpan(clickableSpanUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanOpenToShare.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanOpenToShare.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                            0, spanOpenToShare.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanOpenToShare);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid interaction type:" + interaction.getInteractionType().name());
                            }

                            bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                            bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);

                        } else {

                            switch (interaction.getInteractionType()) {

                                case ADD:
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_add_book_outline_36dp);
                                    bookProcessHolder.mProcessChange.setText(R.string.you_added);
                                    break;
                                case READ_START:
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                                    bookProcessHolder.mProcessChange.setText(R.string.you_started_to_read);
                                    break;

                                case READ_STOP:
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                                    bookProcessHolder.mProcessChange.setText(R.string.you_finished_to_read);
                                    break;

                                case CLOSE_TO_SHARE:
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                                    bookProcessHolder.mProcessChange.setText(R.string.you_closed_sharing);
                                    break;

                                case OPEN_TO_SHARE:
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                                    bookProcessHolder.mProcessChange.setText(R.string.you_opened_sharing);
                                    break;

                                default:
                                    throw new IllegalArgumentException("Invalid interaction type:" + interaction.getInteractionType().name());
                            }
                        }
                    }

                    @Override
                    public void visit(final Book.Transaction transaction) { // If BookProcess is a Book.Transaction object

                        bookProcessHolder.mCreatedAt.setText(DurationTextUtils.getShortDurationString(mContext, transaction.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.VISIBLE);

                        String fromUserName = transaction.getFromUser().getName();
                        String toUserName = transaction.getToUser().getName();

                        ClickableSpan clickableSpanFromUserName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(transaction.getFromUser());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ClickableSpan clickableSpanToUserName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(transaction.getToUser());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        if (transaction.getFromUser().getID() == SessionManager.getCurrentUser(mContext).getID()) {

                            switch (transaction.getTransactionType()) {

                                case COME_TO_HAND: {
                                    String comeToHandString = mContext.getString(R.string.x_took, toUserName);
                                    SpannableString spanComeToHand = new SpannableString(comeToHandString);
                                    int startIndex = comeToHandString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanComeToHand.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                           0, spanComeToHand.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanComeToHand);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.you_sent_the_book_to_x, toUserName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);
                                    int startIndex = sentBookString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanSentBook.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.you_lost_the_book_while_sending_to_x, toUserName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);
                                    int startIndex = lostBookString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanLostBook.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getTransactionType().name());
                            }

                        } else if (transaction.getToUser().getID() == SessionManager.getCurrentUser(mContext).getID()) {

                            switch (transaction.getTransactionType()) {

                                case COME_TO_HAND: {
                                    bookProcessHolder.mProcessChange.setText(R.string.you_took);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.x_sent_the_book_to_you, fromUserName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);
                                    int startIndex = sentBookString.indexOf(fromUserName);
                                    int endIndex = startIndex + fromUserName.length();

                                    spanSentBook.setSpan(clickableSpanFromUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.book_lost_while_x_sending_to_you, fromUserName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);
                                    int startIndex = lostBookString.indexOf(fromUserName);
                                    int endIndex = startIndex + fromUserName.length();

                                    spanLostBook.setSpan(clickableSpanFromUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getTransactionType().name());
                            }

                        } else {

                            switch (transaction.getTransactionType()) {

                                case COME_TO_HAND: {
                                    String comeToHandString = mContext.getString(R.string.x_took, toUserName);
                                    SpannableString spanComeToHand = new SpannableString(comeToHandString);
                                    int startIndex = comeToHandString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanComeToHand.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                           0, spanComeToHand.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanComeToHand);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.x_sent_the_book_to_y, fromUserName, toUserName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);

                                    int startIndexToUser = sentBookString.indexOf(toUserName);
                                    int endIndexToUser = startIndexToUser + toUserName.length();

                                    int startIndexFromUser = sentBookString.indexOf(fromUserName);
                                    int endIndexFromUser = startIndexFromUser + fromUserName.length();

                                    spanSentBook.setSpan(clickableSpanFromUserName, startIndexFromUser, endIndexFromUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexFromUser, endIndexFromUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentBook.setSpan(clickableSpanToUserName, startIndexToUser, endIndexToUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexToUser, endIndexToUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost, fromUserName, toUserName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);

                                    int startIndexFromUser = lostBookString.indexOf(fromUserName);
                                    int endIndexFromUser = startIndexFromUser + fromUserName.length();

                                    int startIndexToUser = lostBookString.indexOf(toUserName);
                                    int endIndexToUser = startIndexToUser + toUserName.length();

                                    spanLostBook.setSpan(clickableSpanFromUserName, startIndexFromUser, endIndexFromUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexFromUser, endIndexFromUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanLostBook.setSpan(clickableSpanToUserName, startIndexToUser, endIndexToUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexToUser, endIndexToUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getTransactionType().name());
                            }
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void visit(final Book.Request request) { // If BookProcess is a Book.Request object

                        bookProcessHolder.mCreatedAt.setText(DurationTextUtils.getShortDurationString(mContext, request.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.GONE);

                        String fromUserName = request.getFromUser().getName();
                        String toUserName = request.getToUser().getName();

                        ClickableSpan clickableSpanFromUserName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(request.getFromUser());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ClickableSpan clickableSpanToUserName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(request.getToUser());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        if (request.getFromUser().getID() == SessionManager.getCurrentUser(mContext).getID()) {

                            switch (request.getRequestType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.you_sent_request, toUserName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);
                                    int startIndex = sendRequestString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanSentRequest.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.you_accepted_request, toUserName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);
                                    int startIndex = acceptRequestString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanAcceptRequest.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.you_rejected_request, toUserName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);
                                    int startIndex = rejectRequestString.indexOf(toUserName);
                                    int endIndex = startIndex + toUserName.length();

                                    spanRejectRequest.setSpan(clickableSpanToUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getRequestType().name());
                            }

                        } else if (request.getToUser().getID() == SessionManager.getCurrentUser(mContext).getID()) {

                            switch (request.getRequestType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.x_sent_request_to_you, fromUserName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);
                                    int startIndex = sendRequestString.indexOf(fromUserName);
                                    int endIndex = startIndex + fromUserName.length();

                                    spanSentRequest.setSpan(clickableSpanFromUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                            0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.x_accepted_your_request, fromUserName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);
                                    int startIndex = acceptRequestString.indexOf(fromUserName);
                                    int endIndex = startIndex + fromUserName.length();

                                    spanAcceptRequest.setSpan(clickableSpanFromUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.x_rejected_your_request, fromUserName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);
                                    int startIndex = rejectRequestString.indexOf(fromUserName);
                                    int endIndex = startIndex + fromUserName.length();

                                    spanRejectRequest.setSpan(clickableSpanFromUserName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getRequestType().name());
                            }

                        } else {

                            switch (request.getRequestType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.x_sent_request_to_y, fromUserName, toUserName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);

                                    int startIndexFromUser = sendRequestString.indexOf(fromUserName);
                                    int endIndexFromUser = startIndexFromUser + fromUserName.length();

                                    int startIndexToUser = sendRequestString.indexOf(toUserName);
                                    int endIndexToUser = startIndexToUser + toUserName.length();

                                    spanSentRequest.setSpan(clickableSpanFromUserName, startIndexFromUser, endIndexFromUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexFromUser, endIndexFromUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentRequest.setSpan(clickableSpanToUserName, startIndexToUser, endIndexToUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexToUser, endIndexToUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                            0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.x_accepted_ys_request, fromUserName, toUserName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);

                                    int startIndexFromUser = acceptRequestString.indexOf(fromUserName);
                                    int endIndexFromUser = startIndexFromUser + fromUserName.length();

                                    int startIndexToUser = acceptRequestString.indexOf(toUserName);
                                    int endIndexToUser = startIndexToUser + toUserName.length();

                                    spanAcceptRequest.setSpan(clickableSpanFromUserName, startIndexFromUser, endIndexFromUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexFromUser, endIndexFromUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanAcceptRequest.setSpan(clickableSpanToUserName, startIndexToUser, endIndexToUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexToUser, endIndexToUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.x_rejected_ys_request, fromUserName, toUserName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);

                                    int startIndexFromUser = rejectRequestString.indexOf(fromUserName);
                                    int endIndexFromUser = startIndexFromUser + fromUserName.length();

                                    int startIndexToUser = rejectRequestString.indexOf(toUserName);
                                    int endIndexToUser = startIndexToUser + toUserName.length();

                                    spanRejectRequest.setSpan(clickableSpanFromUserName, startIndexFromUser, endIndexFromUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexFromUser, endIndexFromUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanRejectRequest.setSpan(clickableSpanToUserName, startIndexToUser, endIndexToUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexToUser, endIndexToUser, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getRequestType().name());
                            }
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
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

    public int getRequestCount() {
        final int[] requestCountFinal = {0};
        ArrayList<Book.BookProcess> bookProcesses = mBookDetails.getBookProcesses();
        int i = 0;
        while
            (i < bookProcesses.size() &&
            ((bookProcesses.get(i) instanceof Book.Request) && ((Book.Request) bookProcesses.get(i)).getRequestType() == Book.RequestType.ACCEPT)) {
            Book.BookProcess process = bookProcesses.get(i);
            process.accept(new Book.TimelineDisplayableVisitor() {
                @Override
                public void visit(Book.Interaction interaction) {}

                @Override
                public void visit(Book.Transaction transaction) {}

                @Override
                public void visit(Book.Request request) {
                    if (request.getRequestType() == Book.RequestType.SEND) {
                        requestCountFinal[0]++;
                    }
                }
            });
            i++;
        }
        return requestCountFinal[0];
    }

    private String getStateDurationText() {
        Book.BookProcess lastProcess;

        int i = 0;
        do {
            lastProcess = mBookDetails.getBookProcesses().get(mBookDetails.getBookProcesses().size() - 1 - i++);
        } while (lastProcess instanceof Book.Request || lastProcess == null);

        Calendar createdAt = null;
        if (lastProcess instanceof Book.Interaction) {
            createdAt = ((Book.Interaction) lastProcess).getCreatedAt();
        } else if (lastProcess instanceof Book.Transaction) {
            createdAt = ((Book.Transaction) lastProcess).getCreatedAt();
        }

        int dayDiff = DurationTextUtils.calculateDayDiff(createdAt);

        if (dayDiff > 0) {
            return mContext.getString(R.string.state_duration, dayDiff);
        } else {
            return mContext.getString(R.string.state_today);
        }
    }

    private String bookStateToString(Book.State state) {

        switch (state) {

            case READING:
                return mContext.getString(R.string.reading);

            case OPENED_TO_SHARE:
                return mContext.getString(R.string.opened_to_share);

            case CLOSED_TO_SHARE:
                return mContext.getString(R.string.closed_to_share);

            case ON_ROAD:
                return mContext.getString(R.string.on_road);

            case LOST:
                return mContext.getString(R.string.lost);

            default:
                return null;
        }
    }

    public interface HeaderClickListeners {
        void onBookPictureClick(Book book);
    }

    public interface StateOtherUserClickListeners {
        void onRequestButtonClick(Book.Details details);

        void onArrivedButtonClick(Book.Details details);

        void onOwnerClick(User owner);
    }

    public interface StateCurrentUserClickListeners {
        void onStateClick(Book.Details bookDetails);

        void onRequestButtonClick(Book.Details bookDetails);
    }

    public interface SpanTextClickListeners {
        void onUserNameClick(User user);
    }

    public void setBookDetails(Book.Details bookDetails) {
        mBookDetails = bookDetails;
        mProgressBarActive = false;
        notifyDataSetChanged();
    }

    public void setError(int errorType){
        mErrorType = errorType;
        if (errorType != ERROR_TYPE_NONE){
            mBookDetails = null;
            setProgressBarActive(false);
        }
        notifyDataSetChanged();
    }

    public HeaderClickListeners getHeaderClickListeners() {
        return mHeaderClickListeners;
    }

    public void setHeaderClickListeners(HeaderClickListeners headerClickListeners) {
        mHeaderClickListeners = headerClickListeners;
        notifyItemChanged(0);
    }

    public void setCurrentUserClickListeners(StateCurrentUserClickListeners currentUserClickListeners) {
        mCurrentUserClickListeners = currentUserClickListeners;
    }

    public void setOtherUserClickListeners(StateOtherUserClickListeners otherUserClickListeners) {
        mOtherUserClickListeners = otherUserClickListeners;
    }

    public void setProgressBarActive(boolean active) {
        mProgressBarActive = active;
        notifyItemChanged(getItemCount() - 1);
    }

    public SpanTextClickListeners getSpanTextClickListener() {
        return mSpanTextClickListener;
    }

    public void setSpanTextClickListener(SpanTextClickListeners spanTextClickListener) {
        mSpanTextClickListener = spanTextClickListener;
    }
}
