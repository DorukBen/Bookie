package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.MessageAdapter;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {


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

        MessageAdapter messageAdapter = new MessageAdapter(getActivity(), Message.GENERATOR.generateMessageList(User.GENERATOR.generateUser(), 50));
        messageAdapter.setMessageClickListener(new MessageAdapter.MessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage) {
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra("user", lastMessage.getSender());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(messageAdapter);

        return rootView;
    }

}
