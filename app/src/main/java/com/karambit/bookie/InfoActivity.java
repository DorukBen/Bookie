package com.karambit.bookie;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
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

    public static final String EXTRA_INFO_CODES = "info_codes";
    public static final int INFO_CODE_VERIFICATION = 0;
    public static final int INFO_CODE_LOCATION = 1;
    public static final int INFO_CODE_BOOK_COUNTER = 2;
    public static final int INFO_CODE_REQUESTS = 3;
    public static final int INFO_CODE_TRANSACTIONS = 4;
    public static final int INFO_CODE_POINT = 5;
    public static final int INFO_CODE_FEEDBACK = 6;
    public static final int INFO_CODE_CREDITS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        SpannableString s = new SpannableString(getResources().getString(R.string.info));
        s.setSpan(new TypefaceSpan(this, MainActivity.FONT_GENERAL_TITLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        float titleSize = getResources().getDimension(R.dimen.actionbar_app_name_title_size);
        s.setSpan(new AbsoluteSizeSpan((int) titleSize), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primaryTextColor)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            float elevation = getResources().getDimension(R.dimen.actionbar_starting_elevation);
            actionBar.setElevation(elevation);

            setTitle("");

            ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(s);

            toolbar.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

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
        final ImageView infoDropDown_5 = (ImageView) findViewById(R.id.infoDropDownImage_5);
        final ImageView infoDropDown_6 = (ImageView) findViewById(R.id.infoDropDownImage_6);
        final ImageView infoDropDown_7 = (ImageView) findViewById(R.id.infoDropDownImage_7);

        final TextView infoContent_0 = (TextView) findViewById(R.id.infoContent_0);
        final TextView infoContent_1 = (TextView) findViewById(R.id.infoContent_1);
        final TextView infoContent_2 = (TextView) findViewById(R.id.infoContent_2);
        final TextView infoContent_3 = (TextView) findViewById(R.id.infoContent_3);
        final TextView infoContent_4 = (TextView) findViewById(R.id.infoContent_4);
        final TextView infoContent_5 = (TextView) findViewById(R.id.infoContent_5);
        final TextView infoContent_6 = (TextView) findViewById(R.id.infoContent_6);
        final TextView infoContent_7 = (TextView) findViewById(R.id.infoContent_7);

        LinearLayout infoContainer_0 = (LinearLayout) findViewById(R.id.infoTitleContainer_0);
        LinearLayout infoContainer_1 = (LinearLayout) findViewById(R.id.infoTitleContainer_1);
        LinearLayout infoContainer_2 = (LinearLayout) findViewById(R.id.infoTitleContainer_2);
        LinearLayout infoContainer_3 = (LinearLayout) findViewById(R.id.infoTitleContainer_3);
        LinearLayout infoContainer_4 = (LinearLayout) findViewById(R.id.infoTitleContainer_4);
        LinearLayout infoContainer_5 = (LinearLayout) findViewById(R.id.infoTitleContainer_5);
        LinearLayout infoContainer_6 = (LinearLayout) findViewById(R.id.infoTitleContainer_6);
        LinearLayout infoContainer_7 = (LinearLayout) findViewById(R.id.infoTitleContainer_7);

        // Expanded info titles on beginning of activity
        int[] infoCodes = getIntent().getIntArrayExtra(EXTRA_INFO_CODES);

        if (infoCodes != null) {

            for (int code : infoCodes) {

                switch (code) {

                    case INFO_CODE_VERIFICATION:
                        infoDropDown_0.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_0.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_LOCATION:
                        infoDropDown_1.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_1.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_BOOK_COUNTER:
                        infoDropDown_2.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_2.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_REQUESTS:
                        infoDropDown_3.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_3.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_TRANSACTIONS:
                        infoDropDown_4.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_4.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_POINT:
                        infoDropDown_5.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_5.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_FEEDBACK:
                        infoDropDown_6.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_6.setVisibility(View.VISIBLE);
                        break;

                    case INFO_CODE_CREDITS:
                        infoDropDown_7.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        infoContent_7.setVisibility(View.VISIBLE);
                        break;
                }
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

        infoContainer_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_5.getVisibility() == View.GONE) {
                    infoContent_5.setVisibility(View.VISIBLE);
                    infoDropDown_5.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_5.setVisibility(View.GONE);
                    infoDropDown_5.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_6.getVisibility() == View.GONE) {
                    infoContent_6.setVisibility(View.VISIBLE);
                    infoDropDown_6.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_6.setVisibility(View.GONE);
                    infoDropDown_6.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });

        infoContainer_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoContent_7.getVisibility() == View.GONE) {
                    infoContent_7.setVisibility(View.VISIBLE);
                    infoDropDown_7.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    infoContent_7.setVisibility(View.GONE);
                    infoDropDown_7.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });
    }
}
