package com.karambit.bookie.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.R;
import com.karambit.bookie.adapter.HomeTimelineAdapter;
import com.karambit.bookie.model.Book;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.homeRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        HomeTimelineAdapter adapter = new HomeTimelineAdapter(
                getContext(),
                Book.GENERATOR.generateBookList(3),
                Book.GENERATOR.generateBookList(20)
        );

        recyclerView.setAdapter(adapter);

        return rootView;
    }

}
