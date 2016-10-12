package com.karambit.bookie.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.model.Book;

/**
 * Created by orcan on 10/12/16.
 */

public class BookTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = BookTimelineAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BOOK_PROCESS = 1;
    private static final int TYPE_SUBTITLE = 2;
    private static final int TYPE_FOOTER = 3;

    private Context mContext;
    private Book.Details mBookDetails;

    private boolean mProgressBarActive;

    public BookTimelineAdapter(Context context, Book.Details bookDetails) {
        mContext = context;
        mBookDetails = bookDetails;

        mProgressBarActive = false;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mBookPicture;
        private TextView mBookName;
        private TextView mAuthor;
        private TextView mGenre;
        private CircleImageView mOwnerPicture;
        private TextView mOwnerName;
        private TextView mBookState;
        private Button mRequest;

        private HeaderViewHolder(View headerView) {
            super(headerView);

            mBookPicture = (CircleImageView) headerView.findViewById(R.id.bookPictureHeaderCircleImageView);
            mBookName = (TextView) headerView.findViewById(R.id.bookNameHeaderTextView);
            mAuthor = (TextView) headerView.findViewById(R.id.authorHeaderTextView);
            mGenre = (TextView) headerView.findViewById(R.id.genreHeaderTextView);
            mOwnerPicture = (CircleImageView) headerView.findViewById(R.id.ownerPictureHeaderCircleImageView);
            mOwnerName = (TextView) headerView.findViewById(R.id.ownerNameHeaderTextView);
            mBookState = (TextView) headerView.findViewById(R.id.bookStateHeaderTextView);
            mRequest = (Button) headerView.findViewById(R.id.requestHeaderButton);
        }
    }

    private static class BookProcessViewHolder extends RecyclerView.ViewHolder {
        private ImageView mProcessImage;
        private TextView mProcessChange;
        private View mTopLine;
        private View mBottomLine;

        private BookProcessViewHolder(View itemView) {
            super(itemView);
            mProcessImage = (ImageView) itemView.findViewById(R.id.bookProcessImageView);
            mProcessChange = (TextView) itemView.findViewById(R.id.bookProcessChangeTextView);
            mTopLine = itemView.findViewById(R.id.topLineView);
            mBottomLine = itemView.findViewById(R.id.bottomLineView);
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
        return mBookDetails.getBookProcesses().size() + 3; // + Header + Subtitle + Footer
    }

    /*
        HEADER
        SUBTITLE
        BOOK PROCESSES
        FOOTER
     */
    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return TYPE_HEADER;

        } else if (position == 1) {
            return TYPE_SUBTITLE;

        } else if (position < mBookDetails.getBookProcesses().size() + 2) { // + Header + Subtitle
            return TYPE_BOOK_PROCESS;

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
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_book_timeline, parent, false);
                return new HeaderViewHolder(headerView);

            case TYPE_BOOK_PROCESS:
                View bookProcessView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_process_book_timeline, parent, false);
                return new BookProcessViewHolder(bookProcessView);

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

                // TODO Listener setup

                Glide.with(mContext)
                        .load(mBookDetails.getBook().getThumbnailURL())
                        .asBitmap()
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .into(headerViewHolder.mBookPicture);

                headerViewHolder.mBookName.setText(mBookDetails.getBook().getName());
                headerViewHolder.mAuthor.setText(mBookDetails.getBook().getAuthor());

                //TODO Genre
                headerViewHolder.mGenre.setText("Genre");

                Glide.with(mContext)
                        .load(mBookDetails.getBook().getOwner().getThumbnailUrl())
                        .asBitmap()
                        .placeholder(R.drawable.placeholder_book)
                        .centerCrop()
                        .into(headerViewHolder.mOwnerPicture);

                headerViewHolder.mOwnerName.setText(mBookDetails.getBook().getOwner().getName());

                Book.State state = mBookDetails.getBook().getState();

                headerViewHolder.mBookState.setText(bookStateToString(state));

                // Enable or disable book request button
                if (state == Book.State.OPENED_TO_SHARE) {
                    headerViewHolder.mRequest.setEnabled(true);
                } else {
                    headerViewHolder.mRequest.setEnabled(false);
                }

                break;
            }

            case TYPE_SUBTITLE: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(mContext.getString(R.string.pass_through));

                break;
            }

            case TYPE_BOOK_PROCESS: {

                final BookProcessViewHolder itemHolder = (BookProcessViewHolder) holder;


                // TopLine BottomLine setup
                if (mBookDetails.getBookProcesses().size() == 1) {
                    itemHolder.mTopLine.setVisibility(View.INVISIBLE);
                    itemHolder.mBottomLine.setVisibility(View.INVISIBLE);
                } else {
                    if (position == 2) {
                        itemHolder.mTopLine.setVisibility(View.INVISIBLE);
                        itemHolder.mBottomLine.setVisibility(View.VISIBLE);
                    } else if (position == getItemCount() - 2) {
                        itemHolder.mTopLine.setVisibility(View.VISIBLE);
                        itemHolder.mBottomLine.setVisibility(View.INVISIBLE);
                    } else {
                        itemHolder.mTopLine.setVisibility(View.VISIBLE);
                        itemHolder.mBottomLine.setVisibility(View.VISIBLE);
                    }
                }

                final Book.BookProcess item = mBookDetails.getBookProcesses().get(position - 2); // - Header - Subtitle

                /**
                 * Decide which Book process. Visitor pattern takes care this.
                 */
                item.accept(new Book.TimelineDisplayableVisitor() { // Visitor interface

                    @Override
                    public void visit(Book.Interaction interaction) { // If BookProcess is a Book.Interaction object

                        itemHolder.mProcessImage.setVisibility(View.VISIBLE);

                        switch (interaction.getInteractionType()) {

                            case ADD:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.reading_24dp);
                                itemHolder.mProcessChange.setText( mContext.getString(R.string.x_added_this_book, interaction.getUser().getName()));
                                break;

                            case READ_START:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.reading_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_started_to_read_this_book, interaction.getUser().getName()));
                                break;

                            case READ_STOP:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.reading_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_finished_to_read_this_book, interaction.getUser().getName()));
                                break;

                            case CLOSE_TO_SHARE:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.close_to_share_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_closed_sharing_for_this_book, interaction.getUser().getName()));
                                break;

                            case OPEN_TO_SHARE:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.open_to_share_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_opened_sharing_for_this_book, interaction.getUser().getName()));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid interaction type:" + interaction.getInteractionType().name());
                        }
                    }

                    @Override
                    public void visit(Book.Transaction transaction) { // If BookProcess is a Book.Transaction object

                        itemHolder.mProcessImage.setVisibility(View.VISIBLE);

                        switch (transaction.getTransactionType()) {

                            case COME_TO_HAND:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.on_road_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_took_the_book, transaction.getToUser().getName()));
                                break;

                            case DISPACTH:
//                                itemHolder.mProcessImage.setImageResource(R.drawable.on_road_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_sent_the_book_to_y, transaction.getFromUser().getName(), transaction.getToUser().getName()));
                                break;

                            case LOST:

//                                itemHolder.mProcessImage.setImageResource(R.drawable.lost_24dp);
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.book_sent_from_x_to_y_and_its_lost, transaction.getFromUser().getName(), transaction.getToUser().getName()));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid transaction type:" + transaction.getTransactionType().name());
                        }
                    }

                    @Override
                    public void visit(Book.Request request) { // If BookProcess is a Book.Request object

                        itemHolder.mProcessImage.setVisibility(View.GONE);

                        switch (request.getRequestType()) {

                            case SEND:
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_sent_request_to_y, request.getFromUser().getName(), request.getToUser().getName()));
                                break;

                            case ACCEPT:
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_accepted_ys_request, request.getFromUser().getName(), request.getToUser().getName()));
                                break;

                            case REJECT:
                                itemHolder.mProcessChange.setText(mContext.getString(R.string.x_rejected_ys_request, request.getFromUser().getName(), request.getToUser().getName()));
                                break;

                            default:
                                throw new IllegalArgumentException("Invalid request type:" + request.getRequestType().name());
                        }
                    }
                });

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

//    public interface HeaderClickListeners {
//        void onRequestButtonClick(Book.Details details);
//        void onOwnerClick(User owner);
//        void onBookPictureClick(Book.Details details);
//    }


}
