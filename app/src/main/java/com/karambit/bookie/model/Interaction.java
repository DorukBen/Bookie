package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by orcan on 3/26/17.
 */

public class Interaction implements Book.BookProcess, Parcelable {

    public enum Type {

        ADD(0),
        READ_START(1),
        READ_STOP(2),
        OPEN_TO_SHARE(3),
        CLOSE_TO_SHARE(4);

        private final int mInteractionCode;

        Type(int code) {
            mInteractionCode = code;
        }

        public int getInteractionCode() {
            return mInteractionCode;
        }

        public static Type valueOf(int interactionCode) {
            if (interactionCode == ADD.mInteractionCode) {
                return ADD;
            } else if (interactionCode == READ_START.mInteractionCode) {
                return READ_START;
            } else if (interactionCode == READ_STOP.mInteractionCode) {
                return READ_STOP;
            } else if (interactionCode == OPEN_TO_SHARE.mInteractionCode) {
                return OPEN_TO_SHARE;
            } else if (interactionCode == CLOSE_TO_SHARE.mInteractionCode) {
                return CLOSE_TO_SHARE;
            } else {
                throw new IllegalArgumentException("Invalid interaction type");
            }
        }
    }

    private Book mBook;
    private User mUser;
    private Type mType;
    private Calendar mCreatedAt;

    public Interaction(@NonNull Book book, User user, Type type, Calendar createdAt) {
        mBook = book;
        mUser = user;
        mType = type;
        mCreatedAt = createdAt;
    }

    public Interaction(@NonNull Book book, User user, Type type, String createdAt) {
        mBook = book;
        mUser = user;
        mType = type;
        Timestamp timestamp = Timestamp.valueOf(createdAt);
        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(timestamp.getTime());
    }

    public Book getBook() {
        return mBook;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
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

    public static Interaction jsonObjectToInteraction(@NonNull Book book, @NonNull JSONObject jsonObject) throws JSONException {
        return new Interaction(book,
                        User.jsonObjectToUser(jsonObject.getJSONObject("user")),
                        Interaction.Type.valueOf(jsonObject.getInt("interactionType")),
                        jsonObject.getString("createdAt"));
    }

    public static ArrayList<Interaction> jsonArrayToInteractionList(@NonNull Book book, @NonNull JSONArray jsonArray) {
        ArrayList<Interaction> interactions = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                interactions.add(Interaction.jsonObjectToInteraction(book, jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return interactions;
    }

    protected Interaction(Parcel in) {
        mBook = in.readParcelable(Book.class.getClassLoader());
        mUser = in.readParcelable(User.class.getClassLoader());
        mType = (Type) in.readSerializable();
        long millis = in.readLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        mCreatedAt = calendar;
    }

    public static final Creator<Interaction> CREATOR = new Creator<Interaction>() {
        @Override
        public Interaction createFromParcel(Parcel in) {
            return new Interaction(in);
        }

        @Override
        public Interaction[] newArray(int size) {
            return new Interaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBook, flags);
        dest.writeParcelable(mUser, flags);
        dest.writeSerializable(mType);
        dest.writeLong(mCreatedAt.getTimeInMillis());
    }

    @Override
    public String toString() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault());
        String createdAt = dateFormat.format(mCreatedAt.getTime());

        return mBook.toShortString() + ".Interaction{" +
            "\n\tmInteractionType=" + mType + "," +
            "\n\tmUser=" + mUser.toShortString() +
            ", mCreatedAt=" + createdAt + '}';
    }

    @Override
    public void accept(Book.TimelineDisplayableVisitor visitor) {
        visitor.visit(this);
    }
}