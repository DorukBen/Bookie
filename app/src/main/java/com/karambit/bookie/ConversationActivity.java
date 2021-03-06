package com.karambit.bookie;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
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
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.rest_api.UserApi;
import com.karambit.bookie.service.BookieFirebaseMessagingService;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

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
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
            float elevation = getResources().getDimension(R.dimen.actionbar_max_elevation);
            getSupportActionBar().setElevation(elevation);

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.toolbarTitle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //If not admin or support user
                    if(mOppositeUser.getID() >= 0){
                        Intent intent = new Intent(ConversationActivity.this, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.EXTRA_USER, mOppositeUser);
                        startActivity(intent);
                    }
                }
            });

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConversationAdapter.getSelectedIndexes().size() > 0) {
                        setSelectionMode(false);
                        mConversationAdapter.getSelectedIndexes().clear();
                        mConversationAdapter.notifyDataSetChanged();
                    } else {
                        finish();
                    }
                }
            });
        }

        mDbManager = new DBManager(this);
        mDbManager.open();
        mMessages = mDbManager.getMessageDataSource().getConversation(mOppositeUser, SessionManager.getCurrentUser(this));

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendMessageButton = (ImageButton) findViewById(R.id.messageSendButton);

        final TextView dateLabel = (TextView) findViewById(R.id.dateLabelTextView);

        mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);

        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

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

                        Logger.d("Message " + position + " selected");

                        if (selectedIndexes.size() == 0) {
                            setSelectionMode(true);
                        }

                        selectedIndexes.add(position);

                        String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);

                    } else {

                        Logger.d("Message " + position + " unselected");

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

                    Logger.d("Message " + position + " selected");

                    if (selectedIndexes.size() == 0) {
                        setSelectionMode(true);
                    }

                    selectedIndexes.add(position);

                    String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                    setActionBarTitle(selectedTitle);

                } else {

                    Logger.d("Message " + position + " unselected");

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

            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(ConversationActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER, user);
                startActivity(intent);
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

                    if (mDbManager.getMessageDataSource().saveMessage(message, message.getOppositeUser(SessionManager.getCurrentUser(ConversationActivity.this)))) {

                        mMessageEditText.setText("");
                        toggleSendButton(false);
                        sendMessageToServer(message);
                        insertMessage(message);
                    } else {
                        Logger.e("Message Insertion Failed: " + message);
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
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_RECEIVED)){
                    Message message = intent.getParcelableExtra(BookieIntentFilters.EXTRA_MESSAGE);
                    if (message != null){
                        if(message.getOppositeUser(currentUser).equals(mOppositeUser)){
                            insertMessage(message);

                            fetchSeenMessages();

                            Logger.d("Message received, received from FCM: " + message);
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_DELIVERED)){
                    final int messageId = intent.getIntExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, -1);
                    if (messageId > 0){
                        Message message = changeMessageState(messageId, Message.State.DELIVERED);

                        Logger.d("Message delivered received from FCM: " + message);
                    }
                } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_SEEN)){
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

                        Logger.d("Message seen received from FCM: " + changedMessage);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_DELIVERED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_SEEN));

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

        Logger.d("Message inserted: " + newMessage.getText());

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
                    mDbManager.Threaded(mDbManager.getMessageDataSource().cUpdateMessageState(message, state));

                    if (message.getSender().getID() != SessionManager.getCurrentUser(getApplicationContext()).getID()){
                        uploadMessageStateToServer(message);
                    }
                    return message;
                } else {
                    if (state == Message.State.SEEN){
                        message.setState(state);
                        mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                        mDbManager.Threaded(mDbManager.getMessageDataSource().cUpdateMessageState(message, state));

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
                mDbManager.Threaded(mDbManager.getMessageDataSource().cUpdateMessageId(oldMessageID, newMessageID));
                return true;
            }
        }
        return false;
    }

    private void setSelectionMode(boolean toggle) {

        if (toggle) {
            String selectedTitle = getString(R.string.x_messages_selected, mConversationAdapter.getSelectedIndexes().size());
            setActionBarTitle(selectedTitle);

            mDeleteMenuItem.setVisible(true);

            Logger.d("Message selection mode on");
        } else {
            setActionBarTitle(mOppositeUser.getName());

            mDeleteMenuItem.setVisible(false);

            Logger.d("Message selection mode off");
        }
    }

    private void setActionBarTitle(String selectedTitle) {

        SpannableString title = new SpannableString(selectedTitle);
        title.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, title.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_title_size);
        title.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        ((TextView) findViewById(R.id.toolbar).findViewById(R.id.toolbarTitle)).setText(title);
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
                                mDbManager.Threaded(mDbManager.getMessageDataSource().cDeleteMessage(message));
                            }

                            deleteMessagesOnServer(selectedMessageIds);

                            Logger.d("Deleted message indexes: " + selectedIndexes.toString());
                            Logger.d("Deleted message IDs: " + Arrays.toString(selectedMessageIds));

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

        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();

        String deletedMessageIds;

        int i = 0;

        StringBuilder builder = new StringBuilder();
        for (Integer genreCode: selectedMessageIds){
            builder.append(genreCode);
            if (i < selectedMessageIds.length - 1){
                builder.append("_");
            }
            i++;
        }
        deletedMessageIds = builder.toString();

        Call<ResponseBody> deleteMessages = userApi.deleteMessages(email, password, deletedMessageIds);

        Logger.d("deleteMessages() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tmessageIDs=" + deletedMessageIds);

        deleteMessages.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Logger.d("Messages deleted from server");
                            } else {

                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);
                            }
                        }else{
                            Logger.e("Response body is null. (Conversation Error)");
                        }
                    }else{
                        Logger.e("Response object is null. (Conversation Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Logger.e("deleteMessages Failure: " + t.getMessage());
            }
        });
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

        Logger.d("sendMessage() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tmessage=" + message.getText() +
                     ", \n\ttoUserID=" + message.getReceiver().getID() + ", \n\toldMessageID=" + message.getID());

        sendMessage.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            final JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("oldMessageID") && !responseObject.isNull("newMessageID")){

                                    try {
                                        changeMessageID(responseObject.getInt("oldMessageID"), responseObject.getInt("newMessageID"));
                                        Logger.d("Message sent to server succesfully");
                                    } catch (JSONException e) {
                                        Logger.e("JSONException caught: " + e.getMessage());
                                    }

                                    try {
                                        changeMessageState(responseObject.getInt("newMessageID"), Message.State.SENT);
                                    } catch (JSONException e) {
                                        Logger.e("JSONException caught: " + e.getMessage());
                                    }

                                }

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);

                                changeMessageState(message.getID(), Message.State.ERROR);
                            }
                        }else{
                            Logger.e("Response body is null. (Send Message Error)");
                            changeMessageState(message.getID(), Message.State.ERROR);
                        }
                    }else {
                        Logger.e("Response object is null. (Send Message Error)");
                        changeMessageState(message.getID(), Message.State.ERROR);
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                    changeMessageState(message.getID(), Message.State.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("Send Message onFailure: " + t.getMessage());
                changeMessageState(message.getID(), Message.State.ERROR);
            }
        });
    }

    private void uploadMessageStateToServer(final Message message) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> uploadMessageState = fcmApi.uploadMessageState(email, password, message.getID(), message.getState().getStateCode());

        Logger.d("uploadMessageState() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tmessageID=" + message.getID() + ", \n\tmessageState=" + message.getState().getStateCode());

        uploadMessageState.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Logger.d("Message state uploaded to server: " + message.getState());
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);
                            }
                        }else{
                            Logger.e("Response body is null. (Upload Message State Error)");
                        }
                    }else {
                        Logger.e("Response object is null. (Upload Message State Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("uploadMessageState Failure: " + t.getMessage());
                uploadMessageStateToServer(message);
            }
        });
    }
}
