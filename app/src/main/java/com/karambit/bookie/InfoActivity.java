package com.karambit.bookie;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.karambit.bookie.helper.ElevationScrollListener;
import com.karambit.bookie.helper.TypefaceSpan;

/**
 *  This activity takes an integer array extra for which info titles are expanded on activity start.
 */
public class InfoActivity extends AppCompatActivity {

    public static final int INFO_CODE_REQUESTS = 0;
    public static final int INFO_CODE_TRANSACTIONS = 1;
    public static final int INFO_CODE_POINT = 2;
    public static final int INFO_CODE_FEEDBACK = 3;
    public static final int INFO_CODE_CREDITS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        SpannableString s = new SpannableString(getResources().getString(R.string.info));
        s.setSpan(new TypefaceSpan(this, "comfortaa.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan((int) convertDpToPixel(18, this)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(s);
        actionBar.setElevation(0);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_primary_text_color);

        final ScrollView infoScrollView = (ScrollView) findViewById(R.id.infoScrollView);
        infoScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = infoScrollView.getScrollY();
                actionBar.setElevation(ElevationScrollListener.getActionbarElevation(scrollY));
            }
        });

        final ImageView infoDropDown_0 = (ImageView) findViewById(R.id.infoDropDownImage_0);
        final ImageView infoDropDown_1 = (ImageView) findViewById(R.id.infoDropDownImage_1);
        final ImageView infoDropDown_2 = (ImageView) findViewById(R.id.infoDropDownImage_2);
        final ImageView infoDropDown_3 = (ImageView) findViewById(R.id.infoDropDownImage_3);
        final ImageView infoDropDown_4 = (ImageView) findViewById(R.id.infoDropDownImage_4);

        final TextView infoContent_0 = (TextView) findViewById(R.id.infoContent_0);
        final TextView infoContent_1 = (TextView) findViewById(R.id.infoContent_1);
        final TextView infoContent_2 = (TextView) findViewById(R.id.infoContent_2);
        final TextView infoContent_3 = (TextView) findViewById(R.id.infoContent_3);
        final TextView infoContent_4 = (TextView) findViewById(R.id.infoContent_4);

        LinearLayout infoContainer_0 = (LinearLayout) findViewById(R.id.infoTitleContainer_0);
        LinearLayout infoContainer_1 = (LinearLayout) findViewById(R.id.infoTitleContainer_1);
        LinearLayout infoContainer_2 = (LinearLayout) findViewById(R.id.infoTitleContainer_2);
        LinearLayout infoContainer_3 = (LinearLayout) findViewById(R.id.infoTitleContainer_3);
        LinearLayout infoContainer_4 = (LinearLayout) findViewById(R.id.infoTitleContainer_4);

        // Expanded info titles on beginning of activity
        int[] infoCodes = getIntent().getIntArrayExtra("info_codes");

        for (int code : infoCodes) {

            switch (code) {

                case INFO_CODE_REQUESTS:
                    infoDropDown_1.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    infoContent_1.setVisibility(View.VISIBLE);
                    break;

                case INFO_CODE_TRANSACTIONS:
                    infoDropDown_0.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    infoContent_0.setVisibility(View.VISIBLE);
                    break;

                case INFO_CODE_POINT:
                    infoDropDown_2.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    infoContent_2.setVisibility(View.VISIBLE);
                    break;

                case INFO_CODE_FEEDBACK:
                    infoDropDown_3.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    infoContent_3.setVisibility(View.VISIBLE);
                    break;

                case INFO_CODE_CREDITS:
                    infoDropDown_4.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    infoContent_4.setVisibility(View.VISIBLE);
                    break;
            }
        }

        infoContainer_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_0.getVisibility() == View.GONE) {
                    infoContent_0.setVisibility(View.VISIBLE);
                    infoDropDown_0.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_0.setVisibility(View.GONE);
                    infoDropDown_0.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_1.getVisibility() == View.GONE) {
                    infoContent_1.setVisibility(View.VISIBLE);
                    infoDropDown_1.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_1.setVisibility(View.GONE);
                    infoDropDown_1.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_2.getVisibility() == View.GONE) {
                    infoContent_2.setVisibility(View.VISIBLE);
                    infoDropDown_2.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_2.setVisibility(View.GONE);
                    infoDropDown_2.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_3.getVisibility() == View.GONE) {
                    infoContent_3.setVisibility(View.VISIBLE);
                    infoDropDown_3.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_3.setVisibility(View.GONE);
                    infoDropDown_3.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_4.getVisibility() == View.GONE) {
                    infoContent_4.setVisibility(View.VISIBLE);
                    infoDropDown_4.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_4.setVisibility(View.GONE);
                    infoDropDown_4.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

            default:
                return false;
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
