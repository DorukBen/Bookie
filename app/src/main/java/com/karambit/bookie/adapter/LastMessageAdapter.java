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

public class LastMessageAdapter extends RecyclerView.Adapter<LastMessageAdapter.MessageViewHolder> {

    private Context mContext;

    private ArrayList<Message> mLastMessages;

    private MessageClickListener mMessageClickListener;

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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mLastMessages.size();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View messageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_last_message, parent, false);
        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        final Message message = mLastMessages.get(position);

        User currentUser = SessionManager.getCurrentUser(mContext);
        User oppositeUser = message.getOppositeUser(currentUser);

        if (! TextUtils.isEmpty(oppositeUser.getThumbnailUrl())) {
            Glide.with(mContext)
                 .load(oppositeUser.getThumbnailUrl())
                 .asBitmap()
                 .centerCrop()
                 .error(R.drawable.error_56dp)
                 .placeholder(R.drawable.placeholder_56dp)
                 .into(holder.mProfilePicture);
        } else {
            holder.mProfilePicture.setImageResource(R.drawable.placeholder_56dp);
        }

        holder.mUserName.setText(oppositeUser.getName());

        if (message.getSender().getID() == currentUser.getID()) {
            holder.mState.setVisibility(View.VISIBLE);

            switch (message.getState()) {
                case PENDING:
                    holder.mState.setImageResource(R.drawable.ic_messaging_pending_18dp);
                    holder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                    break;

                case SENT:
                    holder.mState.setImageResource(R.drawable.ic_messaging_sent_18dp);
                    holder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                    break;

                case DELIVERED:
                    holder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                    holder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                    break;

                case SEEN:
                    holder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                    holder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                    break;

                case ERROR:
                    holder.mState.setImageResource(R.drawable.ic_messaging_error_24dp);
                    holder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.error_red));
                    break;

                default:
                    throw new IllegalArgumentException("Invalid message state");
            }

        } else {
            holder.mState.setVisibility(View.GONE);
        }

        holder.mLastMessageText.setText(message.getText());
        holder.mCreatedAt.setText(calendarToCreatedAt(message.getCreatedAt()));

        if (message.getState() == Message.State.DELIVERED && message.getSender().getID() != currentUser.getID()) {
            holder.mIndicator.setVisibility(View.VISIBLE);

        } else {
            holder.mIndicator.setVisibility(View.INVISIBLE);
        }

        if (mMessageClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMessageClickListener.onMessageClick(message);
                }
            });
        }
    }

    private String calendarToCreatedAt(Calendar calendar) {

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int minute = calendar.get(Calendar.MINUTE);
        String minuteString = minute < 10 ? ("0" + minute) : String.valueOf(minute);

        SimpleDateFormat df = new SimpleDateFormat("kk:mm", Locale.getDefault());

        return df.format(calendar.getTime());
    }

    public ArrayList<Message> getLastMessages() {
        return mLastMessages;
    }

    public void setLastMessages(ArrayList<Message> lastMessages) {
        mLastMessages = lastMessages;
        notifyDataSetChanged();
    }

    public interface MessageClickListener {
        void onMessageClick(Message lastMessage);
    }

    public MessageClickListener getMessageClickListener() {
        return mMessageClickListener;
    }

    public void setMessageClickListener(MessageClickListener messageClickListener) {
        mMessageClickListener = messageClickListener;
    }
}