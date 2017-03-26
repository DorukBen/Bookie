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

public class Transaction implements Book.BookProcess, Parcelable{

    public enum Type {

        DISPACTH(8),
        COME_TO_HAND(9),
        LOST(10);

        private final int mTransactionCode;

        Type(int code) {
            mTransactionCode = code;
        }

        public int getTransactionCode() {
            return mTransactionCode;
        }

        public static Type valueOf(int transactionCode) {
            if (transactionCode == DISPACTH.mTransactionCode) {
                return DISPACTH;
            } else if (transactionCode == COME_TO_HAND.mTransactionCode) {
                return COME_TO_HAND;
            } else if (transactionCode == LOST.mTransactionCode) {
                return LOST;
            } else {
                throw new IllegalArgumentException("Invalid transaction type");
            }
        }
    }

    private Book mBook;
    private User mFromUser;
    private User mToUser;
    private Type mType;
    private Calendar mCreatedAt;

    public Transaction(@NonNull Book book, User fromUser, User toUser, Type type, Calendar createdAt) {
        mBook = book;
        mFromUser = fromUser;
        mToUser = toUser;
        mType = type;
        mCreatedAt = createdAt;
    }

    public Transaction(@NonNull Book book, User fromUser, User toUser, Type type, String createdAt) {
        mBook = book;
        mFromUser = fromUser;
        mToUser = toUser;
        mType = type;
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(timestamp.getTime());
    }

    public Book getBook() {
        return mBook;
    }

    public User getFromUser() {
        return mFromUser;
    }

    public void setFromUser(User fromUser) {
        mFromUser = fromUser;
    }

    public User getToUser() {
        return mToUser;
    }

    public void setToUser(User toUser) {
        mToUser = toUser;
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

    public static Transaction jsonObjectToTransaction(@NonNull Book book, @NonNull JSONObject jsonObject) throws JSONException {
        return new Transaction(book,
                               User.jsonObjectToUser(jsonObject.getJSONObject("fromUser")),
                               User.jsonObjectToUser(jsonObject.getJSONObject("toUser")),
                               Type.valueOf(jsonObject.getInt("transactionType")),
                               jsonObject.getString("createdAt"));
    }

    public static ArrayList<Transaction> jsonArrayToTransactionList(@NonNull Book book, @NonNull JSONArray jsonArray) {
        ArrayList<Transaction> transactions = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                transactions.add(Transaction.jsonObjectToTransaction(book, jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }


    protected Transaction(Parcel in) {
        mBook = in.readParcelable(Book.class.getClassLoader());
        mFromUser = in.readParcelable(User.class.getClassLoader());
        mToUser = in.readParcelable(User.class.getClassLoader());
        mType = (Type) in.readSerializable();
        long millis = in.readLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        mCreatedAt = calendar;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBook, flags);
        dest.writeParcelable(mFromUser, flags);
        dest.writeParcelable(mToUser, flags);
        dest.writeSerializable(mType);
        dest.writeLong(mCreatedAt.getTimeInMillis());
    }

    @Override
    public String toString() {
        return mBook + ".Transaction{" +
            "mTransactionType=" + mType +
            ", mFromUser=" + mFromUser.getName() +
            ", mToUser=" + mToUser.getName() +
            ", mCreatedAt=" + mCreatedAt.getTimeInMillis() + '}';
    }

    @Override
    public void accept(Book.TimelineDisplayableVisitor visitor) {
        visitor.visit(this);
    }
}
