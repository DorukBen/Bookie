package com.karambit.bookie.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.adapter.HomeTimelineAdapter;
import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.model.Book;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final int HOME_FRAGMENT_TAB_INEX = 0;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.homeRecyclerView);

        recyclerView.setOnScrollListener(new ElevationScrollListener((MainActivity) getActivity(), HOME_FRAGMENT_TAB_INEX));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        HomeTimelineAdapter adapter = new HomeTimelineAdapter(
                getContext(),
                Book.GENERATOR.generateBookList(3),
                Book.GENERATOR.generateBookList(20)
        );

        adapter.setHasStableIds(true);

        recyclerView.setAdapter(adapter);

        //For improving recyclerviews performance
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        return rootView;
    }

}
