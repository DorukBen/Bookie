package com.karambit.bookie.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Message model
 * <p>
 * Created by orcan on 10/25/16.
 */

public class Message implements Parcelable, Comparable<Message> {

    public enum State {PENDING, SENT, DELIVERED, SEEN, ERROR}

    private int mID;
    private String mText;
    private User mSender;
    private User mReceiver;
    private Calendar mCreatedAt;
    private State mState;

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

    public User getOppositeUser(User currentUser) {
        if (currentUser.equals(mReceiver)) {
            return mSender;
        } else if (currentUser.equals(mSender)){
            return mReceiver;
        } else {
            throw new IllegalArgumentException("Invalid current user: " + currentUser);
        }
    }

    public static Message jsonObjectToMessage(JSONObject messageObject) {
        try {
            if (messageObject != null){
                Timestamp timestamp = Timestamp.valueOf(messageObject.isNull("createdAt")|| TextUtils.isEmpty(messageObject.getString("createdAt"))? null: messageObject.getString("createdAt"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(timestamp);
                return new Message(messageObject.isNull("messageID")? -1: messageObject.getInt("messageID"),
                        messageObject.isNull("messageText")|| TextUtils.isEmpty(messageObject.getString("messageText"))? null: messageObject.getString("messageText"),
                        messageObject.isNull("fromUser")? null: User.jsonObjectToUser(messageObject.getJSONObject("fromUser")),
                        messageObject.isNull("toUser")? null: User.jsonObjectToUser(messageObject.getJSONObject("toUser")),
                        calendar,
                        State.values()[messageObject.isNull("messageState")? 2: messageObject.getInt("messageState")]);
            }else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Message> jsonObjectToMessageList(JSONObject jsonObject){
        ArrayList<Message> messages = new ArrayList<>();
        try {
            if (!jsonObject.isNull("Messages")){
                JSONArray jsonArray = jsonObject.getJSONArray("Messages");
                for (int i = 0; i < jsonArray.length(); i++){
                    messages.add(jsonObjectToMessage(jsonArray.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messages;

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
                    otherMessage.mSender.equals(this.mSender) &&
                    otherMessage.mReceiver.equals(this.mReceiver) &&
                    otherMessage.mText.equals(this.mText) &&
                    otherMessage.mCreatedAt.getTimeInMillis() == this.mCreatedAt.getTimeInMillis();
            } else {
                return otherMessage.mID == this.mID;
            }
        }
    }

    @Override
    public String toString() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault());
        String createdAt = dateFormat.format(mCreatedAt.getTime());

        return "\nMessage{" +
            "\n\tmText='" + mText + "\'," +
            "\n\tmSender=" + mSender.getName() + "," +
            "\n\tmReceiver=" + mReceiver + "," +
            "\n\tmState=" + mState + "," +
            "\n\tmCreatedAt=" + createdAt +
            "\n}";
    }
}
