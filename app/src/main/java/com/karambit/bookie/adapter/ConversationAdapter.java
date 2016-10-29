package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by orcan on 10/26/16.
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PHONE_OWNER = 0;
    private static final int TYPE_OPPOSITE_USER = 1;

    private RecyclerView mRecyclerView;

    private Context mContext;
    private User mPhoneOwner;
    private User mOppositeUser;
    private ArrayList<Message> mMessages;

    public ConversationAdapter(Context context, User phoneOwner, User oppositeUser, ArrayList<Message> messages) {
        mContext = context;
        mPhoneOwner = phoneOwner;
        mOppositeUser = oppositeUser;
        mMessages = messages;
    }

    private static class PhoneOwnerMessageViewHolder extends RecyclerView.ViewHolder {
        private View mRootView;
        private TextView mText;
        private CircleImageView mProfilePicture;
        private TextView mCreatedAt;

        public PhoneOwnerMessageViewHolder(View phoneOwnerMessageView) {
            super(phoneOwnerMessageView);

            mRootView = phoneOwnerMessageView.findViewById(R.id.phoneOwnerMessageRoot);
            mText = (TextView) phoneOwnerMessageView.findViewById(R.id.phoneOwnerMessageText);
            mProfilePicture = (CircleImageView) phoneOwnerMessageView.findViewById(R.id.phoneOwnerMessageProfilePicture);
            mCreatedAt = (TextView) phoneOwnerMessageView.findViewById(R.id.phoneOwnerCreatedAt);
        }
    }

    private static class OppositeUserMessageViewHolder extends RecyclerView.ViewHolder {
        private View mRootView;
        private TextView mText;
        private CircleImageView mProfilePicture;
        private TextView mCreatedAt;

        public OppositeUserMessageViewHolder(View oppositeUserMessageView) {
            super(oppositeUserMessageView);

            mRootView = oppositeUserMessageView.findViewById(R.id.oppositeUserMessageRoot);
            mText = (TextView) oppositeUserMessageView.findViewById(R.id.oppositeUserMessageText);
            mProfilePicture = (CircleImageView) oppositeUserMessageView.findViewById(R.id.oppositeUserMessageProfilePicture);
            mCreatedAt = (TextView) oppositeUserMessageView.findViewById(R.id.oppositeUserCreatedAt);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {

        User sender = mMessages.get(position).getSender();

        if (sender.getID() == mPhoneOwner.getID()) {
            return TYPE_PHONE_OWNER;
        } else if (sender.getID() == mOppositeUser.getID()) {
            return TYPE_OPPOSITE_USER;
        }

        throw new IllegalArgumentException("Invalid view type at position: " + position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //View inflating for view types and creating ViewHolders

        switch (viewType) {

            case TYPE_PHONE_OWNER:
                View phonOwnerMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_phone_owner_message, parent, false);
                return new PhoneOwnerMessageViewHolder(phonOwnerMessageView);

            case TYPE_OPPOSITE_USER:
                View oppositeUserMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_opposite_user_message, parent, false);
                return new OppositeUserMessageViewHolder(oppositeUserMessageView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        switch (getItemViewType(position)) {

            case TYPE_PHONE_OWNER: {

                Message phoneOwnerMessage = mMessages.get(position);

                PhoneOwnerMessageViewHolder phoneOwnerHolder = (PhoneOwnerMessageViewHolder) holder;

                phoneOwnerHolder.mText.setText(phoneOwnerMessage.getText());

                phoneOwnerHolder.mCreatedAt.setText(calendarToCreatedAt(phoneOwnerMessage.getCreatedAt()));

                Glide.with(mContext)
                        .load(mPhoneOwner.getThumbnailUrl())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_book)
                        .into(phoneOwnerHolder.mProfilePicture);

                if (position == getItemCount() - 1) {
                    phoneOwnerHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else if (mMessages.get(position + 1).getSender().getID() != mPhoneOwner.getID()) {
                    phoneOwnerHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else {
                    phoneOwnerHolder.mProfilePicture.setVisibility(View.INVISIBLE);
                }

                break;
            }

            case TYPE_OPPOSITE_USER: {

                final Message oppositeUserMessage = mMessages.get(position);

                final OppositeUserMessageViewHolder oppositeUserHolder = (OppositeUserMessageViewHolder) holder;

                oppositeUserHolder.mText.setText(oppositeUserMessage.getText());

                oppositeUserHolder.mCreatedAt.setText(calendarToCreatedAt(oppositeUserMessage.getCreatedAt()));

                Glide.with(mContext)
                        .load(mOppositeUser.getThumbnailUrl())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_book)
                        .into(oppositeUserHolder.mProfilePicture);

                  if (position == 0) {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else if (mMessages.get(position - 1).getSender().getID() != mOppositeUser.getID()) {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.INVISIBLE);
                }

                break;
            }
        }
    }

    private String calendarToCreatedAt(Calendar calendar) {

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int minute = calendar.get(Calendar.MINUTE);
        String minuteString = minute < 10 ? ("0" + minute) : String.valueOf(minute);

        SimpleDateFormat df = new SimpleDateFormat("kk:mm", Locale.getDefault());

        return df.format(calendar.getTime());
    }

    public void insertNewMessage(Message message) {
        mMessages.add(0, message);
        notifyItemInserted(0);

        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void insertNewMessageList(ArrayList<Message> messages) {
        if (messages.size() > 0) {
            mMessages.addAll(messages);
            Collections.sort(mMessages);
            notifyItemRangeInserted(0, messages.size() - 1);

            if (mRecyclerView != null) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public User getPhoneOwner() {
        return mPhoneOwner;
    }

    public void setPhoneOwner(User phoneOwner) {
        mPhoneOwner = phoneOwner;
    }

    public User getOppositeUser() {
        return mOppositeUser;
    }

    public void setOppositeUser(User oppositeUser) {
        mOppositeUser = oppositeUser;
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public void setMessages(ArrayList<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();

        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }
}
