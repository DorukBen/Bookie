package com.karambit.bookie.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.R;
import com.karambit.bookie.helper.ProfileTimelineAdapter;
import com.karambit.bookie.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ProfileTimelineAdapter(getContext(), User.GENERATOR.generateUserDetails()));

        return rootView;
    }

}
