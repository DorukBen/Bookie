package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by orcan on 3/26/17.
 */

public class Request implements Book.BookProcess, Parcelable, Comparable<Request> {

    public enum Type {

        SEND(5),
        ACCEPT(6),
        REJECT(7);

        private final int mRequestCode;

        Type(int code) {
            mRequestCode = code;
        }

        public int getRequestCode() {
            return mRequestCode;
        }

        public static Type valueOf(int requestCode) {
            if (requestCode == SEND.mRequestCode) {
                return SEND;
            } else if (requestCode == ACCEPT.mRequestCode) {
                return ACCEPT;
            } else if (requestCode == REJECT.mRequestCode) {
                return REJECT;
            } else {
                throw new IllegalArgumentException("Invalid request type");
            }
        }
    }

    private Book mBook;
    private User mRequester;
    private User mResponder; // Book owner at that moment
    private Type mType;
    private Calendar mCreatedAt;

    public Request(@NonNull Book book, User requester, User responder, Type type, Calendar createdAt) {
        mBook = book;
        mResponder = responder;
        mRequester = requester;
        mType = type;
        mCreatedAt = createdAt;
    }

    public Request(@NonNull Book book, User requester, User responder, Type type, String createdAt) {
        mBook = book;
        mResponder = responder;
        mRequester = requester;
        mType = type;
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(timestamp.getTime());
    }

    public Book getBook() {
        return mBook;
    }

    public User getRequester() {
        return mRequester;
    }

    public void setRequester(User requester) {
        mRequester = requester;
    }

    public User getResponder() {
        return mResponder;
    }

    public void setResponder(User responder) {
        mResponder = responder;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    @Override
    public Calendar getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        mCreatedAt = createdAt;
    }

    public static Request jsonObjectToRequest(@NonNull Book book, @NonNull JSONObject jsonObject) throws JSONException {
        return new Request(book,
                           User.jsonObjectToUser(jsonObject.getJSONObject("requester")), // TODO Server key change ("fromUser" -> "requester")
                           User.jsonObjectToUser(jsonObject.getJSONObject("responder")),  // TODO Server key change ("toUser" -> "responder")
                           Type.valueOf(jsonObject.getInt("requestType")),
                           jsonObject.getString("createdAt"));
    }

    public static ArrayList<Request> jsonArrayToRequestList(@NonNull Book book, @NonNull JSONArray jsonArray) {
        ArrayList<Request> requests = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                requests.add(Request.jsonObjectToRequest(book, jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return requests;
    }

    protected Request(Parcel in) {
        mBook = in.readParcelable(Book.class.getClassLoader());
        mRequester = in.readParcelable(User.class.getClassLoader());
        mResponder = in.readParcelable(User.class.getClassLoader());
        mType = (Type) in.readSerializable();
        long millis = in.readLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        mCreatedAt = calendar;
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBook, flags);
        dest.writeParcelable(mRequester, flags);
        dest.writeParcelable(mResponder, flags);
        dest.writeSerializable(mType);
        dest.writeLong(mCreatedAt.getTimeInMillis());
    }

    @Override
    public String toString() {
        return mBook + ".Request{" +
            "mType=" + mType +
            ", mRequester=" + mRequester.getName() +
            ", mResponder=" + mResponder.getName() +
            ", mCreatedAt=" + mCreatedAt.getTimeInMillis() + '}';
    }

    @Override
    public void accept(Book.TimelineDisplayableVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int compareTo(@NonNull Request otherRequest) {
        int ordinalDifference = this.mType.ordinal() - otherRequest.getType().ordinal();
        if (ordinalDifference != 0) {
            return ordinalDifference;
        } else {
            return (int) (otherRequest.getCreatedAt().getTimeInMillis() - this.mCreatedAt.getTimeInMillis());
        }
    }
}