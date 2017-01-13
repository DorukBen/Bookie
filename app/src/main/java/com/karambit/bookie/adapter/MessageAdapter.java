package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by orcan on 10/27/16.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context mContext;

    private ArrayList<Message> mLastMessages;

    private MessageClickListener mMessageClickListener;

    public MessageAdapter(Context context, ArrayList<Message> lastMessages) {
        mContext = context;
        mLastMessages = lastMessages;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mLastMessageText;
        private TextView mCreatedAt;
        private View mIndicator;

        public MessageViewHolder(View messageView) {
            super(messageView);

            mProfilePicture = (CircleImageView) messageView.findViewById(R.id.profilePicureMessage);
            mUserName = (TextView) messageView.findViewById(R.id.userNameMessage);
            mLastMessageText = (TextView) messageView.findViewById(R.id.lastMessageTextMessage);
            mCreatedAt = (TextView) messageView.findViewById(R.id.createdAtMessage);
            mIndicator = messageView.findViewById(R.id.indicatorMessage);
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
        View messageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        final Message message = mLastMessages.get(position);

        Glide.with(mContext)
                .load(message.getSender().getThumbnailUrl())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.placeholder_book)
                .into(holder.mProfilePicture);


        if (SessionManager.getCurrentUser(mContext).getID() == message.getSender().getID()){
            holder.mUserName.setText(message.getReceiver().getName());
        }else{
            holder.mUserName.setText(message.getSender().getName());
        }
        holder.mLastMessageText.setText(message.getText());
        holder.mCreatedAt.setText(calendarToCreatedAt(message.getCreatedAt()));

        if (message.getState() == Message.State.DELIVERED) {
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
