package test.blur.ribbon;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class Ribbon {

    private float xOffsetRatio, yOffsetRatio;
    private float scale;
    private float rotate;
    private int startColor, endColor;
    private int partDegree1, partDegree2;

    private Path path = new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PointF center = new PointF();
    private float dimen;

    private PointF swipeDirection = new PointF();
    private PointF floatDirection = new PointF();

    private long startTime = -1;
    private long startOffset;
    private long duration;
    private float floating;
    private Interpolator interpolator = new DecelerateInterpolator();
    private float dest;
    private RectF clip = new RectF();

    private boolean scaleCanvas = false;

    //////////////////////////
    // constructor and initial methods begin

    public Ribbon(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.paint.setAlpha(0xd9);
    }

    public Ribbon setOffsetRatio(float xOffsetRatio, float yOffsetRatio) {
        this.xOffsetRatio = xOffsetRatio;
        this.yOffsetRatio = yOffsetRatio;
        return this;
    }

    public Ribbon setScaleAndRotate(float scale, float rotate) {
        this.scale = scale;
        this.rotate = rotate;
        return this;
    }

    public Ribbon setCornerAndDirection(int corner, int direction) {
        switch (corner) {
            case 1:
                partDegree1 = 0;
                partDegree2 = 90;
                break;
            case 2:
                partDegree1 = 90;
                partDegree2 = 90;
                break;
            case 3:
                partDegree1 = -90;
                partDegree2 = -90;
                break;
            default:
                partDegree1 = 0;
                partDegree2 = -90;
        }

        switch (direction) {
            case 1:
                this.swipeDirection.set(1, -1);
                break;
            case 2:
                this.swipeDirection.set(1, 1);
                break;
            case 3:
                this.swipeDirection.set(-1, 1);
                break;
            default:
                this.swipeDirection.set(-1, -1);
                break;
        }

        return this;
    }

    public Ribbon setDurationAndOffset(long duration, long startOffset) {
        this.duration = duration;
        this.startOffset = startOffset;
        return this;
    }

    public Ribbon setFloatDirection(float rx, float ry, float last) {
        floatDirection.set(rx, ry);
        this.floating = last;
        return this;
    }

    public Ribbon setScaleCanvas(boolean scale) {
        this.scaleCanvas = scale;
        return this;
    }

    //////////////////////////
    // constructor and initial methods end

    public boolean draw(Canvas canvas, long drawingTime) {
        boolean more = duration > 0;

        if (startTime == -1) {
            startTime = drawingTime;
        }

        long passed = drawingTime - startTime - startOffset;
        if (passed <= 0) {
            return true;
        }

        float normalized = 0;
        float ratio = 1f;
        float floatingFactor = 0;
        if (duration > 0) {
            normalized = passed * 1.0f / duration;
            floatingFactor = passed / floating / duration;
            more = normalized < 1f || floatingFactor < 1f;
            if (normalized > 1f) {
                normalized = 1f;
            }
            ratio = interpolator.getInterpolation(normalized);
            if (floatingFactor > 1f) {
                floatingFactor = 1f;
            }
            floatingFactor = interpolator.getInterpolation(floatingFactor);
        }

        float cx = center.x;
        float cy = center.y;

        canvas.save();

        if (more) {
            float tx = dest * (ratio - 1) * swipeDirection.x;
            float ty = dest * (ratio - 1) * swipeDirection.y;
            canvas.translate(tx, ty);
            canvas.rotate(45, cx, cy);
            canvas.clipRect(clip);
            canvas.rotate(-45, cx, cy);
            canvas.translate(-tx, -ty);
        }

        float floatDimen = scaleCanvas ? dimen : dimen / scale;
        float fx = floatingFactor * floatDimen * floatDirection.x;
        float fy = floatingFactor * floatDimen * floatDirection.y;
        canvas.translate(fx, fy);

        float dx = dimen * xOffsetRatio;
        float dy = dimen * yOffsetRatio;
        canvas.translate(dx, dy);
        canvas.rotate(rotate, cx - dx, cy - dy);
        if (scaleCanvas) {
            canvas.scale(scale, scale, cx - dx, cy - dy);
        }

        if (partDegree1 != 0) {
            canvas.rotate(partDegree1, cx, cy);
        }
        canvas.drawPath(path, paint);
        canvas.rotate(partDegree2, cx, cy);
        canvas.drawPath(path, paint);

        canvas.restore();

        return more;
    }

    public void setBounds(Rect bounds) {
        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();
        float d = Math.min(bounds.width(), bounds.height()) * 0.125f;
        center.set(cx, cy);

        if (!scaleCanvas) {
            d *= scale;
        }

        this.dimen = d;

        RectF rect = new RectF();
        path.moveTo(cx - d * 4, cy);
        rect.set(cx - d * 4, cy - d * 2, cx, cy + d * 2);
        path.arcTo(rect, -180, 90);
        path.lineTo(cx + d, cy - d * 2);
        rect.set(cx, cy - d * 2, cx + d * 2, cy);
        path.arcTo(rect, -90, 90);
        path.lineTo(cx + d * 2, cy - d * 2);
        rect.set(cx - d * 2, cy - d * 4, cx + d * 2, cy);
        path.arcTo(rect, 0, -90);
        path.lineTo(cx - d,  cy - d * 4);
        rect.set(cx - d * 4, cy - d * 4, cx + d * 2, cy + d * 2);
        path.arcTo(rect, -90, -90);
        path.close();

        paint.setShader(new RadialGradient(cx - d * 2.2f, cy - d * 3, d * 5, startColor, endColor, TileMode.CLAMP));

        dest = (float) (Math.hypot(bounds.width(), bounds.height()) / 2f);
        clip.set(cx - dest, cy - dest, cx + dest, cy + dest);
    }

}
