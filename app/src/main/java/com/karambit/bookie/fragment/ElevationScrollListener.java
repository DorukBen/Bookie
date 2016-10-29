package com.karambit.bookie.fragment;

import android.support.v7.widget.RecyclerView;

import com.karambit.bookie.MainActivity;
import com.karambit.bookie.helper.LayoutUtils;

/**
 * Created by orcan on 10/29/16.
 */
class ElevationScrollListener extends RecyclerView.OnScrollListener {

    public static final int ELEVATION_SCROLL_MAX = 120;
    public static final int ACTIONBAR_ELEVATION_DP = 4;

    private MainActivity mA;
    private int totalScrolled = 0;

    public ElevationScrollListener(MainActivity mainActivity) {
        mA = mainActivity;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        totalScrolled += dy;

        int absTotal = Math.abs(totalScrolled);
        int clampTotal = absTotal <= ELEVATION_SCROLL_MAX ? absTotal : ELEVATION_SCROLL_MAX;
        float elevation = ((float) clampTotal / ELEVATION_SCROLL_MAX) * (LayoutUtils.DP * ACTIONBAR_ELEVATION_DP);

        mA.setActionBarElevation(elevation);
    }
}
