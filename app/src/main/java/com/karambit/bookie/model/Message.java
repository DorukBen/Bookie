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
 * <p>
 * Created by orcan on 10/25/16.
 */

public class Message implements Parcelable, Comparable<Message> {

    public enum State {PENDING, SENT, DELIVERED, SEEN, ERROR, NONE}

    private int mID;
    private String mText;
    private User mSender;
    private User mReceiver;
    private Calendar mCreatedAt;
    private State mState;

    public Message(String text, User sender, User receiver, Calendar createdAt, State state) {
        mID = -1;
        mText = text;
        mSender = sender;
        mReceiver = receiver;
        mCreatedAt = createdAt;
        mState = state;
    }

    public Message(Integer id, String text, User sender, User receiver, Calendar createdAt, State state) {
        mID = id;
        mText = text;
        mSender = sender;
        mReceiver = receiver;
        mCreatedAt = createdAt;
        mState = state;
    }

    protected Message(Parcel in) {
        mID = in.readInt();
        mText = in.readString();
        mSender = in.readParcelable(User.class.getClassLoader());
        mReceiver = in.readParcelable(User.class.getClassLoader());

        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(in.readLong());

        mState = (State) in.readSerializable();
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
        dest.writeInt(mID);
        dest.writeString(mText);
        dest.writeParcelable(mSender, flags);
        dest.writeParcelable(mReceiver, flags);
        dest.writeLong(mCreatedAt.getTimeInMillis());
        dest.writeSerializable(mState);
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
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

    public User getReceiver() {
        return mReceiver;
    }

    public void setReceiver(User receiver) {
        mReceiver = receiver;
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
                User receiver;
                if (sender != phoneOwner) {
                    receiver = phoneOwner;
                } else {
                    receiver = oppositeUser;
                }

                Calendar createdAt = Calendar.getInstance();
                createdAt.setTimeInMillis(createdMillis);

                messages.add(new Message(generateRandomText(), sender, receiver, createdAt, State.DELIVERED));

                createdMillis -= MIN_IN_MILLIS * RANDOM.nextInt(5);
            }

            Collections.sort(messages);

            return messages;
        }

        public static ArrayList<Message> generateMessageList(User phoneOwner, int count) {
            ArrayList<Message> messages = new ArrayList<>(count);

            long createdMillis = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {

                User sender = RANDOM.nextBoolean() ? User.GENERATOR.generateUser() : phoneOwner;
                User receiver;
                if (sender != phoneOwner) {
                    receiver = phoneOwner;
                } else {
                    receiver = User.GENERATOR.generateUser();
                }

                Calendar createdAt = Calendar.getInstance();
                createdAt.setTimeInMillis(createdMillis);

                messages.add(new Message(generateRandomText(), sender, receiver, createdAt, State.SEEN));

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

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Message)) {
            return false;
        } else {

            Message otherMessage = (Message) obj;

            if (otherMessage.mID < 0 || this.mID < 0) {
                return
                    otherMessage.mSender.getID() == this.mSender.getID() &&
                    otherMessage.mReceiver.getID() == this.mReceiver.getID() &&
                    otherMessage.mText == this.mText &&
                    otherMessage.mCreatedAt.getTimeInMillis() == this.mCreatedAt.getTimeInMillis();
            } else {
                return otherMessage.mID == this.mID;
            }
        }
    }

    @Override
    public String toString() {
        return "Message{" +
            "mText='" + mText + '\'' +
            ", mSender=" + mSender +
            ", mReceiver=" + mReceiver +
            ", mState=" + mState +
            ", mCreatedAt=" + mCreatedAt + '}';
    }
}
