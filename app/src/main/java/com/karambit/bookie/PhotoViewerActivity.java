package com.karambit.bookie;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class PhotoViewerActivity extends AppCompatActivity implements View.OnTouchListener {

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
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

    private float mImageWidth;
    private float mImageHeight;

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

        Glide.with(PhotoViewerActivity.this)
                .load(getIntent().getStringExtra("image"))
                .asBitmap()
                .into(new BitmapImageViewTarget(mPhotoView) {
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
                        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                        mPhotoView.setImageMatrix(matrix);
                    }
                });

        mPhotoView.setOnTouchListener(this);

        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
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
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
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
                    matrix.set(savedMatrix);
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

                    matrix.postTranslate(dx, dy);

                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
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


                            matrix.postTranslate(deltaX, deltaY);
                            matrix.postScale(scale, scale, size.x / 2, size.y / 2);

                        }else {
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }

                    }
                }
                break;
        }

        view.setImageMatrix(matrix);

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
}
