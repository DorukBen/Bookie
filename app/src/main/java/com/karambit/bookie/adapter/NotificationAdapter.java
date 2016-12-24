package com.karambit.bookie.adapter;

import android.content.Context;
import android.content.Intent;
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
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by doruk on 24.12.2016.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_TYPE_USER_PHOTO = 0;
    public static final int ITEM_TYPE_BOOK_PHOTO = 1;

    private Context mContext;
    private ArrayList<Notification> mNotifications;
    private SpanTextClickListeners mSpanTextClickListeners;

    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        mContext = context;
        mNotifications = notifications;
    }

    public static class NotificationUserViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mUserImageView;
        private TextView mMessage;
        private TextView mCreatedAt;
        private View mNotificationIndicator;

        public NotificationUserViewHolder(View itemView) {
            super(itemView);

            mUserImageView = (CircleImageView) itemView.findViewById(R.id.notificationUserImageView);
            mMessage = (TextView) itemView.findViewById(R.id.notificationMessageTextView);
            mCreatedAt = (TextView) itemView.findViewById(R.id.createdAtNotification);
            mNotificationIndicator = itemView.findViewById(R.id.indicatorNotification);
        }
    }

    public static class NotificationBookViewHolder extends RecyclerView.ViewHolder {
        private ImageView mBookImageView;
        private TextView mMessage;
        private TextView mCreatedAt;
        private View mNotificationIndicator;

        public NotificationBookViewHolder(View itemView) {
            super(itemView);

            mBookImageView = (ImageView) itemView.findViewById(R.id.notificationBookImageView);
            mMessage = (TextView) itemView.findViewById(R.id.notificationMessageTextView);
            mCreatedAt = (TextView) itemView.findViewById(R.id.createdAtNotification);
            mNotificationIndicator = itemView.findViewById(R.id.indicatorNotification);
        }
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mNotifications.get(position).getType()) {
            case REQUESTED:
                return ITEM_TYPE_USER_PHOTO;
            case REQUEST_ACCEPTED:
                return ITEM_TYPE_BOOK_PHOTO;
            case REQUEST_REJECTED:
                return ITEM_TYPE_BOOK_PHOTO;
            case BOOK_OWNER_CHANGED:
                return ITEM_TYPE_USER_PHOTO;
            case BOOK_LOST:
                return ITEM_TYPE_USER_PHOTO;
            default:
                throw new IllegalArgumentException("Position WTF?");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_BOOK_PHOTO:
                View notificationBookPhotoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_book_photo , parent, false);
                return new NotificationBookViewHolder(notificationBookPhotoView);

            case ITEM_TYPE_USER_PHOTO:
                View notificationUserPhotoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_user_photo , parent, false);
                return new NotificationUserViewHolder(notificationUserPhotoView);

            default:
                throw new IllegalArgumentException("Unsupported view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Notification notification = mNotifications.get(position);

        SpannableString spanUserName = new SpannableString(notification.getOppositeUser().getName());
        ClickableSpan clickableSpanUserName = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                mSpanTextClickListeners.onUserNameClick(notification.getOppositeUser());
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        SpannableString spanBookName = new SpannableString(notification.getBook().getName());

        ClickableSpan clickableSpanBookName = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                mSpanTextClickListeners.onBookNameClick(notification.getBook());
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


        spanBookName.setSpan(clickableSpanBookName, 0, spanBookName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanBookName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primaryTextColor)), 0, spanBookName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanBookName.setSpan(new StyleSpan(Typeface.BOLD), 0, spanBookName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        switch (getItemViewType(position)) {
            case ITEM_TYPE_USER_PHOTO:

                NotificationUserViewHolder userHolder = (NotificationUserViewHolder) holder;

                if (notification.isSeen()) {
                    userHolder.mNotificationIndicator.setVisibility(View.INVISIBLE);
                } else {
                    userHolder.mNotificationIndicator.setVisibility(View.VISIBLE);
                }

                Glide.with(mContext)
                        .load(notification.getOppositeUser().getThumbnailUrl())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_book)
                        .into(userHolder.mUserImageView);

                userHolder.mUserImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSpanTextClickListeners.onUserPhotoClick(notification.getOppositeUser());
                    }
                });

                userHolder.mCreatedAt.setText(calendarToCreatedAt(notification.getCreatedAt()));

                switch (notification.getType()) {

                    case REQUESTED:
                        userHolder.mMessage.setText(TextUtils.concat(spanUserName," requested for " ,spanBookName));
                        break;
                    case BOOK_OWNER_CHANGED:
                        userHolder.mMessage.setText(TextUtils.concat(spanBookName," now owned by " ,spanUserName));
                        break;
                    case BOOK_LOST:
                        userHolder.mMessage.setText(TextUtils.concat(spanUserName," lost " ,spanBookName));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid notification type");
                }

                userHolder.mMessage.setMovementMethod(LinkMovementMethod.getInstance());
                userHolder.mMessage.setHighlightColor(Color.TRANSPARENT);

                break;

            case ITEM_TYPE_BOOK_PHOTO:

                NotificationBookViewHolder bookHolder = (NotificationBookViewHolder) holder;

                if (notification.isSeen()) {
                    bookHolder.mNotificationIndicator.setVisibility(View.INVISIBLE);
                } else {
                    bookHolder.mNotificationIndicator.setVisibility(View.VISIBLE);
                }

                Glide.with(mContext)
                        .load(notification.getBook().getThumbnailURL())
                        .centerCrop()
                        .crossFade()
                        .placeholder(R.drawable.placeholder_book)
                        .into(bookHolder.mBookImageView);

                bookHolder.mBookImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSpanTextClickListeners.onBookImageClick(notification.getBook());
                    }
                });

                bookHolder.mCreatedAt.setText(calendarToCreatedAt(notification.getCreatedAt()));

                switch (notification.getType()) {
                    case REQUEST_ACCEPTED:
                        bookHolder.mMessage.setText(TextUtils.concat(spanUserName," accepted your request for " ,spanBookName));
                        break;

                    case REQUEST_REJECTED:
                        bookHolder.mMessage.setText(TextUtils.concat(spanUserName," rejected your request for " ,spanBookName));
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid notification type");
                }

                bookHolder.mMessage.setMovementMethod(LinkMovementMethod.getInstance());
                bookHolder.mMessage.setHighlightColor(Color.TRANSPARENT);

                break;
        }
    }

    public ArrayList<Notification> getNotifications() {
        return mNotifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        mNotifications = notifications;
        notifyDataSetChanged();
    }

    private String calendarToCreatedAt(Calendar calendar) {

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int minute = calendar.get(Calendar.MINUTE);
        String minuteString = minute < 10 ? ("0" + minute) : String.valueOf(minute);

        SimpleDateFormat df = new SimpleDateFormat("kk:mm", Locale.getDefault());

        return df.format(calendar.getTime());
    }

    public interface SpanTextClickListeners{
        void onUserNameClick(User user);

        void onBookNameClick(Book book);

        void onUserPhotoClick(User user);

        void onBookImageClick(Book book);
    }

    public SpanTextClickListeners getSpanTextClickListeners() {
        return mSpanTextClickListeners;
    }

    public void setSpanTextClickListeners(SpanTextClickListeners spanTextClickListeners){
        mSpanTextClickListeners = spanTextClickListeners;
    }
}
