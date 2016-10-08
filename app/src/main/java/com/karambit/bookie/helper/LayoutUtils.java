package com.karambit.bookie.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Ekran ve Layoutlar için ara metodlar
 */
public class LayoutUtils {

    public static final float DP = Resources.getSystem().getDisplayMetrics().density;
    public static final float SP = Resources.getSystem().getDisplayMetrics().scaledDensity;

    /**
     * @param layout Dikey olarak ekran boyutuna uzatılacak olan ViewGroup layout (Relative, Linear, Frame)
     * @return Boyut ayarlama bittiğinde ekran boyutu döndürülür.
     */
    public static int setLayoutHeigthToDisplaySize(Context context, ViewGroup layout) {
        ViewGroup.LayoutParams profileInfoLayoutParams = layout.getLayoutParams();

        int screenSize = LayoutUtils.getDisplayHeight(context) - LayoutUtils.getNavigationBarHeight(context);

        profileInfoLayoutParams.height = screenSize;

        layout.setLayoutParams(profileInfoLayoutParams);

        return screenSize;
    }


    /*
     *      Dikey ekran boyutu döndürülür
     */
    public static int getDisplayHeight(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }


    /**
     * Durum çubuğu boyutu döndürülür
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /*
     *      Ekrana dahil olan navigasyon tuşlarının bulunduğu alanın dikey boyutu döndürülür
     */
    public static int getNavigationBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(context.getResources().getIdentifier(
                "navigation_bar_height", "dimen", "android"));
    }

    // Calculate ActionBar height
    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();

        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            return 0;
        }
    }


    /*
     *      Girilen dp değerini ekran yoğunluğuna göre pixel karşılığı döndürülür
     */
    public static int dpToPx(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int spToPx(Context context, int sp) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale);
    }


    public static RectF getOnScreenRect(View view) {
        return new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    /**
     * @param listView ScrollView içindeki ListView boyutunu maximum yapar ve Dahil olmasını sağlar
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.AT_MOST),
                         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    public static boolean isStatusBarTranscluent(Activity activity) {
        Window w = activity.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        int flags = lp.flags;
        // Here I'm comparing the binary value of Translucent Status Bar with flags in the window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        else
            return false;
    }

    /**
     * Make transition for a view to a target view
     *
     * @param transitionProgress A value between 0f and 1f
     * @param scrollY if content is scrolling, scroll amount added to the Y translation. If its not give zero
     */
    public static void viewToViewTransition(View transitingView, View targetView, float transitionProgress, int scrollY) {

        float scaleChangeX = transitionProgress * ((float) targetView.getWidth() / transitingView.getWidth() - 1f);
        float scaleChangeY = transitionProgress * ((float) targetView.getHeight() / transitingView.getHeight() - 1f);

        transitingView.setScaleX(1 + scaleChangeX);
        transitingView.setScaleY(1 + scaleChangeY);

        float translationX = transitionProgress *
                (targetView.getLeft() + targetView.getRight() - transitingView.getLeft() - transitingView.getRight()) / 2f;

        float translationY = transitionProgress *
                (targetView.getTop() + targetView.getBottom() - transitingView.getTop() - transitingView.getBottom()) / 2f;

        transitingView.setTranslationX(translationX);
        transitingView.setTranslationY(translationY + scrollY);
    }
}
