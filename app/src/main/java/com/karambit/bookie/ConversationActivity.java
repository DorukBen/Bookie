package com.karambit.bookie;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.karambit.bookie.adapter.ConversationAdapter;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.service.BookieFirebaseMessagingService;
import com.karambit.bookie.service.BookieIntentFilters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = ConversationActivity.class.getSimpleName();

    public static final int RESULT_MESSAGE_INSERTED = 1;
    public static final int RESULT_ALL_MESSAGES_DELETED = 2;

    public static final String EXTRA_USER = "user";
    public static final String EXTRA_LAST_MESSAGE = "last_message";
    public static final String EXTRA_OPPOSITE_USER = "opposite_user";

    public static Integer currentConversationUserId = -1;

    private User mOppositeUser;
    private DBManager mDbManager;
    private ConversationAdapter mConversationAdapter;
    private ImageButton mSendMessageButton;
    private EditText mMessageEditText;
    private ArrayList<Message> mMessages;
    private RecyclerView mRecyclerView;
    private MenuItem mDeleteMenuItem;
    private BroadcastReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mOppositeUser = getIntent().getExtras().getParcelable(EXTRA_USER);

        SpannableString s = new SpannableString(mOppositeUser.getName());
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        final float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        mDbManager = new DBManager(this);
        mDbManager.open();
        mMessages = mDbManager.getMessageDataSource().getConversation(mOppositeUser, SessionManager.getCurrentUser(this));

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendMessageButton = (ImageButton) findViewById(R.id.messageSendButton);

        final TextView dateLabel = (TextView) findViewById(R.id.dateLabelTextView);

        mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private final int DAY_DURATION_MILLIS = 1000 * 60 * 60 * 24;
            private int mStoredPosition;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int currentPosition = layoutManager.findLastVisibleItemPosition();

                if (currentPosition != mStoredPosition && mMessages.size() > 0) {

                    mStoredPosition = currentPosition;

                    Calendar createdAt = mMessages.get(currentPosition).getCreatedAt();
                    long createdAtMillis = createdAt.getTimeInMillis();

                    Calendar now = Calendar.getInstance();
                    now.set(Calendar.MINUTE, 0);
                    now.set(Calendar.HOUR_OF_DAY, 0);
                    long dayStartMillis = now.getTimeInMillis();

                    String yesterday = getString(R.string.yesterday);

                    if (dayStartMillis < createdAtMillis) {
                        dateLabel.setVisibility(View.GONE);

                    } else if (dayStartMillis - createdAtMillis < DAY_DURATION_MILLIS) {

                        if (dateLabel.getVisibility() != View.VISIBLE || !dateLabel.getText().equals(yesterday)) {
                            dateLabel.setVisibility(View.VISIBLE);
                            dateLabel.setText(yesterday);
                        }
                    } else {

                        if (dateLabel.getVisibility() != View.VISIBLE) {
                            dateLabel.setVisibility(View.VISIBLE);
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", Locale.getDefault());
                        String dateText = sdf.format(createdAt.getTime());

                        dateLabel.setText(dateText);
                    }

                    dateLabel.bringToFront();
                }
            }
        });

        mConversationAdapter = new ConversationAdapter(this, mMessages, mOppositeUser);

        mConversationAdapter.setOnMessageClickListener(new ConversationAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message message, int position) {

                ArrayList<Integer> selectedIndexes = mConversationAdapter.getSelectedIndexes();

                if (!selectedIndexes.isEmpty()) {

                    if (!selectedIndexes.contains(position)) {

                        Log.i(TAG, "Message " + position + " selected");

                        if (selectedIndexes.size() == 0) {
                            setSelectionMode(true);
                        }

                        selectedIndexes.add(position);

                        String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);

                    } else {

                        Log.i(TAG, "Message " + position + " unselected");

                        selectedIndexes.remove((Integer) position);

                        if (selectedIndexes.isEmpty()) {
                            setSelectionMode(false);
                        } else {
                            String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                            setActionBarTitle(selectedTitle);
                        }
                    }
                    mConversationAdapter.notifyItemChanged(position);

                }
            }

            @Override
            public boolean onMessageLongClick(Message message, int position) {

                ArrayList<Integer> selectedIndexes = mConversationAdapter.getSelectedIndexes();

                if (!selectedIndexes.contains(position)) {

                    Log.i(TAG, "Message " + position + " selected");

                    if (selectedIndexes.size() == 0) {
                        setSelectionMode(true);
                    }

                    selectedIndexes.add(position);

                    String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                    setActionBarTitle(selectedTitle);

                } else {

                    Log.i(TAG, "Message " + position + " unselected");

                    selectedIndexes.remove((Integer) position);

                    if (selectedIndexes.isEmpty()) {
                        setSelectionMode(false);
                    } else {
                        String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);
                    }
                }
                mConversationAdapter.notifyItemChanged(position);
                return true;
            }

            @Override
            public void onMessageErrorClick(Message message, int position) {
                sendMessageToServer(message);
            }
        });

        mRecyclerView.setAdapter(mConversationAdapter);

        //For improving recyclerviews performance
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String trimmed = s.toString().trim();

                if (trimmed.length() > 0) {
                    toggleSendButton(true);
                } else {
                    toggleSendButton(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messageText = mMessageEditText.getText().toString();
                messageText = messageText.trim();

                if (!TextUtils.isEmpty(messageText)) {

                    messageText = trimInnerNewLines(messageText);

                    int id = createTemporaryMessageID();
                    final Message message = new Message(id, messageText, SessionManager.getCurrentUser(ConversationActivity.this),
                            mOppositeUser, Calendar.getInstance(), Message.State.PENDING);

                                if (mDbManager.getMessageDataSource().saveMessage(message, SessionManager.getCurrentUser(ConversationActivity.this))){

                                    mMessageEditText.setText("");
                                    toggleSendButton(false);
                                    sendMessageToServer(message);
                                    insertMessage(message);
                                }else {
                                    Log.e(TAG, "Message Insertion Failed!");
                                    Toast.makeText(ConversationActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                                }

                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentConversationUserId = mOppositeUser.getID();

        Iterator<Message> iterator = BookieFirebaseMessagingService.mNotificationMessages.iterator();

        while (iterator.hasNext()){
            Message message = iterator.next();
            if (message.getSender().getID() == currentConversationUserId){
                iterator.remove();
            }
        }


        if (BookieFirebaseMessagingService.mNotificationUserIds.contains(currentConversationUserId)){
            BookieFirebaseMessagingService.mNotificationUserIds.remove(currentConversationUserId);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(getString(R.string.message_notification), BookieFirebaseMessagingService.MESSAGE_NOTIFICATION_ID);
        }

        final User currentUser = SessionManager.getCurrentUser(this);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_RECEIVED)){
                    Message message = intent.getParcelableExtra(BookieIntentFilters.EXTRA_MESSAGE);
                    if (message != null){
                        if(message.getOppositeUser(currentUser).equals(mOppositeUser)){
                            insertMessage(message);

                            fetchSeenMessages();
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_DELIVERED)){
                    final int messageId = intent.getIntExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, -1);
                    if (messageId > 0){

                        changeMessageState(messageId, Message.State.DELIVERED);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_SEEN)){
                    final int messageId = intent.getIntExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, -1);
                    if (messageId > 0){

                        Message changedMessage = changeMessageState(messageId, Message.State.SEEN);
                        if (changedMessage != null){
                            for (final Message message: mMessages){

                                if (message.getSender().equals(currentUser) && message.getState() == Message.State.DELIVERED &&
                                        message.getCreatedAt().getTimeInMillis() <= changedMessage.getCreatedAt().getTimeInMillis()){
                                    changeMessageState(message.getID(), Message.State.SEEN);

                                }
                            }
                        }

                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_DELIVERED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_SEEN));

        fetchSeenMessages();
    }

    private void fetchSeenMessages() {

        User currentUser = SessionManager.getCurrentUser(ConversationActivity.this);

        Iterator<Message> messageIterator = mMessages.iterator();
        while (messageIterator.hasNext()){
            final Message message = messageIterator.next();
            if (message.getReceiver().equals(currentUser) && message.getState() != Message.State.SEEN){

                changeMessageState(message.getID(), Message.State.SEEN);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        currentConversationUserId = -1;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public int createTemporaryMessageID() {
        int minimum = mDbManager.getMessageDataSource().getMinimumMessageId();

        if (minimum >= 0) {
            return -1;
        } else {
            return minimum - 1;
        }
    }

    public void insertMessage(final Message newMessage) {

        Log.i(TAG, "Message inserted: " + newMessage.getText());

        mMessages.add(0, newMessage);
        mConversationAdapter.notifyItemInserted(0);

        User currentUser = SessionManager.getCurrentUser(this);
        if (newMessage.getSender().equals(currentUser)) {
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition <= 1) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }

        if (newMessage.getSender().getID() != currentUser.getID()) {

            changeMessageState(newMessage.getID(), Message.State.SEEN);
        }

        ArrayList<Integer> selectedIndexes = mConversationAdapter.getSelectedIndexes();
        for (int i = 0; i < selectedIndexes.size(); i++) {
            selectedIndexes.set(i, selectedIndexes.get(i) + 1);
        }

        setResult(RESULT_MESSAGE_INSERTED, getIntent().putExtra(EXTRA_LAST_MESSAGE, mMessages.get(0)));
    }

    public Message changeMessageState(int messageID, Message.State state) {
        for (final Message message : mMessages) {
            if (message.getID() == messageID) {
                if (message.getState() != Message.State.SEEN) {
                    message.setState(state);
                    mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                    mDbManager.getMessageDataSource().updateMessageState(message, state);

                    if (message.getSender().getID() != SessionManager.getCurrentUser(getApplicationContext()).getID()){
                        uploadMessageStateToServer(message);
                    }
                    return message;
                } else {
                    if (state == Message.State.SEEN){
                        message.setState(state);
                        mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                        mDbManager.getMessageDataSource().updateMessageState(message, state);

                        if (message.getSender().getID() != SessionManager.getCurrentUser(getApplicationContext()).getID()){
                            uploadMessageStateToServer(message);
                        }
                        return message;
                    }else {
                        return message;
                    }
                }
            }
        }
        return null;
    }

    public boolean changeMessageID(int oldMessageID, int newMessageID) {
        for (Message message : mMessages) {
            if (message.getID() == oldMessageID) {
                message.setID(newMessageID);
                mDbManager.getMessageDataSource().updateMessageId(oldMessageID, newMessageID);
                return true;
            }
        }
        return false;
    }

    private void setSelectionMode(boolean toggle) {
        ActionBar actionBar = getSupportActionBar();

        if (toggle) {
            String selectedTitle = getString(R.string.x_messages_selected, mConversationAdapter.getSelectedIndexes().size());
            setActionBarTitle(selectedTitle);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_messaging_cancel_selection);

            mDeleteMenuItem.setVisible(true);

            Log.i(TAG, "Message selection mode on");
        } else {
            setActionBarTitle(mOppositeUser.getName());

            actionBar.setDisplayHomeAsUpEnabled(false);

            mDeleteMenuItem.setVisible(false);

            Log.i(TAG, "Message selection mode off");
        }
    }

    private void setActionBarTitle(String selectedTitle) {
        ActionBar actionBar = getSupportActionBar();

        SpannableString title = new SpannableString(selectedTitle);
        title.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, title.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
        title.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        actionBar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_menu, menu);

        mDeleteMenuItem = menu.findItem(R.id.action_delete_messages);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_delete_messages:

                int selectedCount = mConversationAdapter.getSelectedIndexes().size();

                String prompt;
                if (selectedCount > 1) {
                    prompt = getString(R.string.delete_prompt_conversation_message_multiple, selectedCount, mOppositeUser.getName());
                } else {
                    prompt = getString(R.string.delete_prompt_conversation_message_single, mOppositeUser.getName());
                }

                new AlertDialog.Builder(this)
                    .setMessage(prompt)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ArrayList<Integer> selectedIndexes = mConversationAdapter.getSelectedIndexes();

                            Collections.sort(selectedIndexes);
                            Collections.reverse(selectedIndexes);

                            int[] selectedMessageIds = new int[selectedIndexes.size()];

                            for (int i = 0; i < selectedIndexes.size(); i++) {
                                int index = selectedIndexes.get(i);
                                Message message = mMessages.get(index);
                                selectedMessageIds[i] = message.getID();
                                mMessages.remove(index);
                                mDbManager.getMessageDataSource().deleteMessage(message);
                            }

                            deleteMessagesOnServer(selectedMessageIds);

                            Log.i(TAG, "Deleted message indexes: " + selectedIndexes.toString());
                            Log.i(TAG, "Deleted message IDs: " + Arrays.toString(selectedMessageIds));

                            selectedIndexes.clear();
                            setSelectionMode(false);
                            mConversationAdapter.notifyDataSetChanged();

                            if (!mMessages.isEmpty()) {
                                setResult(RESULT_MESSAGE_INSERTED, getIntent().putExtra(EXTRA_LAST_MESSAGE, mMessages.get(0)));
                            } else {
                                setResult(RESULT_ALL_MESSAGES_DELETED, getIntent().putExtra(EXTRA_OPPOSITE_USER, mOppositeUser));
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();

                break;

            case android.R.id.home:
                setSelectionMode(false);
                mConversationAdapter.getSelectedIndexes().clear();
                mConversationAdapter.notifyDataSetChanged();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleSendButton(boolean active) {
        if (active) {
            mSendMessageButton.animate()
                              .alpha(1f)
                              .setDuration(300)
                              .start();

            mSendMessageButton.setClickable(true);
        } else {
            mSendMessageButton.animate()
                              .alpha(0.5f)
                              .setDuration(300)
                              .start();

            mSendMessageButton.setClickable(false);
        }
    }

    private void deleteMessagesOnServer(int[] selectedMessageIds) {

        // TODO Server delete notify (selectedMessageIds[])
    }

    /**
     * @param messageText Input
     * @return Modified Input
     * <p>
     * This method removes unnecessary new lines in string. For example:
     * <p>
     * "1
     * <p>
     * <p>
     * <p>
     * 2
     * 34
     * <p>
     * <p>
     * <p>
     * <p>
     * 5
     * 6
     * <p>
     * <p>
     * <p>
     * <p>
     * 7"
     * <p>
     * RETURNS:
     * <p>
     * "1
     * <p>
     * 2
     * 34
     * 5
     * 6
     * <p>
     * 7"
     */
    private String trimInnerNewLines(String messageText) {

        StringBuilder stringBuilder = new StringBuilder(messageText);

        for (int i = 0; i < stringBuilder.length() - 2; i++) {
            char currentChar = stringBuilder.charAt(i);
            char nextChar = stringBuilder.charAt(i + 1);
            char twoAfterChar = stringBuilder.charAt(i + 2);

            if (currentChar == '\n' && nextChar == '\n' && twoAfterChar == '\n') {
                stringBuilder.deleteCharAt(i);
                i--;
            }
        }

        return stringBuilder.toString();
    }

    private void sendMessageToServer(final Message message) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        Call<ResponseBody> sendMessage = fcmApi.sendMessage(email, password, message.getText(), message.getReceiver().getID(), message.getID());

        sendMessage.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            final JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("oldMessageID") && !responseObject.isNull("newMessageID")){

                                    try {
                                        changeMessageID(responseObject.getInt("oldMessageID"), responseObject.getInt("newMessageID"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        changeMessageState(responseObject.getInt("newMessageID"), Message.State.SENT);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Send Message Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Send Message Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Send Message Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Send Message Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }

                                changeMessageState(message.getID(), Message.State.ERROR);
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Send Message Error)");
                            changeMessageState(message.getID(), Message.State.ERROR);
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Send Message Error)");
                        changeMessageState(message.getID(), Message.State.ERROR);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    changeMessageState(message.getID(), Message.State.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Send Message onFailure: " + t.getMessage());
                changeMessageState(message.getID(), Message.State.ERROR);
            }
        });
    }

    private void uploadMessageStateToServer(final Message message) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> uploadMessageState = fcmApi.uploadMessageState(email, password, message.getID(), message.getState().ordinal());

        uploadMessageState.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Upload Message State Error)");
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Upload Message State Error)");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Upload Message State onFailure: " + t.getMessage());
                uploadMessageStateToServer(message);
            }
        });
    }
}
