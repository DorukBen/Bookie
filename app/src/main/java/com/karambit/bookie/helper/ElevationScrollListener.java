package com.karambit.bookie.helper;

import android.support.v7.widget.RecyclerView;

import com.karambit.bookie.MainActivity;
import com.karambit.bookie.ProfileActivity;

/**
 * Created by orcan on 10/29/16.
 */
public class ElevationScrollListener extends RecyclerView.OnScrollListener {

    public static final int ELEVATION_SCROLL_MAX = 120;
    public static final int ACTIONBAR_ELEVATION_DP = 8;

    private MainActivity mA;
    private ProfileActivity mP;
    private int mTabIndex;

    public ElevationScrollListener(MainActivity mainActivity, int tabIndex) {
        mA = mainActivity;
        mTabIndex = tabIndex;
    }

    public ElevationScrollListener(ProfileActivity profileActivity) {
        mP = profileActivity;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        float elevation = getActionbarElevation(recyclerView.computeVerticalScrollOffset());

        if (mA != null){
            mA.setActionBarElevation(elevation, mTabIndex);
        }else{
            mP.setActionBarElevation(elevation);
        }

    }

    public static float getActionbarElevation(int scroll) {
        int absTotal = Math.abs(scroll);
        int clampTotal = absTotal <= ELEVATION_SCROLL_MAX ? absTotal : ELEVATION_SCROLL_MAX;
        return ((float) clampTotal / ELEVATION_SCROLL_MAX) * (LayoutUtils.DP * ACTIONBAR_ELEVATION_DP);
    }
}
