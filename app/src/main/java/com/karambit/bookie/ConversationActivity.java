package com.karambit.bookie;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.karambit.bookie.adapter.ConversationAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.TypefaceSpan;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = ConversationActivity.class.getSimpleName();

    public static final int MESSAGE_INSERTED = 1;
    public static final int ALL_MESSAGES_DELETED = 2;

    private User mOppositeUser;
    private DBHandler mDbHandler;
    private ConversationAdapter mConversationAdapter;
    private ImageButton mSendMessageButton;
    private EditText mMessageEditText;
    private ArrayList<Message> mMessages;
    private RecyclerView mRecyclerView;
    private MenuItem mDeleteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mOppositeUser = getIntent().getExtras().getParcelable("user");

        setActionBarTitle(mOppositeUser.getName());

        final User currentUser = SessionManager.getCurrentUser(this);
        mDbHandler = new DBHandler(getApplicationContext());
        mMessages = mDbHandler.getConversationMessages(mOppositeUser, currentUser);

        // TODO Broadcast Manager for messages

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

                if (currentPosition != mStoredPosition) {

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

        mConversationAdapter = new ConversationAdapter(this, mMessages);

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

                        String selectedTitle = getString(R.string.x__messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);

                    } else {

                        selectedIndexes.remove((Integer) position);

                        if (selectedIndexes.isEmpty()) {
                            setSelectionMode(false);
                        } else {
                            String selectedTitle = getString(R.string.x__messages_selected, selectedIndexes.size());
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

                    String selectedTitle = getString(R.string.x__messages_selected, selectedIndexes.size());
                    setActionBarTitle(selectedTitle);

                } else {
                    selectedIndexes.remove((Integer) position);

                    if (selectedIndexes.isEmpty()) {
                        setSelectionMode(false);
                    } else {
                        String selectedTitle = getString(R.string.x__messages_selected, selectedIndexes.size());
                        setActionBarTitle(selectedTitle);
                    }
                }
                mConversationAdapter.notifyItemChanged(position);
                return true;
            }
        });

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
                                                  mOppositeUser, Calendar.getInstance(), Message.State.PENDING);
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
            String selectedTitle = getString(R.string.x__messages_selected, mConversationAdapter.getSelectedIndexes().size());
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
}
