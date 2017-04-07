package com.karambit.bookie.fragment;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.BookieApplication;
import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.LastMessageAdapter;
import com.karambit.bookie.database.DBHelper;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.pull_refresh_layout.PullRefreshLayout;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.rest_api.UserApi;
import com.karambit.bookie.service.BookieIntentFilters;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private DBManager mDBManager;
    private ArrayList<Message> mLastMessages;
    private LastMessageAdapter mLastMessageAdapter;
    private PullRefreshLayout mPullRefreshLayout;
    private SparseIntArray mUnseenCounts;
    private BroadcastReceiver mMessageReceiver;
    private boolean isFirstFetch = true;

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

        mDBManager = new DBManager(getContext());
        mDBManager.open();

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

                    Logger.d("Message user unselected at position " + position + ": " + message.getOppositeUser(currentUser).getName());
                } else {
                    mLastMessageAdapter.setSelectedPosition(position);

                    Logger.d("Message user selected at position " + position + ": " + message.getOppositeUser(currentUser).getName());
                }

                mLastMessageAdapter.notifyItemChanged(position);

                if (previousSelected != -1) {
                    mLastMessageAdapter.notifyItemChanged(previousSelected);
                }

                return true;
            }

            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER, user);
                startActivity(intent);
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

                                final User oppositeUser = message.getOppositeUser(currentUser);
                                mDBManager.Threaded(mDBManager.getMessageDataSource().cDeleteConversation(oppositeUser));

                                deleteMessageUserOnServer(oppositeUser);

                                Logger.d("Message user deleted: " + oppositeUser.getName());
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

        mLastMessageAdapter.setOnSearchUserButtonClickListener(new LastMessageAdapter.OnSearchUserButtonClickListener() {
            @Override
            public void onSearchUserButtonClicked() {
                ((MainActivity) getActivity()).setCurrentPage(SearchFragment.TAB_INDEX);
            }
        });

        // listen refresh event
        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchMessages();
            }
        });

        recyclerView.setAdapter(mLastMessageAdapter);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), TAB_INDEX));



        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_RECEIVED)) {
                    final Message message = intent.getParcelableExtra(BookieIntentFilters.EXTRA_MESSAGE);
                    if (message != null) {
                        if (message.getSender().getID() != ConversationActivity.currentConversationUserId) {
                            insertLastMessage(message);
                        }
                    }
                } else {
                    int messageID = intent.getIntExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, -1);
                    if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_DELIVERED)) {
                        if (messageID > 0) {
                            changeMessageState(messageID, Message.State.DELIVERED);
                        }
                    } else if (intent.getAction().equalsIgnoreCase(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_SEEN)) {
                        if (messageID > 0) {
                            changeMessageState(messageID, Message.State.SEEN);
                        }
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_RECEIVED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_DELIVERED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_SEEN));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SessionManager.isLoggedIn(getContext())){
            fetchMessages();
        }
    }

    private void fetchMessagesFromServer() {

        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        final User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();

        String lastMessageUserIds;

        int i = 0;

        StringBuilder builder = new StringBuilder();
        for (Message message: mLastMessages){
            builder.append(message.getOppositeUser(currentUserDetails.getUser()).getID());
            if (i < mLastMessages.size() - 1){
                builder.append("_");
            }
            i++;
        }
        lastMessageUserIds = builder.toString();

        Call<ResponseBody> fetchMessages = userApi.fetchMessages(email, password, lastMessageUserIds);

        Logger.d("fetchMessages() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tuserIDs=" + lastMessageUserIds);

        fetchMessages.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                ArrayList<Message> messages = Message.jsonObjectToMessageList(responseObject);
                                for (final Message message: messages){
                                    if (message.getReceiver().equals(currentUserDetails.getUser()) &&
                                        message.getState() == Message.State.SENT) {

                                        message.setState(Message.State.DELIVERED);

                                        uploadMessageStateToServer(message);
                                    }
                                    mDBManager.getMessageDataSource().saveMessage(message, message.getOppositeUser(currentUserDetails.getUser()));

                                    mDBManager.checkAndUpdateAllUsers(message.getOppositeUser(currentUserDetails.getUser()));
                                }

                                mLastMessages = mDBManager.getMessageDataSource().getLastMessages(SessionManager.getCurrentUser(getContext()));

                                Collections.sort(mLastMessages);
                                mLastMessageAdapter.setLastMessages(mLastMessages);
                                fetchUnseenCounts();

                                isFirstFetch = false;

                                Logger.d("Messages fetched from server:\n\nLast Messages:\n\n" + mLastMessages);

                                ((MainActivity) getActivity()).hideError();

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response. errorCode = " + errorCode);

                                ((MainActivity) getActivity()).showUnknownError();
                            }
                        }else{
                            Logger.e("Response body is null. (Message Fragment Error)");

                            ((MainActivity) getActivity()).showUnknownError();
                        }
                    }else{
                        Logger.e("Response object is null. (Message Fragment Error)");

                        ((MainActivity) getActivity()).showUnknownError();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("JSONException or IOException caught: " + e.getMessage());

                    if (!BookieApplication.hasNetwork()) {
                        ((MainActivity) getActivity()).showConnectionError();
                    } else {
                        ((MainActivity) getActivity()).hideError();
                    }
                }

                mPullRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("fetchMessages() Failure: " + t.getMessage());

                mPullRefreshLayout.setRefreshing(false);

                if (!BookieApplication.hasNetwork()) {
                    ((MainActivity) getActivity()).showConnectionError();
                } else {
                    ((MainActivity) getActivity()).hideError();
                }
            }
        });
    }

    private void deleteMessageUserOnServer(final User oppositeUser) {
        int userId = oppositeUser.getID();

        final UserApi userApi = BookieClient.getClient().create(UserApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> deleteConversation = userApi.deleteConversation(email, password, userId);

        Logger.d("deleteConversation() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password + ", \n\tuserID=" + userId);

        deleteConversation.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Logger.d("Conversation with " + oppositeUser.getName() + " deleted from server!");
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response. errorCode = " + errorCode);
                            }
                        }else{
                            Logger.e("Response body is null. (Message Fragment Error)");
                        }
                    }else {
                        Logger.e("Response object is null. (Message Fragment Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("JSONException or IOException caught: " + e.getMessage());
                }

                mPullRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                mPullRefreshLayout.setRefreshing(false);
                Logger.e("deleteConversation() Failure: " + t.getMessage());
            }
        });
    }

    private void uploadMessageStateToServer(final Message message) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(getContext());

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> uploadMessageState = fcmApi.uploadMessageState(email, password, message.getID(), message.getState().getStateCode());

        Logger.d("uploadMessageState() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tmessageID=" + message.getID() + ", \n\tmessageState=" + message.getState().getStateCode());

        uploadMessageState.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {
                                Logger.d("Message state uploaded to server: " + message.getState());
                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);
                            }

                        }else{
                            Logger.e("Response body is null. (Upload Message State Error)");
                        }
                    }else {
                        Logger.e("Response object is null. (Upload Message State Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("uploadMessageState Failure: " + t.getMessage());
                uploadMessageStateToServer(message);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    public void fetchMessages() {
        if (isFirstFetch){
            mLastMessages = mDBManager.getMessageDataSource().getLastMessages(SessionManager.getCurrentUser(getContext()));
            fetchMessagesFromServer();

            Collections.sort(mLastMessages);
            mLastMessageAdapter.setLastMessages(mLastMessages);
            fetchUnseenCounts();
        }else {
            fetchMessagesFromServer();
        }
    }

    // Last messages should be sorted
    private void fetchUnseenCounts() {
        if (mUnseenCounts == null) {
            mUnseenCounts = new SparseIntArray(mLastMessages.size());
        } else {
            mUnseenCounts.clear();
        }

        for (int i = 0; i < mLastMessages.size(); i++) {
            Message message = mLastMessages.get(i);
            int unseenCount = mDBManager.getMessageDataSource().getUnseenMessageCount(message.getOppositeUser(SessionManager.getCurrentUser(getContext())));
            mUnseenCounts.append(i, unseenCount);
        }

        mLastMessageAdapter.setUnseenCounts(mUnseenCounts);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAST_MESSAGE_REQUEST_CODE) {

            if (resultCode == ConversationActivity.RESULT_MESSAGE_INSERTED) {
                Message lastMessage = data.getParcelableExtra(ConversationActivity.EXTRA_LAST_MESSAGE);
                insertLastMessage(lastMessage);

            } else if (resultCode == ConversationActivity.RESULT_ALL_MESSAGES_DELETED) {

                User currentUser = SessionManager.getCurrentUser(getContext());
                final User oppositeUser = data.getParcelableExtra(ConversationActivity.EXTRA_OPPOSITE_USER);

                for (int i = 0; i < mLastMessages.size(); i++) {
                    Message message = mLastMessages.get(i);
                    if (message.getOppositeUser(currentUser).equals(oppositeUser)) {
                        mLastMessages.remove(i);
                        mLastMessageAdapter.notifyItemRemoved(i);
                    }
                }
                mDBManager.Threaded(mDBManager.getMessageDataSource().cDeleteConversation(oppositeUser));

                Logger.d("All messages deleted in ConversationActivity with " + oppositeUser.getName() + " (message user deleted)");
            }
        }
    }

    public void insertLastMessage(final Message newMessage) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DBHelper.class) {
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

                Logger.d("Message state changed to " + state + " from " + message.getState());

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

        Logger.d("Message users unselected");
    }
}
