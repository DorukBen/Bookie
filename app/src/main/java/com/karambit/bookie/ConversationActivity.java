package com.karambit.bookie;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.karambit.bookie.helper.ConversationAdapter;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        User oppositeUser = getIntent().getExtras().getParcelable("user");

        final EditText messageEditText = (EditText) findViewById(R.id.messageEditText);
        final ImageButton sendMessageButton = (ImageButton) findViewById(R.id.messageSendButton);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.conversationRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);

        final User phoneOwner = User.GENERATOR.generateUser();

        final ArrayList<Message> messages = Message.GENERATOR.generateMessageList(phoneOwner, oppositeUser, 50);

        final ConversationAdapter conversationAdapter = new ConversationAdapter(this, phoneOwner, oppositeUser, messages);

        recyclerView.setAdapter(conversationAdapter);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    sendMessageButton.setEnabled(true);
                } else {
                    sendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();

                Message message = new Message(messageText, phoneOwner, Calendar.getInstance(), Message.State.DELIVERED);

                conversationAdapter.insertNewMessage(message);

                messageEditText.setText("");
            }
        });
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ConversationActivity.class);
        context.startActivity(starter);
    }
}
