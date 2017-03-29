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
import android.util.Log;
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
import com.karambit.bookie.helper.CreatedAtHelper;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.SmartisanProgressBarDrawable;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Interaction;
import com.karambit.bookie.model.Request;
import com.karambit.bookie.model.Transaction;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

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

        User currentUser = SessionManager.getCurrentUser(mContext);

        if (mBookDetails != null) {

            if (position == 0) {
                return TYPE_HEADER;

            } else if (position == 1) {

                if (mBookDetails.getBook().getOwner().equals(currentUser)) {
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

                if (currentUser.equals(mBook.getOwner())) {
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

        final User currentUser = SessionManager.getCurrentUser(mContext);

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
                    stateCurrentHolder.mStateDuration.setText(CreatedAtHelper.calculateLongDurationText(mContext, mBookDetails.getBookProcesses()));

                    /////////////////////////////////////////////////////////////////////////////////////////////

                    int requestCount = getPendingRequestCount();

                    if (requestCount > 0) {

                        stateCurrentHolder.mRequestCount.setVisibility(View.VISIBLE);

                        if (requestCount <= 9) {
                            stateCurrentHolder.mRequestCount.setText(String.valueOf(requestCount));
                        } else {
                            stateCurrentHolder.mRequestCount.setText("9+");
                        }

                    } else {
                        stateCurrentHolder.mRequestCount.setVisibility(View.GONE);
                    }

                    if (mCurrentUserClickListeners != null) {
                        stateCurrentHolder.mRequestClickArea.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentUserClickListeners.onRequestButtonClick(mBookDetails);
                            }
                        });
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
                            Book.BookProcess lastProcess = bookProcesses.get(bookProcesses.size() -1);
                            if (lastProcess instanceof Transaction) {
                                Transaction sentTransaction = (Transaction) lastProcess;

                                if (sentTransaction.getTaker().equals(currentUser)) {
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
                                    stateOtherHolder.mRequest.setEnabled(false);
                                    stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                                    stateOtherHolder.mRequest.setAlpha(0.5f);
                                }
                            } else {
                                Log.e(TAG, "State-BookProcess inconsistency. state=ON_ROAD lastBookProcess!=Transaction");
                            }

                        } else if (state == Book.State.OPENED_TO_SHARE || state == Book.State.READING) {

                            /*
                                    Request tuşu "requested" yada "request" olarak değiştiren kısım
                                    TODO May be inconsistent CHECK
                                    Kitaba birden fazla istek gelir current user da istek atar.
                                    Current user dan başka bir kullanıcının isteği kabul edilir.
                                    Benim REJECTED yazılmayacağı için kitap tekrar önceki ownera dönerse tutarsızlık oluşabilir
                                */

                            int canSendRequest = 0;
                            for (Book.BookProcess process: mBookDetails.getBookProcesses()) {

                                if (process instanceof Request) {

                                    Request request = (Request) process;

                                    if (request.getRequester().equals(currentUser) && request.getResponder().equals(mBook.getOwner())) {

                                        if (request.getType() == Request.Type.SEND) {
                                            canSendRequest--;
                                        } else { // Request.Type.ACCEPT || Request.Type.REJECT
                                            canSendRequest++;
                                        }
                                    }
                                }
                            }
                            if (canSendRequest < 0){
                                stateOtherHolder.mRequest.setText(R.string.requested_button);
                                stateOtherHolder.mRequest.setEnabled(false);
                                stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                                stateOtherHolder.mRequest.setAlpha(0.5f);
                            }else {
                                stateOtherHolder.mRequest.setText(R.string.request_button);
                                stateOtherHolder.mRequest.setEnabled(true);
                                stateOtherHolder.mRequest.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                                stateOtherHolder.mRequest.setAlpha(1f);
                            }

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


                    String durationText = CreatedAtHelper.calculateLongDurationText(mContext, mBookDetails.getBookProcesses());
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

                        if (state == Book.State.CLOSED_TO_SHARE || state == Book.State.LOST || state == Book.State.ON_ROAD) {

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

                /*
                  Decide which Book process. Visitor pattern takes care of this.
                 */
                item.accept(new Book.TimelineDisplayableVisitor() { // Visitor interface

                    @Override
                    public void visit(final Interaction interaction) { // If BookProcess is a Book.Interaction object

                        bookProcessHolder.mCreatedAt.setText(CreatedAtHelper.getShortDurationString(mContext, interaction.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.VISIBLE);

                        if (interaction.getUser().equals(currentUser)) {

                            switch (interaction.getType()) {

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
                                    throw new IllegalArgumentException("Invalid interaction type:" + interaction.getType().name());
                            }
                        } else {

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

                            switch (interaction.getType()) {

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
                                    throw new IllegalArgumentException("Invalid interaction type:" + interaction.getType().name());
                            }

                            bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                            bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);

                        }
                    }

                    @Override
                    public void visit(final Transaction transaction) { // If BookProcess is a Book.Transaction object

                        bookProcessHolder.mCreatedAt.setText(CreatedAtHelper.getShortDurationString(mContext, transaction.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.VISIBLE);

                        String giverName = transaction.getGiver().getName();
                        String takerName = transaction.getTaker().getName();

                        ClickableSpan clickableSpanGiverName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(transaction.getGiver());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ClickableSpan clickableSpanTakerName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(transaction.getTaker());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        if (transaction.getGiver().equals(currentUser)) {

                            switch (transaction.getType()) {

                                case COME_TO_HAND: {
                                    String comeToHandString = mContext.getString(R.string.x_took, takerName);
                                    SpannableString spanComeToHand = new SpannableString(comeToHandString);
                                    int startIndex = comeToHandString.indexOf(takerName);
                                    int endIndex = startIndex + takerName.length();

                                    spanComeToHand.setSpan(clickableSpanTakerName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                           0, spanComeToHand.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanComeToHand);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.you_sent_the_book_to_x, takerName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);
                                    int startIndex = sentBookString.indexOf(takerName);
                                    int endIndex = startIndex + takerName.length();

                                    spanSentBook.setSpan(clickableSpanTakerName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.you_lost_the_book_while_sending_to_x, takerName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);
                                    int startIndex = lostBookString.indexOf(takerName);
                                    int endIndex = startIndex + takerName.length();

                                    spanLostBook.setSpan(clickableSpanTakerName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getType().name());
                            }

                        } else if (transaction.getTaker().equals(currentUser)) {

                            switch (transaction.getType()) {

                                case COME_TO_HAND: {
                                    bookProcessHolder.mProcessChange.setText(R.string.you_took);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.x_sent_the_book_to_you, giverName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);
                                    int startIndex = sentBookString.indexOf(giverName);
                                    int endIndex = startIndex + giverName.length();

                                    spanSentBook.setSpan(clickableSpanGiverName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.book_lost_while_x_sending_to_you, giverName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);
                                    int startIndex = lostBookString.indexOf(giverName);
                                    int endIndex = startIndex + giverName.length();

                                    spanLostBook.setSpan(clickableSpanGiverName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getType().name());
                            }

                        } else {

                            switch (transaction.getType()) {

                                case COME_TO_HAND: {
                                    String comeToHandString = mContext.getString(R.string.x_took, takerName);
                                    SpannableString spanComeToHand = new SpannableString(comeToHandString);
                                    int startIndex = comeToHandString.indexOf(takerName);
                                    int endIndex = startIndex + takerName.length();

                                    spanComeToHand.setSpan(clickableSpanTakerName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanComeToHand.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                           0, spanComeToHand.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanComeToHand);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);

                                    break;
                                }

                                case DISPACTH: {
                                    String sentBookString = mContext.getString(R.string.x_sent_the_book_to_y, giverName, takerName);
                                    SpannableString spanSentBook = new SpannableString(sentBookString);

                                    int startIndexTaker = sentBookString.indexOf(takerName);
                                    int endIndexTaker = startIndexTaker + takerName.length();

                                    int startIndexGiver = sentBookString.indexOf(giverName);
                                    int endIndexGiver = startIndexGiver + giverName.length();

                                    spanSentBook.setSpan(clickableSpanGiverName, startIndexGiver, endIndexGiver, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexGiver, endIndexGiver, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentBook.setSpan(clickableSpanTakerName, startIndexTaker, endIndexTaker, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexTaker, endIndexTaker, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);

                                    break;
                                }

                                case LOST: {
                                    String lostBookString = mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost, giverName, takerName);
                                    SpannableString spanLostBook = new SpannableString(lostBookString);

                                    int startIndexGiver = lostBookString.indexOf(giverName);
                                    int endIndexGiver = startIndexGiver + giverName.length();

                                    int startIndexTaker = lostBookString.indexOf(takerName);
                                    int endIndexTaker = startIndexTaker + takerName.length();

                                    spanLostBook.setSpan(clickableSpanGiverName, startIndexGiver, endIndexGiver, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexGiver, endIndexGiver, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanLostBook.setSpan(clickableSpanTakerName, startIndexTaker, endIndexTaker, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanLostBook.setSpan(new StyleSpan(Typeface.BOLD), startIndexTaker, endIndexTaker, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanLostBook.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanLostBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanLostBook);
                                    bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid transaction type:" + transaction.getType().name());
                            }
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void visit(final Request request) { // If BookProcess is a Book.Request object

                        bookProcessHolder.mCreatedAt.setText(CreatedAtHelper.getShortDurationString(mContext, request.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.GONE);

                        String requesterName = request.getRequester().getName();
                        String responderName = request.getResponder().getName();

                        ClickableSpan clickableSpanRequesterName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(request.getRequester());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        ClickableSpan clickableSpanResponderName = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                mSpanTextClickListener.onUserNameClick(request.getResponder());
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };

                        if (request.getRequester().equals(currentUser)) {

                            switch (request.getType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.you_sent_request, responderName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);
                                    int startIndex = sendRequestString.indexOf(responderName);
                                    int endIndex = startIndex + responderName.length();

                                    spanSentRequest.setSpan(clickableSpanResponderName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                         0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.you_accepted_request, requesterName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);
                                    int startIndex = acceptRequestString.indexOf(requesterName);
                                    int endIndex = startIndex + requesterName.length();

                                    spanAcceptRequest.setSpan(clickableSpanRequesterName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.you_rejected_request, requesterName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);
                                    int startIndex = rejectRequestString.indexOf(requesterName);
                                    int endIndex = startIndex + requesterName.length();

                                    spanRejectRequest.setSpan(clickableSpanRequesterName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getType().name());
                            }

                        } else if (request.getResponder().equals(currentUser)) {

                            switch (request.getType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.x_sent_request_to_you, requesterName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);
                                    int startIndex = sendRequestString.indexOf(requesterName);
                                    int endIndex = startIndex + requesterName.length();

                                    spanSentRequest.setSpan(clickableSpanRequesterName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                            0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.x_accepted_your_request, responderName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);
                                    int startIndex = acceptRequestString.indexOf(responderName);
                                    int endIndex = startIndex + responderName.length();

                                    spanAcceptRequest.setSpan(clickableSpanResponderName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.x_rejected_your_request, responderName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);
                                    int startIndex = rejectRequestString.indexOf(responderName);
                                    int endIndex = startIndex + responderName.length();

                                    spanRejectRequest.setSpan(clickableSpanResponderName, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getType().name());
                            }

                        } else {

                            switch (request.getType()) {

                                case SEND: {
                                    String sendRequestString = mContext.getString(R.string.x_sent_request_to_y, requesterName, responderName);
                                    SpannableString spanSentRequest = new SpannableString(sendRequestString);

                                    int startIndexRequester = sendRequestString.indexOf(requesterName);
                                    int endIndexRequester = startIndexRequester + requesterName.length();

                                    int startIndexResponder = sendRequestString.indexOf(responderName);
                                    int endIndexResponder = startIndexResponder + responderName.length();

                                    spanSentRequest.setSpan(clickableSpanRequesterName, startIndexRequester, endIndexRequester, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexRequester, endIndexRequester, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentRequest.setSpan(clickableSpanResponderName, startIndexResponder, endIndexResponder, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanSentRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexResponder, endIndexResponder, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanSentRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                            0, spanSentRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanSentRequest);

                                    break;
                                }

                                case ACCEPT: {
                                    String acceptRequestString = mContext.getString(R.string.x_accepted_ys_request, responderName, requesterName);
                                    SpannableString spanAcceptRequest = new SpannableString(acceptRequestString);

                                    int startIndexResponder = acceptRequestString.indexOf(responderName);
                                    int endIndexResponder = startIndexResponder + responderName.length();

                                    int startIndexRequester = acceptRequestString.indexOf(requesterName);
                                    int endIndexRequester = startIndexRequester + requesterName.length();

                                    spanAcceptRequest.setSpan(clickableSpanResponderName, startIndexResponder, endIndexResponder, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexResponder, endIndexResponder, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanAcceptRequest.setSpan(clickableSpanRequesterName, startIndexRequester, endIndexRequester, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanAcceptRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexRequester, endIndexRequester, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanAcceptRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanAcceptRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanAcceptRequest);

                                    break;
                                }

                                case REJECT: {

                                    String rejectRequestString = mContext.getString(R.string.x_rejected_ys_request, responderName, requesterName);
                                    SpannableString spanRejectRequest = new SpannableString(rejectRequestString);

                                    int startIndexResponder = rejectRequestString.indexOf(responderName);
                                    int endIndexResponder = startIndexResponder + responderName.length();

                                    int startIndexRequester = rejectRequestString.indexOf(requesterName);
                                    int endIndexRequester = startIndexRequester + requesterName.length();

                                    spanRejectRequest.setSpan(clickableSpanResponderName, startIndexResponder, endIndexResponder, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexResponder, endIndexResponder, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanRejectRequest.setSpan(clickableSpanRequesterName, startIndexRequester, endIndexRequester, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spanRejectRequest.setSpan(new StyleSpan(Typeface.BOLD), startIndexRequester, endIndexRequester, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    spanRejectRequest.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)),
                                                              0, spanRejectRequest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    bookProcessHolder.mProcessChange.setText(spanRejectRequest);

                                    break;
                                }

                                default:
                                    throw new IllegalArgumentException("Invalid request type:" + request.getType().name());
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

    public int getPendingRequestCount() {

        Book.State state = mBookDetails.getBook().getState();

        if (state == Book.State.OPENED_TO_SHARE || state == Book.State.READING) {

            User currentUser = SessionManager.getCurrentUser(mContext);
            ArrayList<Book.BookProcess> bookProcesses = mBookDetails.getBookProcesses();

            int pendingRequestCount = 0;

            // The list is reversed so the closest process to now is the last element
            int index = bookProcesses.size() - 1;
            boolean acceptedRequestFound = false;
            while (!acceptedRequestFound && index >= 0) {

                Book.BookProcess process = bookProcesses.get(index);

                if (process instanceof Request && ((Request) process).getResponder().equals(currentUser)) {

                    Request.Type type = ((Request) process).getType();

                    if (type == Request.Type.SEND) {
                        pendingRequestCount++;
                    } else if (type == Request.Type.REJECT) {
                        pendingRequestCount--;
                    } else if (type == Request.Type.ACCEPT){
                        acceptedRequestFound = true;
                    }
                }
            }

            return pendingRequestCount;

        } else {
            return 0;
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
        Collections.sort(bookDetails.getBookProcesses(), new Comparator<Book.BookProcess>() {
            @Override
            public int compare(Book.BookProcess o1, Book.BookProcess o2) {
                Calendar c1 = o1.getCreatedAt();

                Calendar c2 = o2.getCreatedAt();

                return c1.compareTo(c2);
            }
        });

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

    public int getHeaderIndex() {
        return 0;
    }

    public int getBookStateIndex() {
        return 1; // Header
    }

    public int getBeginningOfBookProcessesIndex() {
        return 3; // Header + State + Subtitle
    }
}
