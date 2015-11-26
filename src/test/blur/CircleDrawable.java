
package test.blur;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class CircleDrawable extends Drawable {

    private float[] mFrames = {5000 / 17f, 1000 / 17f, 1500 / 17f};

    private float mFrameIndex = 0;

    private float mTotalDegrees = 180 + 60 * 360;

    private float mStartAngle = 120;

    private float mCurAngle = mStartAngle;

    private float mSwipeAngle = 300;

    private Interpolator mRotateInterpolator;

    private boolean isHaloing = false;

    private boolean isAnimating = false;

    private float mCurScale = 1.0f;

    private float mCurCircleWidth = 300.0f;

    private float mCurCircleAlpha = 0.6f;

    private Paint mPaint;

    private RectF mRectF = new RectF();

    private float mRadius = 250;

    private int[] mColorSet = {0xff54d5e8, 0xfff5d11b};

    private int mColor = mColorSet[1];

    private float mStartRatio = 5 / 6f;

    private float mEndRatio = 1 / 6f;

    private float mRatio;

    private int mStep = 0;

    public CircleDrawable(View host) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Cap.ROUND);
        mRotateInterpolator = new AccelerateDecelerateInterpolator();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();

        if (mCurScale != 1f) {
            canvas.save();
            canvas.scale(mCurScale, mCurScale, cx, cy);
        }

        // draw background circles begin
        mPaint.setColor(0x4067706f);
        mPaint.setStrokeWidth(120);
        canvas.drawCircle(cx, cy, mRadius + 10, mPaint);

        mPaint.setColor(0xff67706f);
        mPaint.setStrokeWidth(100);
        canvas.drawCircle(cx, cy, mRadius, mPaint);
        // draw background circles end

        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(40);
        mRectF.set(cx - mRadius, cy - mRadius, cx + mRadius, cy + mRadius);
        canvas.drawArc(mRectF, mCurAngle - mSwipeAngle / 2, mSwipeAngle, false, mPaint);

        if (isHaloing) {
            mPaint.setColor(Color.WHITE);
            mPaint.setAlpha((int) (255 * mCurCircleAlpha));
            mPaint.setStrokeWidth(30);
            canvas.drawCircle(cx, cy, mCurCircleWidth, mPaint);
        }

        if (mCurScale != 1f) {
            canvas.restore();
        }

        refreshUi();
    }

    private void refreshUi() {
        if (!isAnimating) {
            return;
        }

        float frames = mFrames[mStep];
        float normalized = mFrameIndex / frames;
        if (normalized > 1f) {
            normalized = 1f;
        }
        mFrameIndex += 1;

        if (mStep == 0) {
            mCurAngle = mStartAngle + mTotalDegrees * mRotateInterpolator.getInterpolation(normalized);
            mSwipeAngle = 300 - 240 * normalized;

            if (normalized > 0.5f) {
                mRatio = mStartRatio + (mEndRatio - mStartRatio) * (normalized - 0.5f) / 0.5f;
                mColor = getColor(mRatio);
            } else {
                mColor = getColor(mStartRatio);
            }

            if (normalized > 0.5f && normalized < 0.8f) {
                mCurScale = 0.6f + 0.4f * Math.abs((normalized - 0.5f) / 0.3f - 0.5f) / 0.5f;
            } else {
                mCurScale = 1f;
            }

            if (normalized > 0.75f && normalized < 0.9f) {
                isHaloing = true;
                float normalizeForRing = (normalized - 0.75f) / 0.15f;
                float ratio = normalizeForRing >= 0.5f ? (normalizeForRing - 0.5f) / 0.5f : normalizeForRing / 0.5f;
                mCurCircleWidth = 300 + 200 * ratio;
                mCurCircleAlpha = 1 - ratio;
            } else {
                isHaloing = false;
            }
        } else if (mStep == 1) {
            float ratio = -1.8633f * normalized * normalized + 2.8633f * normalized;
            mCurAngle = 300 - 60 * ratio;
        } else {
            float ratio = normalized > 0.5f ? (normalized - 0.5f) / 0.5f : 0;
            mCurAngle = 240 - 120 * ratio;
            mSwipeAngle = 60 + 240 * ratio;
            mRatio = mEndRatio + (mStartRatio - mEndRatio) * ratio;
            mColor = getColor(ratio);
        }

        if (normalized < 1f) {
            invalidateSelf();
        } else {
            if (mStep < 2) {
                mStep++;
                mFrameIndex = 0;
                invalidateSelf();
            } else {
                isAnimating = false;
            }
        }
    }

    private int getColor(float ratio) {
        if (ratio <= 0.25f) {
            return mColorSet[0];
        } else if (ratio > 0.75f) {
            return mColorSet[1];
        } else {
            float colorRatio = (ratio - 0.25f) / 0.5f;
            return Color.argb(0xff,
                    (int) (Color.red(mColorSet[0]) + (Color.red(mColorSet[1]) - Color.red(mColorSet[0])) * colorRatio),
                    (int) (Color.green(mColorSet[0]) + (Color.green(mColorSet[1]) - Color.green(mColorSet[0])) * colorRatio),
                    (int) (Color.blue(mColorSet[0]) + (Color.blue(mColorSet[1]) - Color.blue(mColorSet[0])) * colorRatio));
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void startAnimation() {
        if (isAnimating) {
            return;
        }
        isAnimating = true;
        mStep = 0;
        mFrameIndex = 0;
        invalidateSelf();

    }

    public void stopAnimation() {
        if (!isAnimating) {
            return;
        }
        isAnimating = false;
    }
}
