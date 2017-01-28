package com.karambit.bookie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.karambit.bookie.adapter.ConversationAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = ConversationActivity.class.getSimpleName();

    public static final int MESSAGE_INSERTED = 1;

    private DBHandler mDbHandler;
    private ConversationAdapter mConversationAdapter;
    private ImageButton mSendMessageButton;
    private EditText mMessageEditText;
    private ArrayList<Message> mMessages;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        final User oppositeUser = getIntent().getExtras().getParcelable("user");

        //Changes action bar font style by getting font.ttf from assets/fonts action bars font style doesn't
        // change from styles.xml
        SpannableString s = new SpannableString(oppositeUser.getName());
        s.setSpan(new TypefaceSpan(this, "montserrat_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(60), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(s);
        }

        final User currentUser = SessionManager.getCurrentUser(this);
        mDbHandler = new DBHandler(getApplicationContext());
        mMessages = mDbHandler.getConversationMessages(oppositeUser, currentUser);

        // TODO Broadcast Manager for messages

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendMessageButton = (ImageButton) findViewById(R.id.messageSendButton);

        mRecyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        mRecyclerView.setLayoutManager(layoutManager);

        mConversationAdapter = new ConversationAdapter(this, mMessages);

        mRecyclerView.setAdapter(mConversationAdapter);

        //For improving recyclerviews performance
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // Seen for opposite user messages
        for (Message message : mMessages) {
            if (message.getSender().getID() != currentUser.getID()) {
                if (message.getState() != Message.State.SEEN) {
                    message.setState(Message.State.SEEN);
                    mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));

                    mDbHandler.updateMessageState(message.getID(), Message.State.SEEN);
                }
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
                                                        oppositeUser, Calendar.getInstance(), Message.State.PENDING);
                    insertMessage(message);

                    mMessageEditText.setText("");

                    toggleSendButton(false);

                    mDbHandler.insertMessage(message);
                }
            }
        });
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

    public boolean changeMessageState(int messageID, Message.State state) {
        for (Message message : mMessages) {
            if (message.getID() == messageID) {
                message.setState(state);
                mConversationAdapter.notifyItemChanged(mMessages.indexOf(message));
                return true;
            }
        }
        return false;
    }

    public boolean changeMessageID(int oldMessageID, int newMessageID) {
        for (Message message : mMessages) {
            if (message.getID() == oldMessageID) {
                message.setID(newMessageID);
                return true;
            }
        }
        return false;
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
}
