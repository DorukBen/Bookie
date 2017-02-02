package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by orcan on 10/27/16.
 */

public class LastMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_UNSELECTED = 0;
    public static final int VIEW_TYPE_SELECTED = 1;

    private Context mContext;

    private ArrayList<Message> mLastMessages;

    private int mSelectedPosition = -1;

    private OnMessageClickListener mOnMessageClickListener;
    private OnSelectedStateClickListener mOnSelectedStateClickListener;

    public LastMessageAdapter(Context context, ArrayList<Message> lastMessages) {
        mContext = context;
        mLastMessages = lastMessages;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mLastMessageText;
        private TextView mCreatedAt;
        private View mIndicator;
        private ImageView mState;

        public MessageViewHolder(View messageView) {
            super(messageView);

            mProfilePicture = (CircleImageView) messageView.findViewById(R.id.profilePicureMessage);
            mUserName = (TextView) messageView.findViewById(R.id.userNameMessage);
            mLastMessageText = (TextView) messageView.findViewById(R.id.lastMessageTextMessage);
            mCreatedAt = (TextView) messageView.findViewById(R.id.createdAtMessage);
            mIndicator = messageView.findViewById(R.id.indicatorMessage);
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mSelectedPosition) {
            return VIEW_TYPE_SELECTED;
        } else {
            return VIEW_TYPE_UNSELECTED;
        }
    }

    @Override
    public int getItemCount() {
        return mLastMessages.size();
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

            default:
                throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final int finalPosition = position;

        final Message message = mLastMessages.get(position);

        switch (getItemViewType(position)) {

            case VIEW_TYPE_UNSELECTED: {

                MessageViewHolder messageHolder = (MessageViewHolder) holder;

                User currentUser = SessionManager.getCurrentUser(mContext);
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

                messageHolder.mUserName.setText(oppositeUser.getName());

                if (message.getSender().getID() == currentUser.getID()) {
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
                messageHolder.mCreatedAt.setText(calendarToCreatedAt(message.getCreatedAt()));

                if (message.getState() == Message.State.DELIVERED && message.getSender().getID() != currentUser.getID()) {
                    messageHolder.mIndicator.setVisibility(View.VISIBLE);

                } else {
                    messageHolder.mIndicator.setVisibility(View.INVISIBLE);
                }

                if (mOnMessageClickListener != null) {
                    messageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnMessageClickListener.onMessageClick(message, finalPosition);
                        }
                    });

                    messageHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return mOnMessageClickListener.onMessageLongClick(message, finalPosition);
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
                            mOnSelectedStateClickListener.onDeleteClick(message, finalPosition);
                        }
                    });

                    selectedHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnSelectedStateClickListener.onSelectedEmptyClick(message, finalPosition);
                        }
                    });

                    selectedHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return mOnSelectedStateClickListener.onSelectedEmptyClick(message, finalPosition);
                        }
                    });
                }

                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type at position: " + position);
        }
    }

    private String calendarToCreatedAt(Calendar calendar) {

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int minute = calendar.get(Calendar.MINUTE);
        String minuteString = minute < 10 ? ("0" + minute) : String.valueOf(minute);

        SimpleDateFormat df = new SimpleDateFormat("kk:mm", Locale.getDefault());

        return df.format(calendar.getTime());
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

    public interface OnMessageClickListener {
        void onMessageClick(Message lastMessage, int position);
        boolean onMessageLongClick(Message lastMessage, int position);
    }

    public interface OnSelectedStateClickListener {
        boolean onSelectedEmptyClick(Message message, int position);
        void onDeleteClick(Message message, int position);
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