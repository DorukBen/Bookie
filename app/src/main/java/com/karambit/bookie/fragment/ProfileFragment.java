package com.karambit.bookie.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.BookActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.ProfileActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.ProfileTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IS_CURRENT_USER = "is_current_user";
    private static final String USER = "user";

    // TODO: Rename and change types of parameters
    private boolean mIsCurrentUser;
    private User mUser;

    public static final int PROFILE_FRAGMENT_TAB_INEX = 3;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isCurrentUser Parameter 1.
     * @param user Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(boolean isCurrentUser, User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_CURRENT_USER, isCurrentUser);
        args.putParcelable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsCurrentUser = getArguments().getBoolean(IS_CURRENT_USER);
            mUser = getArguments().getParcelable(USER);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // TODO Control which user's profile is going to displayed

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ProfileTimelineAdapter adapter = new ProfileTimelineAdapter(getContext(), User.GENERATOR.generateUserDetails());
        adapter.setBookClickListener(new ProfileTimelineAdapter.BookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(getContext(), BookActivity.class);
                intent.putExtra("book", book);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        if (mIsCurrentUser){
            recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), PROFILE_FRAGMENT_TAB_INEX));
        }else{
            recyclerView.setOnScrollListener(new ElevationScrollListener((ProfileActivity) getActivity()));
        }


        return rootView;
    }

}
