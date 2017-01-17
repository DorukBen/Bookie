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

    public static final int LAST_MESSAGE_CHANGED = 1;

    private DBHandler mDbHandler;
    private ConversationAdapter mConversationAdapter;
    private ImageButton mSendMessageButton;
    private EditText mMessageEditText;

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

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendMessageButton = (ImageButton) findViewById(R.id.messageSendButton);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);

        final User currentUser = SessionManager.getCurrentUser(this);
        final ArrayList<Message> messages = new DBHandler(getApplicationContext()).getConversationMessages(oppositeUser, currentUser);

        mConversationAdapter = new ConversationAdapter(this, messages);

        recyclerView.setAdapter(mConversationAdapter);

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

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

        mDbHandler = new DBHandler(getApplicationContext());
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = mMessageEditText.getText().toString();
                messageText = messageText.trim();

                if (!TextUtils.isEmpty(messageText)) {

                    messageText = trimInnerNewLines(messageText);

                    Message message = new Message(messageText, currentUser,
                                                  oppositeUser, Calendar.getInstance(), Message.State.PENDING);

                    insertMessageProcesses(message);

                    mMessageEditText.setText("");

                    toggleSendButton(false);
                }
            }
        });
    }

    private void insertMessageProcesses(Message message) {
        mConversationAdapter.insertNewMessage(message);
        mDbHandler.insertMessage(message);

        setResult(LAST_MESSAGE_CHANGED, getIntent().putExtra("last_message", message));
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
