package com.karambit.bookie.fragment;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.karambit.bookie.service.BookieIntentFilters;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private static final String TAG = MessageFragment.class.getSimpleName();

    public static final int TAB_INDEX = 4;
    public static final int VIEW_PAGER_INDEX = 3;
    public static final String TAB_SPEC = "tab_message";
    public static final String TAB_INDICATOR = "tab4";

    public static final int LAST_MESSAGE_REQUEST_CODE = 1;

    private DBHandler mDbHandler;
    private ArrayList<Message> mLastMessages;
    private LastMessageAdapter mLastMessageAdapter;
    private PullRefreshLayout mPullRefreshLayout;
    private SparseIntArray mUnseenCounts;
    private BroadcastReceiver mMessageReceiver;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the pullRefreshLayout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.messagingRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDbHandler = DBHandler.getInstance(getContext());

        final User currentUser = SessionManager.getCurrentUser(getContext());

        mLastMessageAdapter = new LastMessageAdapter(getActivity());

        mPullRefreshLayout = (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        mLastMessageAdapter.setOnMessageClickListener(new LastMessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Message lastMessage, int position) {

                Intent intent = new Intent(getActivity(), ConversationActivity.class);

                User currentUser = SessionManager.getCurrentUser(getContext());

                intent.putExtra(ConversationActivity.EXTRA_USER, lastMessage.getOppositeUser(currentUser));

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

                    Log.i(TAG, "Message user unselected at position " + position + ": " + message.getOppositeUser(currentUser).getName());
                } else {
                    mLastMessageAdapter.setSelectedPosition(position);

                    Log.i(TAG, "Message user selected at position " + position + ": " + message.getOppositeUser(currentUser).getName());
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
                            mLastMessageAdapter.notifyDataSetChanged();

                            User oppositeUser = message.getOppositeUser(currentUser);
                            mDbHandler.deleteMessageUsersConversation(oppositeUser);
                            mDbHandler.deleteMessageUser(oppositeUser);

                            deleteMessageUserOnServer(oppositeUser);

                            Log.i(TAG, "Message user deleted: " + oppositeUser.getName());
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

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), TAB_INDEX));

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setHasFixedSize(true);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_RECEIVED)){
                    final Message message = intent.getParcelableExtra(BookieIntentFilters.EXTRA_MESSAGE);
                    if (message != null){
                        if (message.getSender().getID() != ConversationActivity.currentConversationUserId){
                            insertLastMessage(message);
                        }
                    }
                } else {
                    int messageID = intent.getIntExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, -1);
                    if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_DELIVERED)){
                        if (messageID > 0){
                            changeMessageState(messageID, Message.State.DELIVERED);
                        }
                    } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.INTENT_FILTER_MESSAGE_SEEN)){
                        if (messageID > 0){
                            changeMessageState(messageID, Message.State.SEEN);
                        }
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_DELIVERED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.INTENT_FILTER_MESSAGE_SEEN));
        return rootView;
    }

    private void deleteMessageUserOnServer(User oppositeUser) {
        int userId = oppositeUser.getID();

        // TODO Server delete message user
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    public void fetchMessages() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHandler.class) {
                    final User currentUser = SessionManager.getCurrentUser(getContext());
                    final ArrayList<User> users = new ArrayList<>();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (DBHandler.class){
                                users.addAll(mDbHandler.getAllMessageUsers());
                            }
                        }
                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (DBHandler.class){
                                mLastMessages = mDbHandler.getLastMessages(users, currentUser);
                                Collections.sort(mLastMessages);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLastMessageAdapter.setLastMessages(mLastMessages);

                                        mPullRefreshLayout.setRefreshing(false);

                                        synchronized (DBHandler.class) {
                                            fetchUnseenCounts();
                                        }
                                    }
                                });
                            }
                        }
                    }).start();

                }
            }
        }).start();
    }

    // Last messages should be sorted
    private void fetchUnseenCounts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHandler.class){
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

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLastMessageAdapter.setUnseenCounts(mUnseenCounts);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAST_MESSAGE_REQUEST_CODE) {

            if (resultCode == ConversationActivity.RESULT_MESSAGE_INSERTED) {
                Message lastMessage = data.getParcelableExtra(ConversationActivity.EXTRA_LAST_MESSAGE);
                insertLastMessage(lastMessage);

            } else if (resultCode == ConversationActivity.RESULT_ALL_MESSAGES_DELETED) {

                User currentUser = SessionManager.getCurrentUser(getContext());
                User oppositeUser = data.getParcelableExtra(ConversationActivity.EXTRA_OPPOSITE_USER);

                for (int i = 0; i < mLastMessages.size(); i++) {
                    Message message = mLastMessages.get(i);
                    if (message.getOppositeUser(currentUser).equals(oppositeUser)) {
                        mLastMessages.remove(i);
                        mLastMessageAdapter.notifyItemRemoved(i);
                    }
                }
                mDbHandler.deleteMessageUser(oppositeUser);

                Log.i(TAG, "All messages deleted in ConversationActivity with " + oppositeUser.getName() + " (message user deleted)");
            }
        }
    }

    public void insertLastMessage(final Message newMessage) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHandler.class){
                    User currentUser = SessionManager.getCurrentUser(getContext());

                    int messageUserIndex = -1;

                    for (int i = 0; i < mLastMessages.size(); i++) {
                        Message m = mLastMessages.get(i);
                        if (m.getOppositeUser(currentUser).equals(newMessage.getOppositeUser(currentUser))) {
                            messageUserIndex = i;
                        }
                    }

                    int selectedPosition = mLastMessageAdapter.getSelectedPosition();

                    if (messageUserIndex == -1) {
                        mLastMessages.add(0, newMessage);

                        // Selection correction
                        if (selectedPosition != -1) {
                            mLastMessageAdapter.setSelectedPosition(selectedPosition + 1);
                        }

                    } else if (messageUserIndex == 0) {
                        mLastMessages.set(0, newMessage);

                    } else {
                        mLastMessages.remove(messageUserIndex);
                        mLastMessages.add(0, newMessage);

                        // Selection correction
                        if (selectedPosition != -1) {

                            if (selectedPosition < messageUserIndex) {
                                mLastMessageAdapter.setSelectedPosition(selectedPosition + 1);

                            } else if (selectedPosition == messageUserIndex) {
                                mLastMessageAdapter.setSelectedPosition(0);
                            }
                        }
                    }

                    Collections.sort(mLastMessages);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            fetchUnseenCounts();

                            mLastMessageAdapter.setLastMessages(mLastMessages);


                        }
                    });
                }
            }
        }).start();
    }

    public boolean changeMessageState(int messageID, Message.State state) {
        for (Message message : mLastMessages) {
            if (message.getID() == messageID) {

                Log.i(TAG, "Message state changed to " + state + " from " + message.getState());

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

        Log.i(TAG, "Message users unselected");
    }
}
