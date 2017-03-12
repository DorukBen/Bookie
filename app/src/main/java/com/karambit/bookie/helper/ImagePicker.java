package com.karambit.bookie.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.karambit.bookie.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for picking image from gallery or take a new picture.
 * You need to call getPickImageIntent() method and call startActivity for result in activity.
 *
 * Example:
 *          startActivityForResult(ImagePicker.getPickImageIntent(your_context), request_code);
 *
 *On onActivityResult method you neeed to use getImageFromResult() data method and this method return you a bitmap object
 *
 * Example:
 *          ImagePicker.getImageFromResult(your_context, result_code, data);
 *
 *
 * Created by doruk on 1.09.2016.
 * Created for BookieApplication
 */
public class ImagePicker {

    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels count
    private static final String TAG = ImagePicker.class.getSimpleName();
    private static final String TEMP_IMAGE_NAME = "templateImage";

    /**
     * On called method create's a container intent which have take picture intents
     *and pick from gallery intents. Uses addIntentsToList method and adds intents to
     * intent list so if you want to learn which intent selected look at list.
     *
     * @param context current context
     * @return returns intent which contains take picture and pick picture from
     * gallery intents. You can not know which intent user choose. You need to
     * control intent list.
     */
    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        //Intent for pick Image from gallery
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Intent for take picture
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));

        //Add Intents to intent list
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);


        //add all items in intent list to chooser Intent container
        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));
        }

        return chooserIntent;
    }

    /**
     * This method controls intent list which contains all intents and if you want to know which intent
     * is currently used chech list.
     * Method adds given intent to given list via package manager check https://developer.android.com/reference/android/content/pm/PackageManager.html
     *
     * @param context current context
     * @param list  intent list which have all intents
     * @param intent intent which will be added
     * @return  returns new list
     */
    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            Log.d(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    /**
     * Call this method on on activity result this parameters edited for on activity result.
     * Basicly method detects intent's type from uri string. İf intent belong to camera uses file uri
     * else uses given data parameters uri and returns bitmap.
     *
     * @param context current context
     * @param resultCode resultCode from arguments of onActivityResult
     * @param imageReturnedIntent data from arguments of onActivityResult
     * @return returns bitmap object
     */
    public static Bitmap getImageFromResult(Context context, int resultCode,
                                             Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                selectedImage = Uri.fromFile(imageFile);
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }
            Log.d(TAG, "selectedImage: " + selectedImage);

            bm = getImageResized(context, selectedImage);
            int rotation = getRotation(context, selectedImage, isCamera);
            bm = rotate(bm, rotation);
        }
        return bm;
    }

    /**
     * creates a new file in parent directory
     *
     * @param context current context
     * @return returns new file in parent directory
     */
    private static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        if(!imageFile.getParentFile().mkdirs()){
            Log.d(TAG, "Template file is unable to create");
        }
        return imageFile;
    }

    /**
     * Creates bitmap from given uri
     *
     * @param context current context
     * @param theUri uri from file or data.getData()
     * @param sampleSize size of the new bitmap
     * @return bitmap object which created from uri
     */
    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap actuallyUsableBitmap = null;
        if (fileDescriptor != null) {
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.getFileDescriptor(), null, options);
        }

        if (actuallyUsableBitmap != null) {
            Log.d(TAG, options.inSampleSize + " sample method bitmap ... " +
                    actuallyUsableBitmap.getWidth() + " " + actuallyUsableBitmap.getHeight());
        }

        return actuallyUsableBitmap;
    }

    /**
     * Resizes given uri to bbitmap for given min treshold
     *
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     *
     * @param context current context
     * @param selectedImage uri from file or data.getData()
     * @return returns resized new bitmap object
     */
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            Log.d(TAG, "resizer: new bitmap width = " + bm.getWidth());
            i++;
        } while (bm.getWidth() < DEFAULT_MIN_WIDTH_QUALITY && i < sampleSizes.length);
        return bm;
    }

    /**
     * Detects rotation value for camera or gallery
     *
     * @param context current context
     * @param isCamera boolean value which initialized in getImageFromResult method
     * @return rotation value
     */
    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        Log.d(TAG, "Image rotation: " + rotation);
        return rotation;
    }

    /**
     * Detects rotation value for camera using etif class
     *
     * @param context current context
     * @return returns rotation value
     */
    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    /**
     * Detects rotation value for camera using media file orientation query
     *
     * @param context current context
     * @return returns rotation value
     */
    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    /**
     * Rotates bitmap for given value and creates new bitmap with graphics.matrix class
     *
     * @param bm bitmap which will be rotated
     * @param rotation bitmap's rotation value
     * @return returns rotated bitmap
     */
    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }
}