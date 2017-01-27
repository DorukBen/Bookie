package com.karambit.bookie.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

import com.karambit.bookie.R;

/**
 * Created by doruk on 27.01.2017.
 */

public class CurvyDrawable extends Drawable {

    private RectF mBounds;
    private float mWidth;
    private float mHeight;
    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private final Paint mPaint = new Paint();
    private final float mMaxAngle = (float) (180f * .85);
    private float mLineLength;
    private float mErrorLineLength;
    private float mArrowXSpace;
    private float mArrowYSpace;
    private float mDegrees;

    private float mPercent = 1.0f;

    private boolean mIsErrorOccurred = false;

    public CurvyDrawable (Context context) {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(dp2px(2, context));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(context, R.color.progressBarColor));

        mWidth = dp2px(56, context);
        mHeight = dp2px(56, context);

        mRadius = dp2px(12, context);

        mLineLength = (float) (Math.PI / 180 * mMaxAngle * mRadius);

        mErrorLineLength = (int) (mLineLength * .72);
        float mArrowLength = (int) (mLineLength * .15);
        float mArrowAngle = (float) (Math.PI / 180 * 25);
        mArrowXSpace = (int) (mArrowLength * Math.sin(mArrowAngle));
        mArrowYSpace = (int) (mArrowLength * Math.cos(mArrowAngle));
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBounds = new RectF(bounds.width() / 2 - mWidth / 2, bounds.top - mHeight / 2, bounds.width() / 2 + mWidth / 2, bounds.top + mHeight / 2);
        mCenterX = mBounds.centerX();
        mCenterY = mBounds.centerY();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        canvas.save();
        canvas.clipRect(mBounds);


        if (!mIsErrorOccurred){

            canvas.rotate(mDegrees, mCenterX, mCenterY);
            mDegrees = mDegrees < 360 ? mDegrees + 10 : 0;
            invalidateSelf();


            // left
            float leftX = mCenterX - mRadius;
            float leftY = mCenterY;

            // right
            float rightX = mCenterX + mRadius;
            float rightY = mCenterY;

            RectF oval = new RectF(leftX, mCenterY - mRadius, rightX, mCenterY + mRadius);

            canvas.drawArc(oval, 180, mMaxAngle, false, mPaint);


            canvas.drawArc(oval, 0, mMaxAngle, false, mPaint);

            // arrow
            canvas.save();

            canvas.rotate(mMaxAngle, mCenterX, mCenterY);

            // left arrow
            canvas.drawLine(leftX, leftY, leftX - mArrowXSpace, leftY + mArrowYSpace, mPaint);

            // right arrow
            canvas.drawLine(rightX, rightY, rightX + mArrowXSpace, rightY - mArrowYSpace, mPaint);

            canvas.restore();
        }else{
            if (mDegrees != 20.0f){
                canvas.rotate(mDegrees, mCenterX, mCenterY);
                mDegrees = mDegrees < 360 ? mDegrees + 10 : 0;
                invalidateSelf();


                // left
                float leftX = mCenterX - mRadius;
                float leftY = mCenterY;

                // right
                float rightX = mCenterX + mRadius;
                float rightY = mCenterY;

                RectF oval = new RectF(leftX, mCenterY - mRadius, rightX, mCenterY + mRadius);

                canvas.drawArc(oval, 200, mMaxAngle, false, mPaint);


                canvas.drawArc(oval, 20, mMaxAngle, false, mPaint);

                // arrow
                canvas.save();

                canvas.rotate(mMaxAngle, mCenterX, mCenterY);

                // left arrow
                canvas.drawLine(leftX, leftY, leftX - mArrowXSpace, leftY + mArrowYSpace, mPaint);

                // right arrow
                canvas.drawLine(rightX, rightY, rightX + mArrowXSpace, rightY - mArrowYSpace, mPaint);

                canvas.restore();
            }else{
                if (mPercent >= 0.0f){
                    // left
                    float leftX = mCenterX - mRadius;
                    float leftY = mCenterY;

                    // right
                    float rightX = mCenterX + mRadius;
                    float rightY = mCenterY;

                    RectF oval = new RectF(leftX, mCenterY - mRadius, rightX, mCenterY + mRadius);

                    canvas.drawLine(leftX, leftY, leftX + mErrorLineLength - mErrorLineLength * mPercent, leftY - mErrorLineLength + mErrorLineLength * mPercent, mPaint);

                    canvas.drawArc(oval, 200 + (mMaxAngle - mMaxAngle * mPercent), mMaxAngle * mPercent, false, mPaint);



                    canvas.drawLine(rightX, rightY, rightX - mErrorLineLength + mErrorLineLength * mPercent, rightY - mErrorLineLength + mErrorLineLength * mPercent, mPaint);

                    canvas.drawArc(oval, 20 + (mMaxAngle - mMaxAngle * mPercent), mMaxAngle * mPercent, false, mPaint);

                    // arrow
                    canvas.save();

                    // left arrow
                    //canvas.drawLine(leftX, leftY, leftX - mArrowXSpace * mPercent, leftY + mArrowYSpace * mPercent, mPaint);

                    // right arrow
                    //canvas.drawLine(rightX, rightY, rightX + mArrowXSpace * mPercent, rightY - mArrowYSpace * mPercent, mPaint);

                    canvas.restore();

                    mPercent -= 0.04f;
                }else{
                    // left
                    float leftX = mCenterX - mRadius;
                    float leftY = mCenterY;

                    // right
                    float rightX = mCenterX + mRadius;
                    float rightY = mCenterY;

                    RectF oval = new RectF(leftX, mCenterY - mRadius, rightX, mCenterY + mRadius);

                    canvas.drawLine(leftX, leftY, leftX + mErrorLineLength, leftY - mErrorLineLength, mPaint);

                    canvas.drawLine(rightX, rightY, rightX - mErrorLineLength, rightY - mErrorLineLength, mPaint);

                    // arrow
                    canvas.save();

                    canvas.restore();
                }

            }
        }

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public void setIsErrorOccurred(boolean errorOccurred){
        if (errorOccurred && mPercent <= 0.0f){
            mPercent = 1.0f;
        }
        mIsErrorOccurred = errorOccurred;
    }

    public boolean getIsErrorOccured(){
        return mIsErrorOccurred;
    }
}
