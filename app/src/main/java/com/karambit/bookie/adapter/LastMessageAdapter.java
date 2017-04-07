package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.CreatedAtHelper;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;

/**
 * Created by orcan on 10/27/16.
 */

public class LastMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_UNSELECTED = 0;
    public static final int VIEW_TYPE_SELECTED = 1;
    private static final int VIEW_TYPE_EMPTY_STATE = 2;

    private Context mContext;

    private ArrayList<Message> mLastMessages;
    private SparseIntArray mUnseenCounts;

    private int mSelectedPosition = -1;

    private OnMessageClickListener mOnMessageClickListener;
    private OnSelectedStateClickListener mOnSelectedStateClickListener;
    private OnSearchUserButtonClickListener mOnSearchUserButtonClickListener;

    public LastMessageAdapter(Context context) {
        mContext = context;
        mLastMessages = new ArrayList<>();
        mUnseenCounts = new SparseIntArray();
    }

    public LastMessageAdapter(Context context, ArrayList<Message> lastMessages, SparseIntArray unseenCounts) {
        mContext = context;
        mLastMessages = lastMessages;
        mUnseenCounts = unseenCounts;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mLastMessageText;
        private TextView mCreatedAt;
        private TextView mIndicator;
        private ImageView mState;

        public MessageViewHolder(View messageView) {
            super(messageView);

            mProfilePicture = (CircleImageView) messageView.findViewById(R.id.profilePicureMessage);
            mUserName = (TextView) messageView.findViewById(R.id.userNameMessage);
            mLastMessageText = (TextView) messageView.findViewById(R.id.lastMessageTextMessage);
            mCreatedAt = (TextView) messageView.findViewById(R.id.createdAtMessage);
            mIndicator = (TextView) messageView.findViewById(R.id.indicatorMessage);
            mState = (ImageView) messageView.findViewById(R.id.lastMessageState);
        }
    }

    public static class SelectedViewHolder extends RecyclerView.ViewHolder {

        View mDelete;

        public SelectedViewHolder(View selectedView) {
            super(selectedView);

            mDelete = selectedView.findViewById(R.id.deleteImageButton);
        }
    }

    private static class EmptyStateViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTextView;
        private Button mSearchUser;

        private EmptyStateViewHolder(View emptyStateView) {
            super(emptyStateView);

            mImageView = (ImageView) emptyStateView.findViewById(R.id.emptyStateImageView);
            mTextView = (TextView) emptyStateView.findViewById(R.id.emptyStateTextView);
            mSearchUser = (Button) emptyStateView.findViewById(R.id.searchUserButton);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (mLastMessages.size() > 0){
            if (position == mSelectedPosition) {
                return VIEW_TYPE_SELECTED;
            } else {
                return VIEW_TYPE_UNSELECTED;
            }
        } else {
            return VIEW_TYPE_EMPTY_STATE;
        }

    }

    @Override
    public int getItemCount() {
        return (mLastMessages.size() != 0)? mLastMessages.size(): 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_UNSELECTED:
                View messageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_last_message, parent, false);
                return new MessageViewHolder(messageView);

            case VIEW_TYPE_SELECTED:
                View selectedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_last_message_selected, parent, false);
                return new SelectedViewHolder(selectedView);

            case VIEW_TYPE_EMPTY_STATE:
                View emptyView = LayoutInflater.from(mContext).inflate(R.layout.item_empty_state_message_fragment, parent, false);
                return new EmptyStateViewHolder(emptyView);

            default:
                throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Message message = (mLastMessages.size() > 0)? mLastMessages.get(position): null;

        switch (getItemViewType(position)) {

            case VIEW_TYPE_UNSELECTED: {

                MessageViewHolder messageHolder = (MessageViewHolder) holder;

                final User currentUser = SessionManager.getCurrentUser(mContext);
                User oppositeUser = message.getOppositeUser(currentUser);

                if (! TextUtils.isEmpty(oppositeUser.getThumbnailUrl())) {
                    Glide.with(mContext)
                         .load(oppositeUser.getThumbnailUrl())
                         .asBitmap()
                         .centerCrop()
                         .error(R.drawable.error_56dp)
                         .placeholder(R.drawable.placeholder_56dp)
                         .into(messageHolder.mProfilePicture);
                } else {
                    messageHolder.mProfilePicture.setImageResource(R.drawable.placeholder_56dp);
                }

                messageHolder.mProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //If not admin or support user
                        if (message.getOppositeUser(currentUser).getID() >= 0){
                            mOnMessageClickListener.onUserClick(message.getOppositeUser(currentUser));
                        }
                    }
                });

                messageHolder.mUserName.setText(oppositeUser.getName());

                if (message.getSender().equals(currentUser)) {
                    messageHolder.mState.setVisibility(View.VISIBLE);

                    switch (message.getState()) {
                        case PENDING:
                            messageHolder.mState.setImageResource(R.drawable.ic_messaging_pending_18dp);
                            messageHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                            break;

                        case SENT:
                            messageHolder.mState.setImageResource(R.drawable.ic_messaging_sent_18dp);
                            messageHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                            break;

                        case DELIVERED:
                            messageHolder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                            messageHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                            break;

                        case SEEN:
                            messageHolder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                            messageHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                            break;

                        case ERROR:
                            messageHolder.mState.setImageResource(R.drawable.ic_messaging_error_24dp);
                            messageHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.error_red));
                            break;

                        default:
                            throw new IllegalArgumentException("Invalid message state");
                    }

                } else {
                    messageHolder.mState.setVisibility(View.GONE);
                }

                messageHolder.mLastMessageText.setText(message.getText());
                messageHolder.mCreatedAt.setText(CreatedAtHelper.createdAtToSimpleString(mContext, message.getCreatedAt()));

                int unseenCount = mUnseenCounts.get(position, -1);

                if (message.getState() == Message.State.DELIVERED &&
                    message.getSender().getID() != currentUser.getID() &&
                    unseenCount > 0) {

                    messageHolder.mIndicator.setVisibility(View.VISIBLE);
                    messageHolder.mIndicator.setText(String.valueOf(unseenCount));

                } else {
                    messageHolder.mIndicator.setVisibility(View.INVISIBLE);
                }

                if (mOnMessageClickListener != null) {
                    messageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnMessageClickListener.onMessageClick(message, holder.getAdapterPosition());
                        }
                    });

                    messageHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return mOnMessageClickListener.onMessageLongClick(message, holder.getAdapterPosition());
                        }
                    });
                }

                break;
            }

            case VIEW_TYPE_SELECTED: {

                SelectedViewHolder selectedHolder = (SelectedViewHolder) holder;

                if (mOnSelectedStateClickListener != null) {
                    selectedHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnSelectedStateClickListener.onDeleteClick(message, holder.getAdapterPosition());
                        }
                    });

                    selectedHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnSelectedStateClickListener.onSelectedEmptyClick(message, holder.getAdapterPosition());
                        }
                    });

                    selectedHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return mOnSelectedStateClickListener.onSelectedEmptyClick(message, holder.getAdapterPosition());
                        }
                    });
                }

                break;
            }

            case VIEW_TYPE_EMPTY_STATE:
                EmptyStateViewHolder emptyStateHolder = (EmptyStateViewHolder) holder;

                emptyStateHolder.mTextView.setText(R.string.no_messages_yet);
                emptyStateHolder.mSearchUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnSearchUserButtonClickListener != null) {
                            mOnSearchUserButtonClickListener.onSearchUserButtonClicked();
                        }
                    }
                });
                break;

            default:
                throw new IllegalArgumentException("Invalid view type at position: " + position);
        }
    }

    public void setSelectedMessage(int selectedPosition) {
        if (mSelectedPosition != -1) {
            notifyItemChanged(mSelectedPosition);
        }
        mSelectedPosition = selectedPosition;
    }

    public ArrayList<Message> getLastMessages() {
        return mLastMessages;
    }

    public void setLastMessages(ArrayList<Message> lastMessages) {
        mLastMessages = lastMessages;
        notifyDataSetChanged();
    }

    public void setUnseenCounts(SparseIntArray unseenCounts) {
        mUnseenCounts = unseenCounts;
        notifyDataSetChanged();
    }

    public interface OnMessageClickListener {
        void onMessageClick(Message lastMessage, int position);
        boolean onMessageLongClick(Message lastMessage, int position);
        void onUserClick(User user);
    }

    public interface OnSelectedStateClickListener {
        boolean onSelectedEmptyClick(Message message, int position);
        void onDeleteClick(Message message, int position);
    }

    public interface OnSearchUserButtonClickListener {
        void onSearchUserButtonClicked();
    }

    public void setOnSearchUserButtonClickListener(OnSearchUserButtonClickListener onSearchUserButtonClickListener) {
        mOnSearchUserButtonClickListener = onSearchUserButtonClickListener;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    public OnSelectedStateClickListener getOnSelectedStateClickListener() {
        return mOnSelectedStateClickListener;
    }

    public void setOnSelectedStateClickListener(OnSelectedStateClickListener onSelectedStateClickListener) {
        mOnSelectedStateClickListener = onSelectedStateClickListener;
    }

    public OnMessageClickListener getOnMessageClickListener() {
        return mOnMessageClickListener;
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        mOnMessageClickListener = onMessageClickListener;
    }
}