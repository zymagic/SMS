package test.blur;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TestDrawable extends Drawable {

    private PointF center = new PointF();
    private float radius;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Matrix matrix = new Matrix();
    private Paint shader = new Paint();

    private View host;

    public TestDrawable(View host) {
        this.host = host;
        paint.setStyle(Style.FILL);
        shader = new Paint(paint);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float w = bounds.width();
        float h = bounds.height();
        float ratio = level / 100f;
        float up = 1 + LOW / 100f;

        // draw left half

        canvas.save();
        canvas.clipRect(0, 0, center.x, h);

        paint.setColor(0xff87ac41);
        canvas.drawCircle(center.x, center.y, radius, paint);

        matrix.reset();
        matrix.preSkew(0, ratio, center.x, center.y);
        matrix.preScale(up - ratio, 1, center.x, center.y);
        canvas.concat(matrix);
        paint.setColor(0xffe44675);
        canvas.drawCircle(center.x, center.y, radius, paint);

        canvas.drawCircle(center.x, center.y, radius, shader);

        canvas.restore();

        // draw right half

        canvas.save();
        canvas.clipRect(center.x, 0, w, h);

        paint.setColor(0xff7598b9);
        canvas.drawCircle(center.x, center.y, radius, paint);

        matrix.reset();
        matrix.preSkew(0, -ratio, center.x, center.y);
        matrix.preScale(up - ratio, 1, center.x, center.y);
        canvas.concat(matrix);
        paint.setColor(0xffe44675);
        canvas.drawCircle(center.x, center.y, radius, paint);

        canvas.restore();

        if (isAnimating) {
            next();
            host.invalidate();
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

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        center.set(bounds.exactCenterX(), bounds.exactCenterY());
        radius = Math.min(bounds.width(), bounds.height()) * 0.4f;
        shader.setShader(new LinearGradient(0, 0, center.x, 0, 0, 0x33000000, TileMode.CLAMP));
    }

    static final int LOW = 30;
    static final int HIGH = 90;

    private boolean isAnimating;
    private int level = LOW;
    private boolean open = true;
    private float step = (HIGH - LOW) * 17f / 500;

    public void startAnimation() {
        if (isAnimating) {
            return;
        }
        isAnimating = true;
        host.invalidate();
    }

    public void stopAnimation() {
        if (!isAnimating) {
            return;
        }
        isAnimating = false;
        host.invalidate();
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    private void next() {
        if (open) {
            level += step;
            if (level >= HIGH) {
                level = HIGH;
                open = false;
            }
        } else {
            level -= step;
            if (level <= LOW) {
                level = LOW;
                open = true;
            }
        }
    }
}
