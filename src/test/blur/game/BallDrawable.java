package test.blur.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

/**
 * 根据需要修改开头的几个static final变量,
 * 关键方法
 * {@link #setAnimationCallback(AnimationCallback)}
 * {@link #start()}
 * */
public class BallDrawable extends Drawable implements Runnable, Animatable {

    //动画时间
    private static final long SPREAD_DURATION = 1000;
    private static final int FALL_DURATION = 500;

    //球的颜色
    private static final int BALL_COLOR_START = 0xffffff00;
    private static final int BALL_COLOR_END = 0xffffff00;
    private static final int BALL_HALO_COLOR = 0x80ffff00;

    // 球的大小
    private static final float BALL_RADIUS_IN_DIP_SMALL = 2;
    private static final float BALL_RADIUS_IN_DIP_MEDIUM = 4;
    private static final float BALL_RADIUS_IN_DIP_BIG = 7;

    public interface AnimationCallback {
        void onAnimationEnd();
        /**
         * 将球最终的坐标放到point里
         * */
        void getEndPoint(PointF point);
    }

    private AnimationCallback mCallback;

    private PointF endPoint = new PointF();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RadialGradient mPopShader;
    private RadialGradient mPopShadowShader;
    private Matrix mShaderMatrix;
    private float mPopDefaultRadius;
    private float mPopDefaultShadowRatio = 1.5f;

    private PointF mCenter = new PointF();
    private float mAmplitude;
    private PointF mFuseTarget = new PointF();

    private float density;

    private int mStep = -1;

    private float g;

    private ArrayList<Ball> pops;
    private ArrayList<Ball> fused = new ArrayList<BallDrawable.Ball>(13);

    private long mSpreadStartTime = -1;

    public BallDrawable(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        mPopDefaultRadius = density * 10;
        mPopShader = new RadialGradient(0, 0, mPopDefaultRadius, BALL_COLOR_START, BALL_COLOR_END, TileMode.CLAMP);
        mPopShadowShader = new RadialGradient(0, 0, mPopDefaultRadius * mPopDefaultShadowRatio, new int[] {BALL_HALO_COLOR, BALL_HALO_COLOR, BALL_HALO_COLOR & 0x00ffffff}, new float[]{0, 1f / mPopDefaultShadowRatio, 1f}, TileMode.CLAMP);
        mShaderMatrix = new Matrix();
    }

    public void setAnimationCallback(AnimationCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void start() {
        mStep = 0;
        mSpreadStartTime = -1;
        invalidateSelf();
    }

    @Override
    public void stop() {
        mStep = 4;
        scheduleSelf(this, SystemClock.uptimeMillis());
    }

    @Override
    public boolean isRunning() {
        return mStep >= 0 && mStep <= 3;
    }

    @Override
    public void run() {
        if (mCallback != null) {
            mCallback.onAnimationEnd();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mStep <= 2) {
            canvas.drawColor(0x80000000);
        }

        if (mStep < 0 || mStep > 3) {
            return;
        }

        if (mStep == 0) {
            long now = System.currentTimeMillis();
            if (mSpreadStartTime == -1) {
                mSpreadStartTime = now;
            }
            long passed = now - mSpreadStartTime;
            float ratio = passed * 1.0f / SPREAD_DURATION;
            if (ratio <= 0) {
                invalidateSelf();
                return;
            } else if (ratio >= 1f) {
                ratio = 1f;
                ++mStep;
            }
            float moveRatio = (float)(Math.cos((ratio + 1) * Math.PI) / 2.0f) + 0.5f; // accelerate-decelerate
            float scaleRatio = 1 - (1 - ratio) * (1 - ratio); // decelerate
            int count = pops.size();
            for (int i = 0; i < count; i++) {
                Ball p = pops.get(i);
                drawPop(canvas, mCenter.x + (p.position.x - mCenter.x) * moveRatio, mCenter.y + (p.position.y - mCenter.y) * moveRatio, p.radius * scaleRatio);
            }
            invalidateSelf();
        } else if (mStep == 1) {
            boolean more = false;
            int count = pops.size();
            for (int i = 0; i < count; i++) {
                Ball p = pops.get(i);
                more |= p.update();
            }
            computeFuse();
            count = pops.size();
            for (int i = 0; i < count; i++) {
                Ball p = pops.get(i);
                drawPop(canvas, p.position.x, p.position.y, p.radius);
            }
            if (!more) {
                ++mStep;
                Ball finalPop;
                if (pops.size() == 0) {
                    finalPop = new Ball(mFuseTarget.x, mFuseTarget.y, BALL_RADIUS_IN_DIP_BIG * 1.5f);
                    pops.add(finalPop);
                } else {
                    finalPop = pops.get(0);
                }
                finalPop.velocity.set(0, (float) (-Math.sqrt(mAmplitude / 2f * g)));
            }
            invalidateSelf();
        } else if (mStep == 2) {
            Ball finalPop = pops.get(0);
            finalPop.position.offset(finalPop.velocity.x, finalPop.velocity.y);
            finalPop.velocity.y += g;
            if (finalPop.position.y + finalPop.radius >= mCenter.y + mAmplitude) {
                finalPop.position.y = mCenter.y + mAmplitude - finalPop.radius;
                ++mStep;
                if (mCallback != null) {
                    mCallback.getEndPoint(endPoint);
                }
                endPoint.y = Math.max(endPoint.y, finalPop.radius);
                float t = (float) (Math.sqrt(2 * finalPop.position.y / g) + Math.sqrt(2 * endPoint.y / g));
                finalPop.velocity.set((endPoint.x - finalPop.position.x) / t, -(float) (Math.sqrt(2 * g * finalPop.position.y)));
            }
            drawPop(canvas, finalPop.position.x, finalPop.position.y, finalPop.radius);
            invalidateSelf();
        } else if (mStep == 3) {
            Ball finalPop = pops.get(0);
            finalPop.position.offset(finalPop.velocity.x, finalPop.velocity.y);
            finalPop.velocity.y += g;
            float ratio = finalPop.velocity.y < 0 ? 0 : Math.max(finalPop.position.y, 0) / endPoint.y;
            if (ratio >= 1f) {
                ratio = 1f;
                finalPop.position.set(endPoint);
                ++mStep;
                scheduleSelf(this, SystemClock.uptimeMillis());
            } else {
                invalidateSelf();
            }
            finalPop.radius *= 1 - ratio;
            canvas.drawColor(Color.argb((int) (0x80 * (1 - ratio)), 0, 0, 0));
            drawPop(canvas, finalPop.position.x, finalPop.position.y, finalPop.radius);
        }
    }

    private void computeFuse() {
        int count = pops.size();
        if (count == 0) {
            return;
        }
        for (int i = count - 1; i >=0; i--) {
            Ball p = pops.get(i);
            if (!p.valid) {
                continue;
            }
            for (int j = i - 1; j >= 0; j--) {
                Ball next = pops.get(j);
                Ball fuse = p.fuse(next);
                if (fuse != null) {
                    fused.add(fuse);
                    break;
                }
            }
            if (p.valid) {
                fused.add(p);
            }
        }
        pops.clear();
        pops.addAll(fused);
        fused.clear();
        Collections.sort(pops);
    }

    @Override
    public void setAlpha(int alpha) {
        //
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        //
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mCenter.set(bounds.exactCenterX(), bounds.exactCenterY());
        mAmplitude = Math.min(bounds.width(), bounds.height()) / 4f;
        mFuseTarget.set(mCenter.x, mCenter.y - mAmplitude / 4f);
        if (pops == null && bounds.width() != 0 && bounds.height() != 0) {
            pops = new ArrayList<BallDrawable.Ball>(13);
            Random random = new Random();
            pops.add(new Ball(mCenter.x - 8 * density, mCenter.y - 5 * density, BALL_RADIUS_IN_DIP_SMALL * density));
            for (int i = 0; i < 12; i++) {
                float x = (2 * (i % 4) + 1) * (mAmplitude / 4f) - mAmplitude;
                float y = (2 * (i / 4 + 1) + 1) * (mAmplitude / 4f) - mAmplitude;
                float theta = (float) (random.nextFloat() * Math.PI * 2f);
                float r = random.nextFloat() * mAmplitude / 4f;
                x += r * Math.cos(theta);
                y += r * Math.sin(theta);
                pops.add(new Ball(mCenter.x + x, mCenter.y + y, i % 2 == 0 ? (random.nextFloat() * (BALL_RADIUS_IN_DIP_MEDIUM - BALL_RADIUS_IN_DIP_SMALL) + BALL_RADIUS_IN_DIP_SMALL) * density
                        : (random.nextFloat() * (BALL_RADIUS_IN_DIP_BIG - BALL_RADIUS_IN_DIP_MEDIUM) + BALL_RADIUS_IN_DIP_MEDIUM) * density));
            }
            Collections.sort(pops);
        }
        float t = FALL_DURATION / 17f;
        g = 2 * mAmplitude / (t * t);
    }

    private void drawPop(Canvas canvas, float x, float y, float radius) {
        float scale = radius / mPopDefaultRadius;
        mShaderMatrix.setScale(scale, scale);
        mPopShadowShader.setLocalMatrix(mShaderMatrix);
        mPopShader.setLocalMatrix(mShaderMatrix);
        canvas.translate(x, y);
        mPaint.setShader(mPopShadowShader);
        canvas.drawCircle(0, 0, radius * mPopDefaultShadowRatio, mPaint);
        mPaint.setShader(mPopShader);
        canvas.drawCircle(0, 0, radius, mPaint);
        canvas.translate(-x, -y);
    }

    private class Ball implements Comparable<Ball> {
        PointF position = new PointF();
        PointF velocity = new PointF();
        float radius;
        float fromRadius, toRadius;
        boolean valid = true;

        int bounceCount = 0;
        boolean readyFuse = false;

        private Ball() {}

        Ball(float tarX, float tarY, float radius) {
            position.set(tarX, tarY);
            this.radius = radius;
        }

        Ball fuse(Ball other) {
            if (readyFuse && other.readyFuse && PointF.length(position.x - other.position.x, position.y - other.position.y) < Math.abs(radius - other.radius)) {
                Ball fused = new Ball();
                float v1 = radius * radius * radius;
                float v2 = other.radius * other.radius * other.radius;
                fused.toRadius = (float) Math.pow(v1 + v2, 1 / 3f);
                Ball bigger = radius > other.radius ? this : other;
                fused.fromRadius = bigger.radius;
                fused.radius = fused.toRadius;
                fused.position.set(bigger.position);
                this.valid = false;
                other.valid = false;
                float e1 = this.velocity.length();
                float e2 = other.velocity.length();
                float e = (e1 * v1 + e2 * v2) / (v1 + v2);
                float x = mFuseTarget.x - fused.position.x;
                float y = mFuseTarget.y - fused.position.y;
                float l = PointF.length(x, y);
                if (l != 0) {
                    fused.velocity.set(x / l * e, y / l * e);
                }
                fused.readyFuse = true;
                return fused;
            }
            return null;
        }

        @Override
        public int compareTo(Ball another) {
            return this.radius > another.radius ? 1 : this.radius == another.radius ? 0 : -1;
        }

        boolean update() {
            position.offset(velocity.x, velocity.y);
            if (!readyFuse) {
                velocity.y += g;
                if (position.y + radius > mCenter.y + mAmplitude) {
                    position.y = mCenter.y + mAmplitude - radius;
                    if (bounceCount == 0) {
                        velocity.y *= -1;
                        ++bounceCount;
                    } else {
                        float e = Math.abs(velocity.y);
                        float x = mFuseTarget.x - position.x;
                        float y = mFuseTarget.y - position.y;
                        float l = PointF.length(x, y);
                        if (l == 0) {
                            velocity.set(0, 0);
                        } else {
                            e = Math.max(e, l / 1000f * 17f);
                            velocity.set(x / l * e, y / l * e);
                        }
                        readyFuse = true;
                    }
                }
            } else {
                if (position.y <= mFuseTarget.y) {
                    position.set(mFuseTarget);
                    velocity.set(0, 0);
                    return false;
                }
            }
            return true;
        }

    }
}
