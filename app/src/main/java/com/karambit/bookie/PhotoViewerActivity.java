package com.karambit.bookie;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.karambit.bookie.helper.FileNameGenerator;
import com.karambit.bookie.helper.ImagePicker;
import com.karambit.bookie.helper.ImageScaler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.helper.UploadFileTask;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoViewerActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int REQUEST_CODE_CUSTOM_PERMISSIONS = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    public static final int RESULT_PROFILE_PICTURE_UPDATED = 3;


    private static final String UPLOAD_USER_IMAGE_URL = "ProfilePictureUpload";
    private static final String UPLOAD_BOOK_IMAGE_URL = "BookUpdatePicture";

    public static final String EXTRA_USER = "user";
    public static final String EXTRA_CAN_EDIT_BOOK_IMAGE = "can_edit_book_image";
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_BOOK_ID = "book_id";

    // these matrices will be used to move and zoom image
    private Matrix mMatrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private ImageView mPhotoView;
    private ImageView mEditPhotoView;

    private float mImageWidth;
    private float mImageHeight;
    private ImageButton mEditPhotoButton;
    private ImageButton mCloseButton;
    private ImageButton mUploadButton;

    private File mSavedUserImageFile;
    private File mSavedBookImageFile;

    private static final String TAG = PhotoViewerActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        if (getSupportActionBar() != null){
            getSupportActionBar().setShowHideAnimationEnabled(false);
            getSupportActionBar().hide();

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }

        mPhotoView = (ImageView) findViewById(R.id.profilePictureFullSize);
        mEditPhotoView = (ImageView) findViewById(R.id.editProfilePictureFullSize);
        mEditPhotoButton = (ImageButton) findViewById(R.id.editButton);
        mCloseButton = (ImageButton) findViewById(R.id.closeButton);
        mUploadButton = (ImageButton) findViewById(R.id.uploadButton);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mUploadButton.getLayoutParams();
        Point size = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(size);
        params.setMargins(0, 0, (int)convertDpToPixel(16, PhotoViewerActivity.this), (size.y - size.x) / 2 - (int)convertDpToPixel(28, PhotoViewerActivity.this));

        mUploadButton.setLayoutParams(params);

        bringFrontPhotoForGlide();

        User photoUser = getIntent().getParcelableExtra(EXTRA_USER);
        User currentUser =  SessionManager.getCurrentUser(this);

        if (photoUser != null && photoUser.equals(currentUser) || getIntent().getBooleanExtra(EXTRA_CAN_EDIT_BOOK_IMAGE, false)){
            mEditPhotoButton.setVisibility(View.VISIBLE);

            mEditPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkPermissions()) {
                        startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), REQUEST_CODE_SELECT_IMAGE);

                        Log.i(TAG, "Permissions OK!");
                    } else {
                        String[] permissions = {
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        };

                        ActivityCompat.requestPermissions(PhotoViewerActivity.this, permissions, REQUEST_CODE_CUSTOM_PERMISSIONS);

                        Log.i(TAG, "Permissions NOT OK!");
                    }
                }
            });

            mUploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getIntent().getBooleanExtra(EXTRA_CAN_EDIT_BOOK_IMAGE, false)){
                        attemptUploadBookPicture();
                    }else {
                        attemptUploadProfilePicture();
                    }
                }
            });
        }else {
            mEditPhotoButton.setVisibility(View.GONE);
        }


        Glide.with(PhotoViewerActivity.this)
                .load(getIntent().getStringExtra(EXTRA_IMAGE))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(ContextCompat.getDrawable(this, R.drawable.error_192dp))
                .into(new BitmapImageViewTarget(mPhotoView) {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Bitmap resource = drawableToBitmap(errorDrawable);

                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                        mImageWidth = resource.getWidth();
                        mImageHeight = resource.getHeight();

                        Point size = new Point();
                        getWindow().getWindowManager().getDefaultDisplay().getSize(size);
                        RectF drawableRect = new RectF(0, 0, resource.getWidth(), resource.getHeight());
                        RectF viewRect = new RectF(0, 0, size.x, size.y);
                        mMatrix = new Matrix();
                        mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                        mPhotoView.setImageMatrix(mMatrix);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);

                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                        mImageWidth = resource.getWidth();
                        mImageHeight = resource.getHeight();

                        Point size = new Point();
                        getWindow().getWindowManager().getDefaultDisplay().getSize(size);
                        RectF drawableRect = new RectF(0, 0, resource.getWidth(), resource.getHeight());
                        RectF viewRect = new RectF(0, 0, size.x, size.y);
                        mMatrix = new Matrix();
                        mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                        mPhotoView.setImageMatrix(mMatrix);
                    }
                });

        mPhotoView.setOnTouchListener(this);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        ImageView view = (ImageView) v;

        float[] tempValues = new float[9];
        savedMatrix.getValues(tempValues);
        float globalX = tempValues[Matrix.MTRANS_X];
        float globalY = tempValues[Matrix.MTRANS_Y];
        float width = tempValues[Matrix.MSCALE_X] * mImageWidth;
        float height = tempValues[Matrix.MSCALE_Y] * mImageHeight;

        Point size = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(size);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(mMatrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(mMatrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    mMatrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;

                    if (globalX + dx > 0){
                        dx = -globalX;
                    }else if (globalX + dx + width < size.x){
                        dx = size.x -(globalX + width);
                    }

                    float yTop = (size.y - height) / 2;
                    float yBot = size.y - (size.y - height) / 2;

                    if (globalY + dy > yTop && yTop > 0 ){
                        dy = yTop - globalY;
                    }else if (globalY + height + dy < yBot && yBot < size.y){
                        dy = yBot - (globalY + height);
                    }

                    if (globalY + dy > 0 && yTop < 0){
                        dy = -globalY;
                    }else if (globalY + height + dy < size.y && yBot > size.y){
                        dy = size.y - (globalY + height);
                    }

                    mMatrix.postTranslate(dx, dy);

                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        mMatrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        if (width * scale < size.x){
                            scale = size.x / width;
                        }else if ((width * scale) > size.x * 3){
                            scale = size.x / width * 3;
                        }
                        if (scale < 1){

                            float totalFlowX = width - size.x;
                            float totalFlowY = height - mImageHeight;
                            float leftFlowX = Math.abs(globalX);
                            float rigthFlowX = totalFlowX - leftFlowX;
                            float topFlowY;
                            if (globalY < 0){
                                topFlowY = Math.abs(globalY) + (size.y - mImageHeight) / 2;
                            }else{
                                topFlowY = (size.y - mImageHeight) / 2 - Math.abs(globalY);
                            }
                            float botFlowY = totalFlowY - topFlowY;

                            float deltaX;
                            float deltaY;
                            deltaX = (leftFlowX - rigthFlowX) / 2;
                            deltaY = (topFlowY - botFlowY) / 2;


                            mMatrix.postTranslate(deltaX, deltaY);
                            mMatrix.postScale(scale, scale, size.x / 2, size.y / 2);

                        }else {
                            mMatrix.postScale(scale, scale, mid.x, mid.y);
                        }

                    }
                }
                break;
        }

        view.setImageMatrix(mMatrix);

        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = ImagePicker.getImageFromResult(getBaseContext(), resultCode, data);
                Bitmap croppedBitmap = ImageScaler.cropImage(bitmap, 72 / 72f);

                if (getIntent().getBooleanExtra(EXTRA_CAN_EDIT_BOOK_IMAGE, false)){
                    croppedBitmap = ImageScaler.cropImage(bitmap, 72 / 96f);
                }

                savedMatrix = new Matrix();
                savedMatrix.reset();
                mMatrix = new Matrix();
                mode = NONE;
                mid = new PointF();
                start = new PointF();
                oldDist = 1f;
                d = 0f;
                newRot = 0f;

                bringFrontPhotoForEdit();
                mEditPhotoView.setImageBitmap(croppedBitmap);
                if (getIntent().getBooleanExtra(EXTRA_CAN_EDIT_BOOK_IMAGE, false)){
                    mSavedBookImageFile = saveBitmap(croppedBitmap);
                }else {
                    mSavedUserImageFile = saveBitmap(croppedBitmap);
                }


                mImageWidth = croppedBitmap.getWidth();
                mImageHeight = croppedBitmap.getHeight();

                Point size = new Point();
                getWindow().getWindowManager().getDefaultDisplay().getSize(size);
                RectF drawableRect = new RectF(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight());
                RectF viewRect = new RectF(0, 0, size.x, size.y);
                mMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                mEditPhotoView.setImageMatrix(mMatrix);
            }
        }
    }

    private File saveBitmap(Bitmap bitmap) {

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.app_name) + File.separator;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        String imageName = FileNameGenerator.generateBookImageName(SessionManager.getCurrentUserDetails(this).getEmail());

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path + imageName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new File(path + imageName);
    }

    private boolean checkPermissions() {

        return !(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CUSTOM_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivityForResult(ImagePicker.getPickImageIntent(getBaseContext()), 1);

                } else {
                    Toast.makeText(this, R.string.permission_request_message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void bringFrontPhotoForGlide(){
        mPhotoView.setVisibility(View.VISIBLE);
        mPhotoView.bringToFront();
        mEditPhotoView.setVisibility(View.GONE);
        mUploadButton.setVisibility(View.GONE);
        mCloseButton.bringToFront();
        mEditPhotoButton.bringToFront();
    }

    private void bringFrontPhotoForEdit(){
        mEditPhotoView.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.VISIBLE);
        mEditPhotoView.bringToFront();
        mPhotoView.setVisibility(View.GONE);
        mCloseButton.bringToFront();
        mEditPhotoButton.bringToFront();
    }

    private void attemptUploadProfilePicture() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.uploading_image));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        // Upload Book image
        String path = mSavedUserImageFile.getPath();
        String name = mSavedUserImageFile.getName();

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String serverArgsString = "?email=" + currentUserDetails.getEmail()
                + "&password=" + currentUserDetails.getPassword()
                + "&imageName=" + name;

        String imageUrlString = BookieClient.BASE_URL + UPLOAD_USER_IMAGE_URL + serverArgsString;


        final UploadFileTask uftImage = new UploadFileTask(path, imageUrlString, name);

        uftImage.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Log.i(TAG, "Image upload progress => " + progress + " / 100");
            }

            @Override
            public void onProgressCompleted() {
                Log.w(TAG, "Image upload is OK");
                mProgressDialog.dismiss();
                setResult(RESULT_PROFILE_PICTURE_UPDATED);
                finish();
            }

            @Override
            public void onProgressError() {
                mProgressDialog.dismiss();
                Log.e(TAG, "Image upload ERROR");
                Toast.makeText(PhotoViewerActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });

        uftImage.execute();
    }

    private void attemptUploadBookPicture() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.uploading_image));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        // Upload Book image
        String path = mSavedBookImageFile.getPath();
        String name = mSavedBookImageFile.getName();

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String serverArgsString = "?email=" + currentUserDetails.getEmail()
                + "&password=" + currentUserDetails.getPassword()
                + "&imageName=" + name
                + "&bookID=" + getIntent().getIntExtra(EXTRA_BOOK_ID,-1);

        String imageUrlString = BookieClient.BASE_URL + UPLOAD_BOOK_IMAGE_URL + serverArgsString;


        final UploadFileTask uftImage = new UploadFileTask(path, imageUrlString, name);

        uftImage.setUploadProgressListener(new UploadFileTask.UploadProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Log.i(TAG, "Image upload progress => " + progress + " / 100");
            }

            @Override
            public void onProgressCompleted() {
                Log.w(TAG, "Image upload is OK");
                mProgressDialog.dismiss();

                /*
                    TODO Update Current user on database.
                    This may be done in another Profile Activity or Main Activity
                    Just check DBHelper for update current profile picture or update all the user details
                  */

                setResult(RESULT_PROFILE_PICTURE_UPDATED);
                finish();
            }

            @Override
            public void onProgressError() {
                mProgressDialog.dismiss();
                Log.e(TAG, "Image upload ERROR");
                Toast.makeText(PhotoViewerActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        });

        uftImage.execute();
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
