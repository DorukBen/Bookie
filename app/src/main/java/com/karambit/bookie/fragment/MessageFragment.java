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

import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.MessageAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    public static final int MESSAGE_FRAGMENT_TAB_INEX = 4;


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

        DBHandler dbHandler = new DBHandler(getContext());

        /*for (int i = 0; i < 5; i++) {
            User user = User.GENERATOR.generateUser();
            User currentUser = SessionManager.getCurrentUser(getContext());
            for (int j = 0; j < 60; j++) {
                String text = Message.GENERATOR.generateRandomText();
                dbHandler.insertMessage(new Message(i, text, user, currentUser, Calendar.getInstance(), Message.State.DELIVERED));
            }

            dbHandler.insertMessageUser(user);
        }*/

        Log.i("OSMAN", "?");

        ArrayList<Message> lastMessages = new DBHandler(getContext()).getLastMessages();

        Log.i("OSMAN", lastMessages.toString());

        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), lastMessages);
        messageAdapter.setMessageClickListener(new MessageAdapter.MessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage) {
                Intent intent = new Intent(getActivity(), ConversationActivity.class);

                User currentUser = SessionManager.getCurrentUser(getContext());

                if (currentUser.getID() != lastMessage.getSender().getID()) {
                    intent.putExtra("user", lastMessage.getSender());
                } else if (currentUser.getID() != lastMessage.getReceiver().getID()){
                    intent.putExtra("user", lastMessage.getReceiver());
                }

                startActivity(intent);
            }
        });

        messageAdapter.setHasStableIds(true);

        recyclerView.setAdapter(messageAdapter);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), MESSAGE_FRAGMENT_TAB_INEX));

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        return rootView;
    }

}
