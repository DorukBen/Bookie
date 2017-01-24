package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.Collections;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    public static final int MESSAGE_FRAGMENT_TAB_INEX = 4;

    private static final String TAG = MessageFragment.class.getSimpleName();

    public static final int LAST_MESSAGE_REQUEST_CODE = 1;

    private DBHandler mDbHandler;
    private ArrayList<Message> mLastMessages;
    private LastMessageAdapter mLastMessageAdapter;
    private PullRefreshLayout mPullRefreshLayout;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the pullRefreshLayout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.messagingRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDbHandler = new DBHandler(getContext().getApplicationContext());

        User currentUser = SessionManager.getCurrentUser(getContext());

        createMessages();

        ArrayList<User> users = mDbHandler.getMessageUsers();
        mLastMessages = mDbHandler.getLastMessages(users, currentUser);
        Collections.sort(mLastMessages);

        mLastMessageAdapter = new LastMessageAdapter(getActivity(), mLastMessages);
        mLastMessageAdapter.setMessageClickListener(new LastMessageAdapter.MessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage) {

                Intent intent = new Intent(getActivity(), ConversationActivity.class);

                User currentUser = SessionManager.getCurrentUser(getContext());

                intent.putExtra("user", lastMessage.getOppositeUser(currentUser));

                startActivityForResult(intent, LAST_MESSAGE_REQUEST_CODE);
            }
        });

        mLastMessageAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mLastMessageAdapter);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), MESSAGE_FRAGMENT_TAB_INEX));

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMessages();
            }
        });

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    /**
     *  TODO Firstly fetch from internet. This is just a demo method...
     */
    private void fetchMessages() {

        final User currentUser = SessionManager.getCurrentUser(getContext());

        new Thread(new Runnable() {
            @Override
            public void run() {

                // createMessages(); // TODO REMOVE

                ArrayList<User> users = mDbHandler.getMessageUsers();
                mLastMessages = new ArrayList<>();
                mLastMessages = mDbHandler.getLastMessages(users, currentUser);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLastMessageAdapter.setLastMessages(mLastMessages);

                        mPullRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void createMessages() {

        mDbHandler.deleteAllMessages();

        if (mDbHandler.getMessageUsers().isEmpty()) {

            User currentUser = SessionManager.getCurrentUser(getContext());

            Random r = new Random();

            for (int i = 0; i < 6; i++) {
                User messageUser = User.GENERATOR.generateUser();
                mDbHandler.insertMessageUser(messageUser);

                for (Message message : Message.GENERATOR.generateMessageList(currentUser, messageUser, 20)) {
                    mDbHandler.insertMessage(message);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, mLastMessages.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAST_MESSAGE_REQUEST_CODE) {
            if (resultCode == ConversationActivity.MESSAGE_INSERTED) {
                Message lastMessage = data.getParcelableExtra("last_message");
                insertLastMessage(lastMessage);
            }
        }
    }

    public void insertLastMessage(Message newMessage) {

        int messageUserIndex = mLastMessages.indexOf(newMessage);

        if (messageUserIndex == -1) {
            mLastMessages.add(0, newMessage);

        } else if (messageUserIndex == 0) {
            mLastMessages.set(0, newMessage);

        } else {
            mLastMessages.remove(messageUserIndex);
            mLastMessages.add(0, newMessage);
        }

        Collections.sort(mLastMessages);

        mLastMessageAdapter.notifyDataSetChanged();
    }

    public boolean changeMessageState(int messageID, Message.State state) {
        for (Message message : mLastMessages) {
            if (message.getID() == messageID) {
                message.setState(state);
                mLastMessageAdapter.notifyItemChanged(mLastMessages.indexOf(message));
                return true;
            }
        }
        return false;
    }

    public boolean changeMessageID(int oldMessageID, int newMessageID) {
        for (Message message : mLastMessages) {
            if (message.getID() == oldMessageID) {
                message.setID(newMessageID);
                return true;
            }
        }
        return false;
    }
}
