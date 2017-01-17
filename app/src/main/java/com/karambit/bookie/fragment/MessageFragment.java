package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baoyz.widget.PullRefreshLayout;
import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.LastMessageAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    public static final int MESSAGE_FRAGMENT_TAB_INEX = 4;

    public static final int LAST_MESSAGE_REQUEST_CODE = 1;

    private DBHandler mDbHandler;
    private ArrayList<Message> mLastMessages;
    private int mLastClickedMessageIndex;
    private LastMessageAdapter mLastMessageAdapter;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.messagingRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDbHandler = new DBHandler(getContext().getApplicationContext());

        User currentUser = SessionManager.getCurrentUser(getContext().getApplicationContext());

        createMessages();

        ArrayList<User> users = mDbHandler.getMessageUsers();
        mLastMessages = mDbHandler.getLastMessages(users, currentUser);
        Collections.sort(mLastMessages);

        mLastMessageAdapter = new LastMessageAdapter(getActivity(), mLastMessages);
        mLastMessageAdapter.setMessageClickListener(new LastMessageAdapter.MessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage) {
                mLastClickedMessageIndex = mLastMessages.indexOf(lastMessage);

                Intent intent = new Intent(getActivity(), ConversationActivity.class);

                User currentUser = SessionManager.getCurrentUser(getContext().getApplicationContext());

                if (currentUser.getID() != lastMessage.getSender().getID()) {
                    intent.putExtra("user", lastMessage.getSender());
                } else if (currentUser.getID() != lastMessage.getReceiver().getID()){
                    intent.putExtra("user", lastMessage.getReceiver());
                }

                startActivityForResult(intent, LAST_MESSAGE_REQUEST_CODE);
            }
        });

        mLastMessageAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mLastMessageAdapter);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), MESSAGE_FRAGMENT_TAB_INEX));

        PullRefreshLayout layout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                //TODO: On page refresh events here layout.serRefreshing() true for start on refresh method
            }
        });

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    private void createMessages() {

        mDbHandler.deleteAllMessages();

        if (mDbHandler.getMessageUsers().size() == 0) {

            User currentUser = SessionManager.getCurrentUser(getContext().getApplicationContext());

            Random r = new Random();

            for (int i = 0; i < 6; i++) {
                User messageUser = User.GENERATOR.generateUser();
                mDbHandler.insertMessageUser(messageUser);

                for (int j = 0; j < 20; j++) {
                    String text = Message.GENERATOR.generateRandomText();
                    Message messageCurrentUser = new Message(0, text, messageUser, currentUser,
                                                             Calendar.getInstance(), Message.State.DELIVERED);

                    Message messageOppositeUser = new Message(1, text, currentUser, messageUser,
                                                              Calendar.getInstance(), Message.State.DELIVERED);

                    mDbHandler.insertMessage(r.nextBoolean() ? messageCurrentUser : messageOppositeUser);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAST_MESSAGE_REQUEST_CODE) {
            if (resultCode == ConversationActivity.LAST_MESSAGE_CHANGED) {

                Message lastMessage = data.getParcelableExtra("last_message");

                insertLastMessage(lastMessage);
            }
        }
    }

    public void insertLastMessage(Message newMessage) {

        int messageUserIndex = -1; // If it does not exists it remains as -1
        int i = 0;

        while (messageUserIndex == -1 && i < mLastMessages.size()) {
            Message message = mLastMessages.get(i);
            User currentUser = SessionManager.getCurrentUser(getContext().getApplicationContext());

            if ((currentUser.getID() != newMessage.getSender().getID() &&
                    (newMessage.getSender().getID() == message.getSender().getID() ||
                        newMessage.getSender().getID() == message.getReceiver().getID())) ||
                (currentUser.getID() != newMessage.getReceiver().getID() &&
                    (newMessage.getReceiver().getID() == message.getSender().getID() ||
                        newMessage.getReceiver().getID() == message.getReceiver().getID()))) {

                    messageUserIndex = i;
                }

            i++;
        }

        if (messageUserIndex == -1) {
            mLastMessages.add(0, newMessage);

        } else if (messageUserIndex == 0) {
            mLastMessages.set(0, newMessage);

        } else {
            mLastMessages.remove(messageUserIndex);
            mLastMessages.add(0, newMessage);
        }

        mLastMessageAdapter.notifyDataSetChanged();
    }

    private boolean messageExists(Message newMessage) {
        for (Message message : mLastMessages) {
            if (message.equals(newMessage)) {
                return true;
            }
        }
        return false;
    }
}
