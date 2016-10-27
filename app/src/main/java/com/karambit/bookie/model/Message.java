package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

/**
 * Message model
 *
 * Created by orcan on 10/25/16.
 */

public class Message implements Parcelable, Comparable<Message> {

    public enum State { PENDING, SENT, DELIVERED, SEEN, ERROR }

    private String mText;
    private User mSender;
    private Calendar mCreatedAt;
    private State mState;

    public Message(String text, User sender, Calendar createdAt, State state) {
        mText = text;
        mSender = sender;
        mCreatedAt = createdAt;
        mState = state;
    }

    protected Message(Parcel in) {
        mText = in.readString();
        mSender = in.readParcelable(User.class.getClassLoader());

        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(in.readLong());

        mState = (State) in.readSerializable();
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public User getSender() {
        return mSender;
    }

    public void setSender(User sender) {
        mSender = sender;
    }

    public Calendar getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        mCreatedAt = createdAt;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
    }

    public static class GENERATOR {

        private static Random RANDOM = new Random();
        private static int MIN_IN_MILLIS = 60000; // 60 * 1000
        private static char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        public static ArrayList<Message> generateMessageList(User phoneOwner, User oppositeUser, int count) {
            ArrayList<Message> messages = new ArrayList<>(count);

            long createdMillis = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {

                User sender = RANDOM.nextBoolean() ? oppositeUser : phoneOwner;

                Calendar createdAt = Calendar.getInstance();
                createdAt.setTimeInMillis(createdMillis);

                messages.add(new Message(generateRandomText(), sender, createdAt, State.DELIVERED));

                createdMillis -= MIN_IN_MILLIS * RANDOM.nextInt(5);
            }

            Collections.sort(messages);

            return messages;
        }

        public static String generateRandomText(int length) {
            String result = "";

            for (int i = 0; i < length; i++) {
                result += ALPHABET[RANDOM.nextInt(ALPHABET.length)];
            }

            return result;
        }

        public static String generateRandomText() {
            return generateRandomText(RANDOM.nextInt(38) + 2); // Min 2 characters
        }

    }

    @Override
    public int compareTo(@NonNull Message otherMessage) {
        return (int) (otherMessage.getCreatedAt().getTimeInMillis() - this.mCreatedAt.getTimeInMillis());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mText);
        dest.writeParcelable(mSender, flags);
        dest.writeLong(mCreatedAt.getTimeInMillis());
        dest.writeSerializable(mState);
    }

    @Override
    public String toString() {
        return "Message{" +
                "mText='" + mText + '\'' +
                ", mSender=" + mSender +
                ", mState=" + mState +
                ", mCreatedAt=" + mCreatedAt + '}';
    }
}
