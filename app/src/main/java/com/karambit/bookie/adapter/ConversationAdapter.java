package com.karambit.bookie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 * Created by orcan on 10/26/16.
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CURRENT_USER = 0;
    private static final int TYPE_OPPOSITE_USER = 1;

    private Context mContext;
    private ArrayList<Message> mMessages;
    private User mCurrentUser;
    private User mOppositeUser;

    private OnMessageClickListener mOnMessageClickListener;

    private ArrayList<Integer> mSelectedIndexes = new ArrayList<>();

    public ConversationAdapter(Context context, ArrayList<Message> messages) {
        mContext = context;
        mMessages = messages;

        mCurrentUser = SessionManager.getCurrentUser(mContext);
        mOppositeUser = messages.get(0).getOppositeUser(mCurrentUser);
    }

    private static class CurrentUserMessageViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mText;
        private CircleImageView mProfilePicture;
        private TextView mCreatedAt;
        private ImageView mState;
        private ImageButton mError;
        private LinearLayout mTextAndMeta;

        public CurrentUserMessageViewHolder(View currentUserMessageView) {
            super(currentUserMessageView);

            mCardView = (CardView) currentUserMessageView.findViewById(R.id.currentUserMessageCardView);
            mText = (TextView) currentUserMessageView.findViewById(R.id.currentUserMessageText);
            mProfilePicture = (CircleImageView) currentUserMessageView.findViewById(R.id.currentUserMessageProfilePicture);
            mCreatedAt = (TextView) currentUserMessageView.findViewById(R.id.currentUserCreatedAt);
            mState = (ImageView) currentUserMessageView.findViewById(R.id.messageState);
            mError = (ImageButton) currentUserMessageView.findViewById(R.id.messageErrorImageView);
            mTextAndMeta = (LinearLayout) currentUserMessageView.findViewById(R.id.textAndMetaLinearLayout);
        }
    }

    private static class OppositeUserMessageViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mText;
        private CircleImageView mProfilePicture;
        private TextView mCreatedAt;

        public OppositeUserMessageViewHolder(View oppositeUserMessageView) {
            super(oppositeUserMessageView);

            mCardView = (CardView) oppositeUserMessageView.findViewById(R.id.oppositeUserMessageCardView);
            mText = (TextView) oppositeUserMessageView.findViewById(R.id.oppositeUserMessageText);
            mProfilePicture = (CircleImageView) oppositeUserMessageView.findViewById(R.id.oppositeUserMessageProfilePicture);
            mCreatedAt = (TextView) oppositeUserMessageView.findViewById(R.id.oppositeUserCreatedAt);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {

        Message message = mMessages.get(position);
        User currentUser = SessionManager.getCurrentUser(mContext);

        if (message.getSender().getID() == currentUser.getID()) {
            return TYPE_CURRENT_USER;
        } else {
            return TYPE_OPPOSITE_USER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //View inflating for view types and creating ViewHolders

        switch (viewType) {

            case TYPE_CURRENT_USER:
                View phoneOwnerMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_current_user_message, parent, false);
                return new CurrentUserMessageViewHolder(phoneOwnerMessageView);

            case TYPE_OPPOSITE_USER:
                View oppositeUserMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_opposite_user_message, parent, false);
                return new OppositeUserMessageViewHolder(oppositeUserMessageView);

            default:
                throw new IllegalArgumentException("Invalid view type variable: viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final int finalPosition = position;

        final Message message = mMessages.get(position);

        if (mSelectedIndexes.contains(position)) {
            int color = ContextCompat.getColor(mContext, R.color.colorAccent);
            color = Color.argb((int) (255 * 1f / 4f), Color.red(color), Color.green(color), Color.blue(color));
            holder.itemView.setBackgroundColor(color);
        } else {
            holder.itemView.setBackgroundColor(0);
        }

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mOnMessageClickListener.onMessageLongClick(message, finalPosition);
            }
        };
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnMessageClickListener.onMessageClick(message, finalPosition);
            }
        };
        switch (getItemViewType(position)) {

            case TYPE_CURRENT_USER: {

                final CurrentUserMessageViewHolder currentUserHolder = (CurrentUserMessageViewHolder) holder;

                switch (message.getState()) {

                    case PENDING:
                        currentUserHolder.mState.setImageResource(R.drawable.ic_messaging_pending_18dp);
                        currentUserHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                        currentUserHolder.mError.setVisibility(View.GONE);
                        break;

                    case SENT:
                        currentUserHolder.mState.setImageResource(R.drawable.ic_messaging_sent_18dp);
                        currentUserHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                        currentUserHolder.mError.setVisibility(View.GONE);
                        break;

                    case DELIVERED:
                        currentUserHolder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                        currentUserHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                        currentUserHolder.mError.setVisibility(View.GONE);
                        break;

                    case SEEN:
                        currentUserHolder.mState.setImageResource(R.drawable.ic_messaging_delivered_seen_18dp);
                        currentUserHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                        currentUserHolder.mError.setVisibility(View.GONE);
                        break;

                    case ERROR:
                        currentUserHolder.mState.setImageResource(R.drawable.ic_messaging_pending_18dp);
                        currentUserHolder.mState.setColorFilter(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                        currentUserHolder.mError.setVisibility(View.VISIBLE);
                        break;
                }

                currentUserHolder.mText.setText(message.getText());

                currentUserHolder.mTextAndMeta.post(new Runnable() {
                    @Override
                    public void run() {

                        if (currentUserHolder.mText.getLineCount() > 2) {
                            currentUserHolder.mTextAndMeta.setOrientation(LinearLayout.VERTICAL);
                        } else {
                            currentUserHolder.mTextAndMeta.setOrientation(LinearLayout.HORIZONTAL);
                        }
                    }
                });

                currentUserHolder.mCreatedAt.setText(createdAtToString(message.getCreatedAt()));

                if (!TextUtils.isEmpty(mCurrentUser.getThumbnailUrl())) {
                    Glide.with(mContext)
                         .load(mCurrentUser.getThumbnailUrl())
                         .asBitmap()
                         .centerCrop()
                         .error(R.drawable.error_36dp)
                         .placeholder(R.drawable.placeholder_36dp)
                         .into(currentUserHolder.mProfilePicture);
                } else {
                    currentUserHolder.mProfilePicture.setImageResource(R.drawable.placeholder_36dp);
                }

                if (position == getItemCount() - 1) {
                    currentUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else if (mMessages.get(position + 1).getSender().getID() != mCurrentUser.getID()) {
                    currentUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else {
                    currentUserHolder.mProfilePicture.setVisibility(View.INVISIBLE);
                }

                if (mOnMessageClickListener != null) {
                    currentUserHolder.mCardView.setOnClickListener(onClickListener);
                    currentUserHolder.mCardView.setOnLongClickListener(onLongClickListener);
                    currentUserHolder.itemView.setOnClickListener(onClickListener);
                    currentUserHolder.itemView.setOnLongClickListener(onLongClickListener);
                }

                break;
            }

            case TYPE_OPPOSITE_USER: {

                final OppositeUserMessageViewHolder oppositeUserHolder = (OppositeUserMessageViewHolder) holder;

                oppositeUserHolder.mText.setText(message.getText());

                oppositeUserHolder.mCreatedAt.setText(createdAtToString(message.getCreatedAt()));

                if (!TextUtils.isEmpty(mOppositeUser.getThumbnailUrl())) {
                    Glide.with(mContext)
                         .load(mOppositeUser.getThumbnailUrl())
                         .asBitmap()
                         .centerCrop()
                         .error(R.drawable.error_36dp)
                         .placeholder(R.drawable.placeholder_36dp)
                         .into(oppositeUserHolder.mProfilePicture);
                } else {
                    oppositeUserHolder.mProfilePicture.setImageResource(R.drawable.placeholder_36dp);
                }

                if (position == getItemCount() - 1) {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else if (mMessages.get(position + 1).getSender().getID() != mOppositeUser.getID()) {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.VISIBLE);

                } else {
                    oppositeUserHolder.mProfilePicture.setVisibility(View.INVISIBLE);
                }

                if (mOnMessageClickListener != null) {
                    oppositeUserHolder.mCardView.setOnClickListener(onClickListener);
                    oppositeUserHolder.mCardView.setOnLongClickListener(onLongClickListener);
                    oppositeUserHolder.itemView.setOnClickListener(onClickListener);
                    oppositeUserHolder.itemView.setOnLongClickListener(onLongClickListener);
                }

                break;
            }
        }
    }

    private String createdAtToString(Calendar createdAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(createdAt.getTime());
    }

    public interface OnMessageClickListener {
        void onMessageClick(Message message, int position);
        boolean onMessageLongClick(Message message, int position);
    }

    public OnMessageClickListener getOnMessageClickListener() {
        return mOnMessageClickListener;
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        mOnMessageClickListener = onMessageClickListener;
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public void setMessages(ArrayList<Message> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return mSelectedIndexes;
    }

    public void setSelectedIndexes(ArrayList<Integer> selectedIndexes) {
        mSelectedIndexes = selectedIndexes;
    }
}
