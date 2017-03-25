package com.karambit.bookie.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Class using for reScale or crop bitmap and drawables
 *
 * Example:
 *          rate = 72/96f;
 *          scaleImage(bimap_object, rate);
 *
 *          height is 0.75 multiple grater than width value
 *
 * Example:
 *          cropImage(drawable_object, width_value, height_value);
 *
 *          rate = width_value / height_value;
 *
 *          new bitmaps with and height will be proportional
 *
 *
 * Created by doruk on 1.09.2016.
 * Created for BookieApplication
 */
public class ImageScaler {

    /**
     * Scale Image for given ration
     * Changes Images With and height
     *
     * @param image Bitmap image paramater which gonna rescale
     * @param rate float parameter means (witdh scale rate / height scale rate)
     * @return resized bitmap
     */
    public static Bitmap scaleImage(Bitmap image, float rate){

        int newWidth = (int)(image.getHeight() * rate);
        int newHeight = image.getHeight();

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    /**
     * Scale's and crop's Image for given ration
     * Changes Images With and height
     *
     * @param srcBmp Bitmap image paramater which gonna rescale and crop
     * @param rate float parameter means (witdh scale rate / height scale rate)
     * @return resized bitmap
     */
    public static Bitmap cropImage(Bitmap srcBmp, float rate) {

        Bitmap dstBmp;

        int newWidth = (int)(srcBmp.getHeight() * rate);
        int newHeight = srcBmp.getHeight();

        if (srcBmp.getWidth() >= newWidth){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - newWidth/2,
                    0,
                    newWidth,
                    newHeight
            );

        }else{

            newWidth = srcBmp.getWidth();
            newHeight = (int)(srcBmp.getWidth() * (1/rate));

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - newHeight/2,
                    newWidth,
                    newHeight
            );
        }

        return  dstBmp;
    }

    /**
     * Scale's and crop's Image for given ration
     * Changes Images With and height by given arguments
     *
     * @param srcBmp Bitmap image paramater which gonna rescale and crop
     * @param width New bitmaps width
     * @param height New bitmaps height
     * @return returns new scaled bitmap
     */
    public static Bitmap cropImage(Bitmap srcBmp, int width, int height) {

        float rate = width / height;

        return cropImage(srcBmp , rate);
    }

    /**
     *Scale's and crop's Image for given ration
     * Changes Images With and height
     *
     * @param srcDrawable Drawable image paramater which gonna rescale and crop
     * @param rate float parameter means (witdh scale rate / height scale rate)
     * @return resized bitmap
     */
    public static Bitmap cropImage(Drawable srcDrawable, float rate) {

        Bitmap srcBmp = drawableToBitmap(srcDrawable);

        return cropImage(srcBmp , rate);
    }

    /**
     * Scale's and crop's Image for given ration
     * Changes Images With and height
     *
     * @param srcDrawable Drawable image paramater which gonna rescale and crop
     * @param width New bitmaps width
     * @param height New bitmaps height
     * @return returns new scaled bitmap
     */
    public static Bitmap cropImage(Drawable srcDrawable, int width, int height) {

        float rate = width / height;

        Bitmap srcBmp = drawableToBitmap(srcDrawable);

        return cropImage(srcBmp , rate);
    }

    /**
     * Create's new bitmap object from given drawable
     *
     * @param drawable which will be used for creating new bitmap
     * @return returns new scaled bitmap
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
