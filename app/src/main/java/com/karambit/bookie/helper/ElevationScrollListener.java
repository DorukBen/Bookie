package com.karambit.bookie.helper;

import android.support.v7.widget.RecyclerView;

import com.karambit.bookie.MainActivity;

/**
 * Created by orcan on 10/29/16.
 */
public class ElevationScrollListener extends RecyclerView.OnScrollListener {

    public static final int ELEVATION_SCROLL_MAX = 120;
    public static final int ACTIONBAR_ELEVATION_DP = 8;

    private MainActivity mA;
    private int mTabIndex;

    public ElevationScrollListener(MainActivity mainActivity, int tabIndex) {
        mA = mainActivity;
        mTabIndex = tabIndex;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        float elevation = getActionbarElevation(recyclerView.computeVerticalScrollOffset());

        mA.setActionBarElevation(elevation, mTabIndex);
    }

    public static float getActionbarElevation(int scroll) {
        int absTotal = Math.abs(scroll);
        int clampTotal = absTotal <= ELEVATION_SCROLL_MAX ? absTotal : ELEVATION_SCROLL_MAX;
        return ((float) clampTotal / ELEVATION_SCROLL_MAX) * (LayoutUtils.DP * ACTIONBAR_ELEVATION_DP);
    }
}
