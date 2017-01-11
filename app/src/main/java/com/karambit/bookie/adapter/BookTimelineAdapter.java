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
import android.text.TextUtils;
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
    private static final int TYPE_EMPTY_STATE = 6;

    private Context mContext;
    private Book.Details mBookDetails;

    private HeaderClickListeners mHeaderClickListeners;
    private StateCurrentUserClickListeners mCurrentUserClickListeners;
    private StateOtherUserClickListeners mOtherUserClickListeners;

    private boolean mProgressBarActive;
    private SpanTextClickListeners mSpanTextClickListener;

    public BookTimelineAdapter(Context context, Book.Details bookDetails) {
        mContext = context;
        mBookDetails = bookDetails;

        mProgressBarActive = false;
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
        private Button mRequestCountButton;
        private TextView mStateDuration;

        private StateSectionCurrentUserViewHolder(View stateSectionView) {
            super(stateSectionView);

            mStateClickArea = stateSectionView.findViewById(R.id.stateClickArea);
            mStateIcon = (ImageView) stateSectionView.findViewById(R.id.bookStateImageView);
            mStateText = (TextView) stateSectionView.findViewById(R.id.bookStateTextView);
            mRequestCountButton = (Button) stateSectionView.findViewById(R.id.requestCountButton);
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
        if (mBookDetails != null) {

            int size = mBookDetails.getBookProcesses().size();

            if (size > 0) {
                return size + 4; // + Header + State + Subtitle + Footer
            } else {
                return 3; // + Header + Empty State + Footer
            }
        } else {
            return 1; // Footer
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

            if (mBookDetails.getBookProcesses().size() > 0) {

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
            } else {

                if (position == 0) {
                    return TYPE_HEADER;

                } else if (position == 1) {
                    return TYPE_EMPTY_STATE;

                } else if (position == 2) {
                    return TYPE_FOOTER;

                } else {
                    throw new IllegalArgumentException("Invalid view type at position " + position);
                }
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

            case TYPE_EMPTY_STATE:
                View emptyStateView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_state, parent, false);
                return new FooterViewHolder(emptyStateView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_HEADER: {

                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                // Header click listeners setup
                if (mHeaderClickListeners != null) {

                    // Book picture click listener setup
                    headerViewHolder.mBookPicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListeners.onBookPictureClick(mBookDetails);
                        }
                    });
                }

                Glide.with(mContext)
                     .load(mBookDetails.getBook().getThumbnailURL())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_book)
                     .centerCrop()
                     .into(headerViewHolder.mBookPicture);

                headerViewHolder.mBookName.setText(mBookDetails.getBook().getName());
                headerViewHolder.mAuthor.setText(mBookDetails.getBook().getAuthor());

                String[] genres = mContext.getResources().getStringArray(R.array.genre_types);

                if (mBookDetails.getGenreCode() >= genres.length) { // TODO Remove this section
                    mBookDetails.setGenreCode(new Random().nextInt(genres.length));
                }

                headerViewHolder.mGenre.setText(genres[mBookDetails.getGenreCode()]);

                break;
            }

            case TYPE_STATE_SECTION_CURRENT_USER: {

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

                    stateCurrentHolder.mStateDuration.setText(getStateDurationText());

                /////////////////////////////////////////////////////////////////////////////////////////////

                int requestCount = getRequestCount();

                if (requestCount > 0) {

                    stateCurrentHolder.mRequestCountButton.setVisibility(View.VISIBLE);

                    if (requestCount <= 9) {
                        stateCurrentHolder.mRequestCountButton.setText(String.valueOf(requestCount));
                    } else {
                        stateCurrentHolder.mRequestCountButton.setText("9+");
                    }

                    if (mCurrentUserClickListeners != null) {
                        stateCurrentHolder.mRequestCountButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentUserClickListeners.onRequestCountClick(mBookDetails);
                            }
                        });
                    }

                } else {
                    stateCurrentHolder.mRequestCountButton.setVisibility(View.GONE);
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
                        stateCurrentHolder.mRequestCountButton.setVisibility(View.GONE);
                        break;

                    case ON_ROAD:
                        stateCurrentHolder.mStateText.setText(R.string.on_road);
                        stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);
                        stateCurrentHolder.mRequestCountButton.setVisibility(View.GONE);
                        break;

                    case LOST:
                        stateCurrentHolder.mStateText.setText(R.string.lost);
                        stateCurrentHolder.mStateIcon.setImageResource(R.drawable.ic_close_white_24dp);
                        stateCurrentHolder.mStateIcon.setColorFilter(Color.RED);
                        stateCurrentHolder.mRequestCountButton.setVisibility(View.GONE);
                        break;
                }

                break;
            }

            case TYPE_STATE_SECTION_OTHER_USER: {

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
                     .placeholder(R.drawable.placeholder_book)
                     .centerCrop()
                     .into(stateOtherHolder.mOwnerPicture);

                stateOtherHolder.mOwnerName.setText(mBookDetails.getBook().getOwner().getName());


                String durationText = getStateDurationText();
                String stateAndDuration = bookStateToString(state) + " " + durationText;
                stateOtherHolder.mBookState.setText(stateAndDuration);

                // Enable or disable book request button


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

                        SpannableString spanUserName = new SpannableString(interaction.getUser().getName());
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

                        spanUserName.setSpan(clickableSpanUserName, 0, spanUserName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanUserName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanUserName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        switch (interaction.getInteractionType()) {

                            case ADD:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_add_book_outline_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanUserName, " " + mContext.getString(R.string.x_added_this_book)));
                                break;
                            case READ_START:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanUserName, " " + mContext.getString(R.string.x_started_to_read_this_book)));
                                break;

                            case READ_STOP:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_read_start_stop_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanUserName, " " + mContext.getString(R.string.x_finished_to_read_this_book)));
                                break;

                            case CLOSE_TO_SHARE:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_closed_to_share_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanUserName, " " + mContext.getString(R.string.x_closed_sharing_for_this_book)));
                                break;

                            case OPEN_TO_SHARE:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_opened_to_share_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanUserName, " " + mContext.getString(R.string.x_opened_sharing_for_this_book)));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid interaction type:" + interaction.getInteractionType().name());
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void visit(final Book.Transaction transaction) { // If BookProcess is a Book.Transaction object

                        bookProcessHolder.mCreatedAt.setText(DurationTextUtils.getShortDurationString(mContext, transaction.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.VISIBLE);

                        SpannableString spanFromUserName = new SpannableString(transaction.getFromUser().getName());
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

                        spanFromUserName.setSpan(clickableSpanFromUserName, 0, spanFromUserName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanFromUserName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanFromUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanFromUserName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanFromUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        SpannableString spanToUserName = new SpannableString(transaction.getToUser().getName());
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

                        spanToUserName.setSpan(clickableSpanToUserName, 0, spanToUserName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanToUserName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanToUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanToUserName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanToUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                        switch (transaction.getTransactionType()) {

                            case COME_TO_HAND:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_come_to_hand_car_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanToUserName, " " + mContext.getString(R.string.x_took_the_book)));
                                break;

                            case DISPACTH:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_dispatch_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanFromUserName, " " + mContext.getString(R.string.x_sent_the_book_to_y) + " ", spanToUserName));
                                break;

                            case LOST:
                                bookProcessHolder.mProcessImage.setImageResource(R.drawable.ic_book_timeline_lost_outline_36dp);
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost_1) + " ", spanFromUserName, " " + mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost_2) + " ", spanToUserName, " " + mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost_3)));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid transaction type:" + transaction.getTransactionType().name());
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void visit(final Book.Request request) { // If BookProcess is a Book.Request object

                        bookProcessHolder.mCreatedAt.setText(DurationTextUtils.getShortDurationString(mContext, request.getCreatedAt()));

                        bookProcessHolder.mProcessImage.setVisibility(View.GONE);

                        SpannableString spanFromUserName = new SpannableString(request.getFromUser().getName());
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

                        spanFromUserName.setSpan(clickableSpanFromUserName, 0, spanFromUserName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanFromUserName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanFromUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanFromUserName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanFromUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        SpannableString spanToUserName = new SpannableString(request.getToUser().getName());
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

                        spanToUserName.setSpan(clickableSpanToUserName, 0, spanToUserName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanToUserName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanToUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanToUserName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanToUserName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        switch (request.getRequestType()) {

                            case SEND:
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanFromUserName, " " + mContext.getString(R.string.x_sent_request_to_y) + " ", spanToUserName));
                                break;

                            case ACCEPT:
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanFromUserName, " " + mContext.getString(R.string.x_accepted_ys_request_1) + " ", spanToUserName, "'" + mContext.getString(R.string.x_accepted_ys_request_2)));
                                break;

                            case REJECT:
                                bookProcessHolder.mProcessChange.setText(TextUtils.concat(spanFromUserName, " " + mContext.getString(R.string.x_rejected_ys_request_1) + " ", spanToUserName, "'" + mContext.getString(R.string.x_rejected_ys_request_2)));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid request type:" + request.getRequestType().name());
                        }

                        bookProcessHolder.mProcessChange.setMovementMethod(LinkMovementMethod.getInstance());
                        bookProcessHolder.mProcessChange.setHighlightColor(Color.TRANSPARENT);
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

    public int getRequestCount() {
        final int[] requestCountFinal = {0};
        for (Book.BookProcess process : mBookDetails.getBookProcesses()) {
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
        void onBookPictureClick(Book.Details details);
    }

    public interface StateOtherUserClickListeners {
        void onRequestButtonClick(Book.Details details);

        void onArrivedButtonClick(Book.Details details);

        void onOwnerClick(User owner);
    }

    public interface StateCurrentUserClickListeners {
        void onStateClick(Book.Details bookDetails);

        void onRequestCountClick(Book.Details bookDetails);
    }

    public interface SpanTextClickListeners {
        void onUserNameClick(User user);
    }

    public void setBookDetails(Book.Details bookDetails) {
        mBookDetails = bookDetails;
        mProgressBarActive = false;
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
