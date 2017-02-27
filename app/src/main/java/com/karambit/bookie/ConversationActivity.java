package com.karambit.bookie;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.service.MyFirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.karambit.bookie.MainActivity.convertDpToPixel;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = ConversationActivity.class.getSimpleName();

    public static final int MESSAGE_INSERTED = 1;
    public static final int ALL_MESSAGES_DELETED = 2;

    public static Integer currentConversationUserId = -1;

    private User mOppositeUser;
    private DBHandler mDbHandler;
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

        mOppositeUser = getIntent().getExtras().getParcelable("user");

        SpannableString s = new SpannableString(mOppositeUser.getName());
        s.setSpan(new TypefaceSpan(this, "comfortaa.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan((int) convertDpToPixel(18, this)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        final User currentUser = SessionManager.getCurrentUser(this);
        mDbHandler = DBHandler.getInstance(this);
        mMessages = mDbHandler.getConversationMessages(mOppositeUser, currentUser);

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

                        if (selectedIndexes.size() == 0) {
                            setSelectionMode(true);
                        }

                        selectedIndexes.add(position);

                        String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);

                    } else {

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

                    if (selectedIndexes.size() == 0) {
                        setSelectionMode(true);
                    }

                    selectedIndexes.add(position);

                    String selectedTitle = getString(R.string.x_messages_selected, selectedIndexes.size());
                    setActionBarTitle(selectedTitle);

                } else {
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

        // Seen for opposite user messages
        int unSeenMessageCount = mDbHandler.getUnseenMessageCount(mOppositeUser);
        if (unSeenMessageCount > 0){
            for (int i = mMessages.size()-1; i >= 0; i--){
                mMessages.get(i).setState(Message.State.SEEN);
                mConversationAdapter.notifyItemChanged(mMessages.indexOf(mMessages.get(i)));
                mDbHandler.updateMessageState(mMessages.get(i).getID(), Message.State.SEEN);
                uploadMessageStateToServer(mMessages.get(i));
            }

        }

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
                    Message message = new Message(id, messageText, currentUser,
                                                  mOppositeUser, Calendar.getInstance(), Message.State.PENDING);

                    if (mDbHandler.saveMessageToDataBase(message)){
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

        for (Message message: MyFirebaseMessagingService.mNotificationMessages){
            if (message.getSender().getID() == currentConversationUserId){
                MyFirebaseMessagingService.mNotificationMessages.remove(message);
            }
        }

        if (MyFirebaseMessagingService.mNotificationUserIds.contains(currentConversationUserId)){
            MyFirebaseMessagingService.mNotificationUserIds.remove(currentConversationUserId);
        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MyFirebaseMessagingService.MESSAGE_NOTIFICATION_ID);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.MESSAGE_RECEIVED")){
                    if (intent.getParcelableExtra("message") != null){
                        Message message = intent.getParcelableExtra("message");
                        if(message.getOppositeUser(SessionManager.getCurrentUser(getApplicationContext())).getID() == mOppositeUser.getID()){
                            insertMessage(message);
                            message.setState(Message.State.SEEN);
                            changeMessageState(message.getID(), Message.State.SEEN);

                            uploadMessageStateToServer(message);
                        }
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.MESSAGE_DELIVERED")){
                    if (intent.getIntExtra("message_id",-1) > 0){
                        changeMessageState(intent.getIntExtra("message_id",-1), Message.State.DELIVERED);
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.karambit.bookie.MESSAGE_SEEN")){
                    if (intent.getIntExtra("message_id",-1) > 0){
                        Message changedMessage = changeMessageState(intent.getIntExtra("message_id",-1), Message.State.SEEN);
                        if (changedMessage != null){
                            for (Message message: mMessages){
                                if (message.getSender().getID() == SessionManager.getCurrentUser(getApplicationContext()).getID() && message.getCreatedAt().getTimeInMillis() <= changedMessage.getCreatedAt().getTimeInMillis()){
                                    changeMessageState(message.getID(), Message.State.SEEN);
                                }
                            }
                        }
                    }
                }
            }
        };

        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.MESSAGE_RECEIVED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.MESSAGE_DELIVERED"));
        registerReceiver(mMessageReceiver, new IntentFilter("com.karambit.bookie.MESSAGE_SEEN"));

        for (Message message: mMessages){
            if (message.getReceiver().getID() == SessionManager.getCurrentUser(getApplicationContext()).getID() && message.getState() != Message.State.SEEN){
                message.setState(Message.State.SEEN);
                changeMessageState(message.getID(), Message.State.SEEN);
                uploadMessageStateToServer(message);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        currentConversationUserId = -1;
        unregisterReceiver(mMessageReceiver);
    }

    public int createTemporaryMessageID() {
        int minimum = mDbHandler.getMinimumMessageId();

        if (minimum >= 0) {
            return -1;
        } else {
            return minimum - 1;
        }
    }

    public void insertMessage(Message newMessage) {
        mMessages.add(0, newMessage);
        mConversationAdapter.notifyItemInserted(0);

        User currentUser = SessionManager.getCurrentUser(this);
        if (newMessage.getSender().getID() == currentUser.getID()) {
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition <= 1) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }

        if (newMessage.getSender().getID() != currentUser.getID()) {
            for (Message message : mMessages) {
                if (message.getState() != Message.State.SEEN) {
                    message.setState(Message.State.SEEN);
                    mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                }
            }
        }

        setResult(MESSAGE_INSERTED, getIntent().putExtra("last_message", mMessages.get(0)));
    }

    public Message changeMessageState(int messageID, Message.State state) {
        for (Message message : mMessages) {
            if (message.getID() == messageID && message.getState() != state) {
                message.setState(state);
                mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                mDbHandler.updateMessageState(message, state);
                return message;
            }
        }
        return null;
    }

    public boolean changeMessageID(int oldMessageID, int newMessageID) {
        for (Message message : mMessages) {
            if (message.getID() == oldMessageID) {
                message.setID(newMessageID);
                mDbHandler.updateMessageId(oldMessageID, newMessageID);
                return true;
            }
        }
        return false;
    }

    private void clearSelections() {
        ArrayList<Integer> selectedIndexes = new ArrayList<>(mConversationAdapter.getSelectedIndexes());
        mConversationAdapter.getSelectedIndexes().clear();
        for (int i : selectedIndexes) {
            mConversationAdapter.notifyItemChanged(i);
        }
    }

    private void setSelectionMode(boolean toggle) {
        ActionBar actionBar = getSupportActionBar();

        if (toggle) {
            String selectedTitle = getString(R.string.x_messages_selected, mConversationAdapter.getSelectedIndexes().size());
            setActionBarTitle(selectedTitle);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_messaging_cancel_selection);

            mDeleteMenuItem.setVisible(true);
        } else {
            setActionBarTitle(mOppositeUser.getName());

            actionBar.setDisplayHomeAsUpEnabled(false);

            mDeleteMenuItem.setVisible(false);
        }
    }

    private void setActionBarTitle(String selectedTitle) {
        ActionBar actionBar = getSupportActionBar();

        SpannableString title = new SpannableString(selectedTitle);
        title.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, title.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setSpan(new AbsoluteSizeSpan(60), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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

                            Log.d(TAG, selectedIndexes.toString());

                            Collections.sort(selectedIndexes);
                            Collections.reverse(selectedIndexes);

                            for (int index : selectedIndexes) {
                                Message message = mMessages.get(index);
                                mMessages.remove(index);
                                mDbHandler.deleteMessage(message);
                            }

                            selectedIndexes.clear();
                            setSelectionMode(false);
                            mConversationAdapter.notifyDataSetChanged();

                            if (!mMessages.isEmpty()) {
                                setResult(MESSAGE_INSERTED, getIntent().putExtra("last_message", mMessages.get(0)));
                            } else {
                                setResult(ALL_MESSAGES_DELETED, getIntent().putExtra("opposite_user", mOppositeUser));
                            }

                            // TODO Server delete notify
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
        String email = SessionManager.getCurrentUserDetails(getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getApplicationContext()).getPassword();
        Call<ResponseBody> sendMessage = fcmApi.sendMessage(email, password, message.getText(), message.getReceiver().getID(), message.getID());

        sendMessage.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                if (!responseObject.isNull("oldMessageID") && !responseObject.isNull("newMessageID")){
                                    changeMessageID(responseObject.getInt("oldMessageID"), responseObject.getInt("newMessageID"));
                                    changeMessageState(responseObject.getInt("newMessageID"), Message.State.SENT);
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
        String email = SessionManager.getCurrentUserDetails(getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getApplicationContext()).getPassword();
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
