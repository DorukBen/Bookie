package com.karambit.bookie.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.LastMessageAdapter;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Collections;

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
    private SparseIntArray mUnseenCounts;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the pullRefreshLayout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.messagingRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDbHandler = new DBHandler(getContext().getApplicationContext());

        final User currentUser = SessionManager.getCurrentUser(getContext());

        // TODO Setup Broadcast Listener for messages

        mLastMessageAdapter = new LastMessageAdapter(getActivity());

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        // First fetch messages
        fetchMessages();

        mLastMessageAdapter.setOnMessageClickListener(new LastMessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage, int position) {

                Intent intent = new Intent(getActivity(), ConversationActivity.class);

                User currentUser = SessionManager.getCurrentUser(getContext());

                intent.putExtra("user", lastMessage.getOppositeUser(currentUser));

                startActivityForResult(intent, LAST_MESSAGE_REQUEST_CODE);
            }

            @Override
            public boolean onMessageLongClick(Message message, final int position) {

                final MainActivity mainActivity = (MainActivity) getActivity();

                MainActivity.TouchEventListener touchEventListener = new MainActivity.TouchEventListener() {

                    private boolean mFirstTouchUp = false;
                    private boolean mSecondTouchDown = false;
                    //private boolean mSecondTouchUp = false;

                    @Override
                    public void onTouchEvent(MotionEvent event) {

                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            mFirstTouchUp = true;
                        }

                        if (mFirstTouchUp) {

                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                mSecondTouchDown = true;
                            }

                            /*
                               if (event.getAction() == MotionEvent.ACTION_UP && mSecondTouchDown) {
                                   mSecondTouchUp = true;
                               }
                               */

                            if (mSecondTouchDown) {

                                final View view = recyclerView.getChildAt(position);

                                int[] location = new int[2];
                                view.getLocationInWindow(location);

                                int left = location[0];
                                int right = left + view.getWidth();
                                int top = location[1];
                                int bottom = top + view.getHeight();

                                if (!(left < event.getX() && right > event.getX() && top < event.getY() && bottom > event.getY())) {

                                    unSelectMessages();

                                    mainActivity.removeTouchEventListener(this);
                                }
                            }
                        }
                    }
                };

                mainActivity.removeTouchEventListener(touchEventListener);

                mainActivity.addTouchEventListener(touchEventListener);

                int previousSelected = mLastMessageAdapter.getSelectedPosition();

                if (previousSelected == position) {
                    mLastMessageAdapter.setSelectedPosition(-1);
                } else {
                    mLastMessageAdapter.setSelectedPosition(position);
                }

                mLastMessageAdapter.notifyItemChanged(position);

                if (previousSelected != -1) {
                    mLastMessageAdapter.notifyItemChanged(previousSelected);
                }

                return true;
            }
        });

        mLastMessageAdapter.setOnSelectedStateClickListener(new LastMessageAdapter.OnSelectedStateClickListener() {
            @Override
            public void onDeleteClick(final Message message, final int position) {
                new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.delete_prompt_message_user, message.getOppositeUser(currentUser).getName()))
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            unSelectMessages();
                            mLastMessages.remove(message);
                            mLastMessageAdapter.notifyItemRemoved(position);

                            mDbHandler.deleteMessageUsersConversation(message.getOppositeUser(currentUser));
                            mDbHandler.deleteMessageUser(message.getOppositeUser(currentUser));

                            // TODO Server delete notify
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            unSelectMessages();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            unSelectMessages();
                        }
                    })
                    .create().show();
            }

            @Override
            public boolean onSelectedEmptyClick(Message message, int position) {
                unSelectMessages();
                return true;
            }
        });

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMessages();
            }
        });

        mLastMessageAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mLastMessageAdapter);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), MESSAGE_FRAGMENT_TAB_INEX));

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    /**
     * TODO Firstly fetch from internet. This is just a demo method...
     */
    private void fetchMessages() {

        mPullRefreshLayout.setRefreshing(true);

        new Thread(new Runnable() {
            @Override
            public void run() {

                generateMessages(); // TODO REMOVE

                final User currentUser = SessionManager.getCurrentUser(getContext());
                ArrayList<User> users = mDbHandler.getAllMessageUsers();
                mLastMessages = mDbHandler.getLastMessages(users, currentUser);
                Collections.sort(mLastMessages);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLastMessageAdapter.setLastMessages(mLastMessages);

                        mPullRefreshLayout.setRefreshing(false);

                        fetchUnseenCounts();
                    }
                });
            }
        }).start();
    }

    // Last messages should be sorted
    private void fetchUnseenCounts() {

        User currentUser = SessionManager.getCurrentUser(getContext());

        if (mUnseenCounts == null) {
            mUnseenCounts = new SparseIntArray(mLastMessages.size());
        } else {
            mUnseenCounts.clear();
        }

        for (int i = 0; i < mLastMessages.size(); i++) {
            Message message = mLastMessages.get(i);
            int unseenCount = mDbHandler.getUnseenMessageCount(message.getOppositeUser(currentUser));
            mUnseenCounts.append(i, unseenCount);
        }

        mLastMessageAdapter.setUnseenCounts(mUnseenCounts);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAST_MESSAGE_REQUEST_CODE) {

            if (resultCode == ConversationActivity.MESSAGE_INSERTED) {
                Message lastMessage = data.getParcelableExtra("last_message");
                insertLastMessage(lastMessage);

            } else if (resultCode == ConversationActivity.ALL_MESSAGES_DELETED) {

                User currentUser = SessionManager.getCurrentUser(getContext());
                User oppositeUser = data.getParcelableExtra("opposite_user");

                for (int i = 0; i < mLastMessages.size(); i++) {
                    Message message = mLastMessages.get(i);
                    if (message.getOppositeUser(currentUser).getID() == oppositeUser.getID()) {
                        mLastMessages.remove(i);
                        mLastMessageAdapter.notifyItemRemoved(i);
                        mDbHandler.deleteMessageUser(oppositeUser);
                    }
                }
            }

            fetchUnseenCounts();
        }
    }

    public void insertLastMessage(Message newMessage) {

        User currentUser = SessionManager.getCurrentUser(getContext());

        int messageUserIndex = -1;

        for (int i = 0; i < mLastMessages.size(); i++) {
            Message m = mLastMessages.get(i);
            if (m.getOppositeUser(currentUser).getID() == newMessage.getOppositeUser(currentUser).getID()) {
                messageUserIndex = i;
            }
        }

        if (messageUserIndex == -1) {
            mLastMessages.add(0, newMessage);

        } else if (messageUserIndex == 0) {
            mLastMessages.set(0, newMessage);

        } else {
            mLastMessages.remove(messageUserIndex);
            mLastMessages.add(0, newMessage);
        }

        Collections.sort(mLastMessages);

        fetchUnseenCounts();

        mLastMessageAdapter.setLastMessages(mLastMessages);
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

    private void unSelectMessages() {
        int tempIndex = mLastMessageAdapter.getSelectedPosition();
        mLastMessageAdapter.setSelectedPosition(-1);
        mLastMessageAdapter.notifyItemChanged(tempIndex);
    }

    private void generateMessages() {

        mDbHandler.deleteAllMessages();

        if (mDbHandler.getAllMessageUsers().isEmpty()) {

            User currentUser = SessionManager.getCurrentUser(getContext());

            for (int i = 0; i < 6; i++) {
                User messageUser = User.GENERATOR.generateUser();
                mDbHandler.insertMessageUser(messageUser);

                for (Message message : Message.GENERATOR.generateMessageList(currentUser, messageUser, 50)) {
                    mDbHandler.insertMessage(message);
                }
            }
        }
    }
}
