package test.blur.game;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class PopDrawable extends Drawable implements Runnable, Animatable {

    private static final int FALL_DURATION = 1000;
    private static final int SHAKE_DURATION = 1000;
    private static final int FLOAT_DURATION = 5000;
    private static final float SHAKE_FADE_EFFECT = 0.75f;
    private static final float FLOAT_FADE_EFFECT = 0.85f;

    private static final int COLOR_BLUE = 0xd621c7ff;
    private static final int COLOR_RED = 0xd6ff116a;
    private static final int COLOR_YELLOW = 0xd6ffb400;

    private Pop blue = new Pop(COLOR_BLUE);
    private Pop red = new Pop(COLOR_RED);
    private Pop yellow = new Pop(COLOR_YELLOW);

    private Paint mPaint;
    private RectF oval = new RectF();

    private boolean isRunning;
    private float g;

    private Random mRandom;

    public PopDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mRandom = new Random(47);
    }

    @Override
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
    }

    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        unscheduleSelf(this);
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        int width = bounds.width();
        int height = bounds.height();
        float radius = width * 0.55f;
        blue.oval.set(width * 0.35f, width * 0.2f - radius, width * 0.35f + 2 * radius, width * 0.2f + radius);
        radius = width * 0.8f;
        red.oval.set(width * 0.1f - radius, -radius, width * 0.1f + radius, radius);
        radius = width * 0.6f;
        yellow.oval.set(width * 0.2f - radius, width * -0.1f - radius, width * 0.2f + radius, width * -0.1f + radius);

        red.offset.set(0, -red.oval.bottom);
        blue.offset.set(0, -blue.oval.bottom);
        yellow.offset.set(0, -yellow.oval.bottom);

        red.state = 0;
        blue.state = 0;
        yellow.state = 0;

        float t = FALL_DURATION / 17f;
        g = height / (t * t);

        t = SHAKE_DURATION / 17f;
        float k = (float) (4 * Math.PI * Math.PI / (t * t) * width * height * height / 8f);
        blue.setShake(k);
        red.setShake(k);
        yellow.setShake(k);

        t = FLOAT_DURATION / 17f;
        k = (float) (4 * Math.PI * Math.PI / (t * t) * width * height * height / 8f);
        blue.setFloat(k);
        red.setFloat(k);
        yellow.setFloat(k);
    }

    @Override
    public void run() {
        float v = (float) Math.sqrt(2 * g * getBounds().height() / 2f) * 0.1f;
        blue.triggerFloat(mRandom, v);
        red.triggerFloat(mRandom, v);
        yellow.triggerFloat(mRandom, v);
        invalidateSelf();
        if (isRunning) {
            scheduleSelf(this, SystemClock.uptimeMillis() + FLOAT_DURATION + 3000);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        /*
        canvas.drawColor(0x80ffffff);*/

        if (!isRunning) {
            return;
        }

        boolean more = false;
        more |= red.draw(canvas, mPaint, oval);
        more |= yellow.draw(canvas, mPaint, oval);
        more |= blue.draw(canvas, mPaint, oval);

        if (more) {
            invalidateSelf();
        } else if (isRunning) {
            unscheduleSelf(this);
            scheduleSelf(this, SystemClock.uptimeMillis() + 1000);
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

    private class Pop {
        int color;
        RectF oval = new RectF();
        PointF offset = new PointF();
        PointF velocity = new PointF();
        int state = 0;
        float ws, wf;
        boolean wStart = true;

        Pop (int color) {
            this.color = color;
        }

        void setShake(float k) {
            float m = oval.width() * oval.width() * oval.height() / 8f;
            if (m == 0) {
                ws = 0;
            } else {
                ws = k / m;
            }
        }

        void setFloat(float k) {
            float m = oval.width() * oval.width() * oval.height() / 8f;
            if (m == 0) {
                wf = 0;
            } else {
                wf = k / m;
            }
        }

        boolean draw(Canvas canvas, Paint paint, RectF oval) {
            paint.setColor(color);
            oval.set(this.oval);
            oval.offset(offset.x, offset.y);
            canvas.drawOval(oval, paint);
            return update();
        }

        private boolean update() {
            if (state == 0) {
                velocity.y += g;
                offset.y += velocity.y;
                if (offset.y > 0) {
                    offset.y = 0;
                    state = 1;
                }
                return true;
            } else if (state == 1) {
                boolean ret = bounceWith(ws, SHAKE_FADE_EFFECT);
                if (!ret) {
                    state = 2;
                    wStart = true;
                }
                return ret;
            } else if (state == 2) {
                boolean ret = bounceWith(wf, FLOAT_FADE_EFFECT);
                if (!ret) {
                    state = 3;
                }
                return ret;
            }
            return false;
        }

        private boolean bounceWith(float w, float f) {
            if (w == 0) {
                return false;
            }
            float lastY = offset.y;
            offset.y += velocity.y;
            float a = -offset.y * w;
            velocity.y += a;
            if (lastY * offset.y <= 0 && !wStart) {
                velocity.y *= f;
            }
            float lastX = offset.x;
            offset.x += velocity.x;
            a = -offset.x * w;
            velocity.x += a;
            if (lastX * offset.x <= 0 && !wStart) {
                velocity.x *= f;
            }
            wStart = false;
            if (offset.length() <= 1f && velocity.length() <= 1f) {
                offset.set(0, 0);
                velocity.set(0, 0);
                return false;
            }
            return true;
        }

        void triggerFloat(Random random, float v) {
            if (state != 3) {
                return;
            }
            state = 2;
            wStart = true;
            v = (random.nextFloat() * 0.5f + 0.5f) * v;
            float radian = (float) (random.nextFloat() * Math.PI * 2f);
            velocity.x += v * Math.cos(radian);
            velocity.y += v * Math.sin(radian);
        }
    }
}
