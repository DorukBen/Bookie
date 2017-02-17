package com.karambit.bookie.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.CircleImageView;
import com.karambit.bookie.helper.CreatedAtHelper;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by orcan on 2/10/17.
 */

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = RequestAdapter.class.getSimpleName();

    private static final int TYPE_SUBTITLE = 1;
    private static final int TYPE_SENT_REQUEST = 2;
    private static final int TYPE_REJECTED_REQUEST = 3;
    private static final int TYPE_EMPTY_STATE = 9;
    private static final int TYPE_NO_CONNECTION = 10;
    private static final int TYPE_UNKNOWN_ERROR = 11;

    public static final int ERROR_TYPE_NONE = 0;
    public static final int ERROR_TYPE_NO_CONNECTION = 1;
    public static final int ERROR_TYPE_UNKNOWN_ERROR = 2;
    private int mErrorType = ERROR_TYPE_NONE;

    private Context mContext;
    private ArrayList<Book.Request> mRequests;
    private Hashtable<Book.Request, String> mLocations;
    private RequestClickListeners mRequestClickListeners;
    private boolean mIsRequestAccepted;

    public RequestAdapter(Context context, ArrayList<Book.Request> requests, Hashtable<Book.Request, String> locations) {
        mContext = context;
        mRequests = requests;
        mLocations = locations;
        mIsRequestAccepted = false;
        for (Book.Request r : requests) {
            if (r.getRequestType() == Book.RequestType.ACCEPT) {
                mIsRequestAccepted = true;
            }
        }
    }

    public RequestAdapter(Context context) {
        mContext = context;
        mRequests = new ArrayList<>();
        mLocations = new Hashtable<>();
        mIsRequestAccepted = false;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfilePicture;
        private TextView mUserName;
        private TextView mCreatedAt;
        private TextView mLocation;
        private ImageButton mAccept;
        private ImageButton mReject;
        private View mUserClickContainer;
        private View mButtonClickContainer;
        private View mDivider;

        public RequestViewHolder(View requestView) {
            super(requestView);

            mProfilePicture = (CircleImageView) requestView.findViewById(R.id.profilePictureRequest);
            mUserName = (TextView) requestView.findViewById(R.id.userNameRequest);
            mCreatedAt = (TextView) requestView.findViewById(R.id.createdAtRequest);
            mLocation = (TextView) requestView.findViewById(R.id.locationRequest);
            mAccept = (ImageButton) requestView.findViewById(R.id.acceptRequest);
            mReject = (ImageButton) requestView.findViewById(R.id.rejectRequest);
            mUserClickContainer = requestView.findViewById(R.id.fromUserClickContainer);
            mButtonClickContainer = requestView.findViewById(R.id.buttonContainer);
            mDivider = requestView.findViewById(R.id.divider);
        }
    }

    private static class SubtitleViewHolder extends RecyclerView.ViewHolder {

        private TextView mSubtitle;

        private SubtitleViewHolder(View subtitleView) {
            super(subtitleView);

            mSubtitle = (TextView) subtitleView.findViewById(R.id.subtitleTextView);
        }
    }

    private static class ExceptionalSituationViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTextView;

        private ExceptionalSituationViewHolder(View noConnectionView) {
            super(noConnectionView);

            mImageView = (ImageView) noConnectionView.findViewById(R.id.emptyStateImageView);
            mTextView = (TextView) noConnectionView.findViewById(R.id.emptyStateTextView);
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mErrorType != ERROR_TYPE_NONE) {
            return 1;
        } else {
            int rejectedRequestCount = getRejectedRequestCount();
            int i = getSentRequestCount() + (rejectedRequestCount > 0 ? rejectedRequestCount + 1/*subtitle*/ : 0);
            return i;
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mErrorType != ERROR_TYPE_NONE){
            if (mErrorType == ERROR_TYPE_NO_CONNECTION){
                return TYPE_NO_CONNECTION;
            } else {
                return TYPE_UNKNOWN_ERROR;
            }
        } else {
            if (position < getSentRequestCount()) {
                return TYPE_SENT_REQUEST;
            } else if (position == getSentRequestCount()) {
                return TYPE_SUBTITLE;
            } else {
                return TYPE_REJECTED_REQUEST;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SENT_REQUEST:case TYPE_REJECTED_REQUEST: {
                View requestView = LayoutInflater.from(mContext).inflate(R.layout.item_request, parent, false);
                return new RequestViewHolder(requestView);
            }

            case TYPE_SUBTITLE: {
                View subtitleView = LayoutInflater.from(mContext).inflate(R.layout.item_subtitle, parent, false);
                return new SubtitleViewHolder(subtitleView);
            }

            case TYPE_EMPTY_STATE:case TYPE_NO_CONNECTION:case TYPE_UNKNOWN_ERROR: {
                View exceptionalView = LayoutInflater.from(mContext).inflate(R.layout.item_empty_state, parent, false);
                return new ExceptionalSituationViewHolder(exceptionalView);
            }

            default: throw new IllegalArgumentException("Invalid viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_SENT_REQUEST:case TYPE_REJECTED_REQUEST: {

                final RequestViewHolder requestViewHolder = (RequestViewHolder) holder;

                int exactRequestPosition = position <= getSentRequestCount() ? position : position - 1; /*SUBTITLE*/
                final Book.Request request = mRequests.get(exactRequestPosition);
                final User fromUser = request.getFromUser();

                if (getItemViewType(position) == TYPE_SENT_REQUEST) {
                    requestViewHolder.mButtonClickContainer.setVisibility(View.VISIBLE);
                    requestViewHolder.mAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mRequestClickListeners != null) {
                                mRequestClickListeners.onAcceptClick(request);
                            }
                        }
                    });

                    requestViewHolder.mReject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mRequestClickListeners != null) {
                                mRequestClickListeners.onRejectClick(request);
                            }
                        }
                    });
                } else {
                    requestViewHolder.mButtonClickContainer.setVisibility(View.GONE);
                }

                requestViewHolder.mUserClickContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRequestClickListeners != null) {
                            mRequestClickListeners.onUserClick(fromUser);
                        }
                    }
                });

                Glide.with(mContext)
                     .load(fromUser.getThumbnailUrl())
                     .asBitmap()
                     .placeholder(R.drawable.placeholder_88dp)
                     .error(R.drawable.error_88dp)
                     .into(requestViewHolder.mProfilePicture);

                requestViewHolder.mUserName.setText(fromUser.getName());

                requestViewHolder.mCreatedAt.setText(CreatedAtHelper.createdAtToSimpleString(mContext, request.getCreatedAt()));

                if (mLocations.containsKey(request)) {
                    requestViewHolder.mLocation.setVisibility(View.VISIBLE);
                    requestViewHolder.mLocation.setText(mLocations.get(request));
                } else {
                    requestViewHolder.mLocation.setVisibility(View.GONE);
                }

                if (position == getSentRequestCount() - 1) {
                    requestViewHolder.mDivider.setVisibility(View.GONE);
                } else {
                    requestViewHolder.mDivider.setVisibility(View.VISIBLE);
                }

                break;
            }

            case TYPE_SUBTITLE: {

                SubtitleViewHolder subtitleHolder = (SubtitleViewHolder) holder;

                subtitleHolder.mSubtitle.setText(R.string.rejected_requests);

                break;
            }

            case TYPE_NO_CONNECTION: {

                ExceptionalSituationViewHolder exceptionalHolder = (ExceptionalSituationViewHolder) holder;

                exceptionalHolder.mTextView.setText(R.string.no_internet_connection);

                break;
            }

            case TYPE_UNKNOWN_ERROR: {

                ExceptionalSituationViewHolder exceptionalHolder = (ExceptionalSituationViewHolder) holder;

                exceptionalHolder.mTextView.setText(R.string.unknown_error);

                break;
            }
        }

    }

    public ArrayList<Book.Request> getRejectedRequests() {
        ArrayList<Book.Request> rejectedRequests = new ArrayList<>();
        for (int i = 0; i < mRequests.size(); i++) {
            Book.Request request = mRequests.get(i);
            if (request.getRequestType() == Book.RequestType.REJECT) {
                rejectedRequests.add(request);
            }
        }
        return rejectedRequests;
    }

    public int getRejectedRequestCount() {
        int count = 0;
        for (int i = 0; i < mRequests.size(); i++) {
            Book.Request request = mRequests.get(i);
            if (request.getRequestType() == Book.RequestType.REJECT) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<Book.Request> getSentRequests() {
        ArrayList<Book.Request> sentRequests = new ArrayList<>();
        for (int i = 0; i < mRequests.size(); i++) {
            Book.Request request = mRequests.get(i);
            if (request.getRequestType() == Book.RequestType.SEND) {
                sentRequests.add(request);
            }
        }
        return sentRequests;
    }

    public int getSentRequestCount() {
        int count = 0;
        for (int i = 0; i < mRequests.size(); i++) {
            Book.Request request = mRequests.get(i);
            if (request.getRequestType() == Book.RequestType.SEND) {
                count++;
            }
        }
        return count;
    }

    public interface RequestClickListeners {
        void onUserClick(User user);
        void onAcceptClick(Book.Request request);
        void onRejectClick(Book.Request request);
    }

    public RequestClickListeners getRequestClickListeners() {
        return mRequestClickListeners;
    }

    public void setRequestClickListeners(RequestClickListeners requestClickListeners) {
        mRequestClickListeners = requestClickListeners;
    }

    public ArrayList<Book.Request> getRequests() {
        return mRequests;
    }

    public void setRequests(ArrayList<Book.Request> requests) {
        mRequests = requests;
    }

    public void setError(int errorType){
        mErrorType = errorType;
        if (errorType != ERROR_TYPE_NONE){
            if (mRequests != null) {
                mRequests.clear();
            }
        }
        notifyDataSetChanged();
    }

    public Hashtable<Book.Request, String> getLocations() {
        return mLocations;
    }

    public void setLocations(Hashtable<Book.Request, String> locations) {
        mLocations = locations;
    }
}
