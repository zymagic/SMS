package test.blur;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class LockDrawable extends Drawable {

    private PointF center = new PointF();

    private Paint circlePaint;
    private Paint linePaint;
    private Paint outerRingPaint;
    private Paint innerRingPaint;

    private float radius;

    private float circleScale = 0f;
    private float lineScale = 0f;
    private float alpha = 0f;
    private float rotation = 0f;

    private boolean isAnimating = true;
    private int step;
    private int[] durations = {(int) (500 / 17f), (int) (500 / 17f), (int) (500 / 17f), (int) (1500 / 17f)};
    private int frameIndex = -1;

    private boolean reverse = false;

    public LockDrawable() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(0xffffffff);
        circlePaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xffffffff);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        outerRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerRingPaint.setStyle(Paint.Style.STROKE);
        innerRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerRingPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void draw(Canvas canvas) {
        boolean more = evalAnimation();

        float cx = center.x;
        float cy = center.y;

        boolean rotate = rotation != 0;
        if (rotate) {
            canvas.save();
            canvas.rotate(rotation, cx, cy);
        }

        if (alpha > 0f) {
            canvas.drawCircle(cx, cy, radius * 0.775f, outerRingPaint);
            canvas.drawCircle(cx, cy, radius * 0.425f, innerRingPaint);
        }

        if (lineScale > 0f) {
            canvas.drawLine(cx - 0.5f * radius * Math.min(lineScale / 0.5f, 1f), cy, cx + 0.95f * radius * lineScale, cy, linePaint);
        }

        if (circleScale > 0f) {
            canvas.drawCircle(cx, cy, radius * 0.16f * circleScale, circlePaint);
        }

        if (rotate) {
            canvas.restore();
        }

        if (more) {
            invalidateSelf();
        }
    }

    private boolean evalAnimation() {
        if (!isAnimating) {
            return false;
        }

        float normalized = ++frameIndex * 1.0f / durations[step];

        if (reverse) {
            normalized = 1 - normalized;
        }

        if (step == 0) {
            circleScale = normalized;
        } else if (step == 1) {
            lineScale = normalized;
        } else if (step == 2) {
            alpha = normalized;
            innerRingPaint.setAlpha((int) (alpha * 255f));
            outerRingPaint.setAlpha((int) (alpha * 255f));
        } else if (step == 3) {
            rotation = 720f * normalized;
            if (rotation == 720f) {
                rotation = 0;
            }
        }

        if (frameIndex >= durations[step]) {
            if (!reverse) step++; else step--;
            frameIndex = -1;
            if (reverse && step < 0 || !reverse && step >= 4) {
                isAnimating = false;
                return false;
            }
        }
        return true;
    }

    public void startAnimation(boolean reverse) {
        if (isAnimating) {
            return;
        }

        isAnimating = true;
        frameIndex = 0;
        step = reverse ? 3 : 0;
        circleScale = reverse ? 1f : 0f;
        lineScale = reverse ? 1f : 0f;
        alpha = reverse ? 1f : 0f;
        rotation = reverse ? 720f : 0f;
        this.reverse = reverse;

        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        center.set(bounds.exactCenterX(), bounds.exactCenterY());

        radius = bounds.width() * 0.4f;

        linePaint.setStrokeWidth(0.1f * radius);

        outerRingPaint.setStrokeWidth(0.45f * radius);
        outerRingPaint.setShader(new SweepGradient(center.x, center.y, 0x00ffffff, 0xffffffff));

        innerRingPaint.setStrokeWidth(0.25f * radius);
        innerRingPaint.setShader(new SweepGradient(center.x, center.y, new int[] {0x80ffffff, 0xffffffff, 0x00ffffff, 0x80ffffff}, new float[] {0, 0.5f, 0.5f, 1f}));

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

}
