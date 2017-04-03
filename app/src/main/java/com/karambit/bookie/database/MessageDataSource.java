package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * Created by doruk on 19.03.2017.
 */

public class MessageDataSource {

    private static final String TAG = MessageDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;
    private MessageUserDataSource mMessageUserDataSource;

    private static final String MESSAGE_TABLE_NAME = "message";
    private static final String MESSAGE_COLUMN_ID = "message_id";
    private static final String MESSAGE_COLUMN_TEXT = "text";
    private static final String MESSAGE_COLUMN_FROM_USER_ID = "from_user_id";
    private static final String MESSAGE_COLUMN_TO_USER_ID = "to_user_id";
    private static final String MESSAGE_COLUMN_IS_DELETED = "is_deleted";
    private static final String MESSAGE_COLUMN_STATE = "state";
    private static final String MESSAGE_COLUMN_CREATED_AT = "created_at";

    static final String CREATE_MESSAGE_TABLE_TAG = "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" +
            MESSAGE_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            MESSAGE_COLUMN_TEXT + " TEXT NOT NULL, " +
            MESSAGE_COLUMN_FROM_USER_ID + " INTEGER NOT NULL, " +
            MESSAGE_COLUMN_TO_USER_ID + " INTEGER NOT NULL, " +
            MESSAGE_COLUMN_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, " +
            MESSAGE_COLUMN_STATE + " INTEGER NOT NULL, " +
            MESSAGE_COLUMN_CREATED_AT + " LONG NOT NULL)";

    static final String UPGRADE_MESSAGE_TABLE_TAG = "DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME;

    MessageDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
        mMessageUserDataSource = new MessageUserDataSource(mSqLiteDatabase);
    }

    /**
     * Saves massage to database if message user not exist inserts message user too.
     *
     * @param message New {@link Message message}<br>
     * @param user {@link User User}
     *
     * @return boolean value if insertion successful returns true else returns false.
     */
    public boolean saveMessage(Message message, User user) {
        mMessageUserDataSource.saveUser(user);

        return !isMessageExists(message) && insertMessage(message);
    }

    /**
     * Checks database for given message id's existence.<br>
     *
     * @param message {@link Message message}
     *
     * @return  boolean value. If message {@link Message message} exist returns true else returns false.
     */
    private boolean isMessageExists(Message message) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME + " WHERE " + MESSAGE_COLUMN_ID  + " = " + message.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Insert message to database.<br>
     *
     * @param message New {@link Message message}<br>
     *
     * @return boolean value. If insertion completed successfully returns true else false.
     */
    private boolean insertMessage(Message message) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_COLUMN_ID, message.getID());
            contentValues.put(MESSAGE_COLUMN_TEXT, message.getText());
            contentValues.put(MESSAGE_COLUMN_FROM_USER_ID, message.getSender().getID());
            contentValues.put(MESSAGE_COLUMN_TO_USER_ID, message.getReceiver().getID());
            contentValues.put(MESSAGE_COLUMN_IS_DELETED, 0);
            contentValues.put(MESSAGE_COLUMN_CREATED_AT, message.getCreatedAt().getTimeInMillis());
            contentValues.put(MESSAGE_COLUMN_STATE, message.getState().getStateCode());

            result = mSqLiteDatabase.insert(MESSAGE_TABLE_NAME, null, contentValues) < 0;
        }finally {
            Logger.d("New message insertion successful");
        }

        return result;
    }

    /**
     * Updates {@link com.karambit.bookie.model.Message.State}.<br>
     *
     * @param messageId {@link Message} id
     * @param state {@link com.karambit.bookie.model.Message.State}
     */
    public boolean updateMessageState(int messageId, Message.State state){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_STATE, state.getStateCode());

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + messageId, null) > 0;
        }finally {
            Logger.d("Message state updated to " + state);
        }

        return result;
    }

    /**
     * Updates {@link com.karambit.bookie.model.Message.State}.<br>
     *
     * @param message {@link Message}
     * @param state {@link com.karambit.bookie.model.Message.State}
     */
    public boolean updateMessageState(Message message, Message.State state){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_STATE, state.getStateCode());

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + message.getID(), null) > 0;
        }finally {
            Logger.d("Message state updated to " + state);
        }

        return result;
    }

    /**
     * Updates {@link Message} id.<br>
     *
     * @param oldMessageId Old {@link Message} id
     * @param newMessageId New {@link Message} id
     */
    public boolean updateMessageId(int oldMessageId, int newMessageId){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_ID, newMessageId);

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + oldMessageId, null) > 0;
        }finally {
            Logger.d("Message id updated from " + oldMessageId + " to " + newMessageId);
        }

        return result;
    }

    /**
     * Gets user's all messages from database which current user have conversation.<br>
     *
     * @param anotherUser Another {@link User user} which have conversation with current {@link User user}<br>
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link ArrayList Arraylist}<{@link Message Message}> all conversation messages
     */
    public ArrayList<Message> getConversation(User anotherUser, User currentUser) {
        Cursor res = null;
        ArrayList<Message> messages = new ArrayList<>();

        try {
            String deletedString = "1";
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME +
                    " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + anotherUser.getID() + " OR " + MESSAGE_COLUMN_TO_USER_ID +
                    " = " + anotherUser.getID() + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + deletedString + " ORDER BY " + MESSAGE_COLUMN_CREATED_AT + " DESC", null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    Calendar calendar = Calendar.getInstance();
                    long time = res.getLong(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT)); //replace 4 with the column index
                    calendar.setTimeInMillis(time);

                    Message message;
                    if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == currentUser.getID()) {
                        message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                                res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                                currentUser,
                                anotherUser,
                                calendar,
                                Message.State.valueOf(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))));
                    } else {
                        message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                                res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                                anotherUser,
                                currentUser,
                                calendar,
                                Message.State.valueOf(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))));
                    }
                    messages.add(message);

                } while (res.moveToNext());
            }
        } finally {
            if (res != null){
                res.close();
            }
        }

        return messages;
    }

    /**
     * Deletes {@link User user's} all conversation messages from database.<br>
     *
     * @param otherUserID Message {@link User user} id
     */
    public boolean deleteConversation(Integer otherUserID){
        boolean result = false;
        try{
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_FROM_USER_ID + "=" + otherUserID + " OR " +
                    MESSAGE_COLUMN_TO_USER_ID + " = " + otherUserID, null) > 0;
        }finally {
            Logger.d("Users conversation messages deleted from database");
        }

        return result;
    }

    /**
     * Deletes {@link User user's} all conversation messages and {@link User user} itself from database.<br>
     *
     * @param otherUser Message {@link User user}
     */
    public boolean deleteConversation(User otherUser){
        boolean result = false;
        try{
            mMessageUserDataSource.deleteUser(otherUser);

            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_FROM_USER_ID + "=" + otherUser.getID() + " OR " +
                    MESSAGE_COLUMN_TO_USER_ID + " = " + otherUser.getID(), null) > 0;
        }finally {
            Logger.d("Users conversation messages deleted from database");
        }
        return  result;
    }

    /**
     * Gets last message between another {@link User user} and current {@link User user}.<br>
     *
     * @param anotherUser Another {@link User user} which have conversation with current {@link User user}<br>
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link Message Message} Last {@link Message message} between given users.
     */
    private Message getLastMessage (User anotherUser, User currentUser) {
        Cursor res = null;
        Message message;
        try {
            String deletedString = "1";
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME +
                    " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + anotherUser.getID() + " OR " + MESSAGE_COLUMN_TO_USER_ID +
                    " = " + anotherUser.getID() + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + deletedString + " ORDER BY " + MESSAGE_COLUMN_CREATED_AT + " DESC "
                    + "LIMIT 1", null);
            res.moveToFirst();

            if (res.getCount() >  0){
                Calendar calendar = Calendar.getInstance();
                long time = res.getLong(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT)); //replace 4 with the column index
                calendar.setTimeInMillis(time);


                if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == currentUser.getID()){
                    message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                            res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                            currentUser,
                            anotherUser,
                            calendar,
                            Message.State.valueOf(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))));
                }else{
                    message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                            res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                            anotherUser,
                            currentUser,
                            calendar,
                            Message.State.valueOf(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))));
                }
            }else {
                message = null;
            }

        }finally {
            if (res != null) {
                res.close();
            }
        }
        return message;
    }

    /**
     * Gives last messages in all conversations current user have.<br>
     *
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link ArrayList Arraylist}<{@link Message Message}> last messages in all conversations
     */
    public ArrayList<Message> getLastMessages(User currentUser) {

        ArrayList<User> users = mMessageUserDataSource.getAllUsers();

        ArrayList<Message> messages = new ArrayList<>();
        for (User user : users){
            messages.add(getLastMessage(user,currentUser));
        }
        return messages;
    }

    /**
     * Checks database and finds minimum id from message table.<br>
     *
     * @return int value. Lowest {@link Message message} id
     */
    public int getMinimumMessageId(){
        Cursor res = null;
        int lastMessageId = 0;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT " + MESSAGE_COLUMN_ID + " FROM " + MESSAGE_TABLE_NAME +
                    " ORDER BY " + MESSAGE_COLUMN_ID + " ASC " +
                    " LIMIT 1", null);
            res.moveToFirst();

            if (res.getCount() > 0){
                lastMessageId = res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID));
            }else {
                lastMessageId = -1;
            }

        } finally {
            if (res != null){
                res.close();
            }
        }
        return lastMessageId;
    }

    /**
     * Counts unseen messages for given {@link User user}.<br>
     *
     * @param oppositeUser {@link User} which have conversation with phone user
     * @return Total unseen message count for given user
     */
    public int getUnseenMessageCount(User oppositeUser){
        Cursor res = null;
        String queryVariableString = "totalCount";
        int unseenMessageCount = 0;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT COUNT(*) AS " + queryVariableString + " FROM " + MESSAGE_TABLE_NAME +
                    " WHERE " + MESSAGE_COLUMN_FROM_USER_ID + " = " + oppositeUser.getID() +
                    " AND " + MESSAGE_COLUMN_STATE + " = " + Message.State.DELIVERED.getStateCode(), null);
            res.moveToFirst();

            unseenMessageCount = res.getInt(res.getColumnIndex(queryVariableString));

        } finally {
            if (res != null){
                res.close();
            }
        }
        return unseenMessageCount;
    }

    /**
     * Counts all unseen messages.<br>
     *
     * @param currentUser Current {@link User user}
     * @return Total unseen message count
     */
    public int getTotalUnseenMessageCount(User currentUser){
        Cursor res = null;
        String queryVariableString = "totalCount";
        int totalUnseenMessageCount = 0;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT COUNT(*) AS " + queryVariableString + " FROM " + MESSAGE_TABLE_NAME +
                    " WHERE " + MESSAGE_COLUMN_FROM_USER_ID + " <> " + currentUser.getID() +
                    " AND " + MESSAGE_COLUMN_STATE + " = " + Message.State.DELIVERED.getStateCode(), null);
            res.moveToFirst();

            totalUnseenMessageCount = res.getInt(res.getColumnIndex(queryVariableString));

        } finally {
            if (res != null){
                res.close();
            }
        }
        return totalUnseenMessageCount;
    }

    /**
     * Deletes message from database.<br>
     *
     * @param messageID {@link Message Message} id<br>
     */
    public boolean deleteMessage(Integer messageID) {
        boolean result = false;
        try{
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + messageID, null) > 0;
        }finally {
            Logger.d("Message deleted from database");
        }

        return result;
    }

    /**
     * Deletes message from database.<br>
     *
     * @param message {@link Message Message}<br>
     */
    public boolean deleteMessage(Message message) {
        boolean result = false;
        try{
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            result = mSqLiteDatabase.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + message.getID(), null) > 0;
        }finally {
            Logger.d("Message deleted from database");
        }

        return result;
    }

    /**
     * Deletes all messages and message users from database.<br>
     */
    public boolean deleteAllMessages() {
        boolean result = false;
        try{
            mMessageUserDataSource.deleteAllUsers();
            result = mSqLiteDatabase.delete(MESSAGE_TABLE_NAME, null, null) > 0;

        } finally {
            Logger.d("All Message Users and Messages deleted from database");
        }

        return result;
    }

    //Callable Methods
    public Callable<Boolean> cSaveMessage(final Message message, final User user){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return saveMessage(message, user);
            }
        };
    }

    public Callable<Boolean> cUpdateMessageState(final int messageId, final Message.State state){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateMessageState(messageId, state);
            }
        };
    }

    public Callable<Boolean> cUpdateMessageState(final Message message, final Message.State state){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateMessageState(message, state);
            }
        };
    }

    public Callable<Boolean> cUpdateMessageId(final int oldMessageId, final int newMessageId){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateMessageId(oldMessageId, newMessageId);
            }
        };
    }

    public Callable<Boolean> cDeleteConversation(final Integer otherUserID){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteConversation(otherUserID);
            }
        };
    }

    public Callable<Boolean> cDeleteConversation(final User otherUser){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteConversation(otherUser);
            }
        };
    }

    public Callable<Boolean> cDeleteMessage(final Integer messageID){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteMessage(messageID);
            }
        };
    }

    public Callable<Boolean> cDeleteMessage(final Message message){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteMessage(message);
            }
        };
    }

    public Callable<Boolean> cDeleteAllMessages(){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteAllMessages();
            }
        };
    }
}
