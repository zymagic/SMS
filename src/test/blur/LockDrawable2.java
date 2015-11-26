package test.blur;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

public class LockDrawable2 extends Drawable implements Interpolator {

    private Bitmap bmp1, bmp2;

    private PointF pos1, pos2;

    private PointF center = new PointF();
    private float radius;

    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float distance, offset = 0f;

    private boolean isAnimating = true;

    private int frameIndex = -1;

    private float duration = 300f / 17f;

    private float startOffset = 300 / 17f;

    public LockDrawable2(Context context) {
        final Resources res = context.getResources();

        bmp1 = BitmapFactory.decodeResource(res, R.drawable.lockdown);
        bmp2 = BitmapFactory.decodeResource(res, R.drawable.lockup);

        pos1 = new PointF();
        pos2 = new PointF();

        float densityScale = 3f / res.getDisplayMetrics().density;
        radius = 110f * densityScale;
        pos1.set(-radius, radius - 138f * densityScale);
        pos2.set(-130f * densityScale, radius + (32f - 288f) * densityScale);

        distance = 64f * densityScale;

        circlePaint.setColor(0xff000000);
        circlePaint.setStyle(Paint.Style.FILL);
        clipPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
    }

    @Override
    public void draw(Canvas canvas) {

        boolean more = evalAnimation();

        canvas.saveLayer(center.x - radius, center.y - radius, center.x + radius, center.y + radius, null, Canvas.ALL_SAVE_FLAG);

        canvas.drawCircle(center.x, center.y, radius, circlePaint);

        canvas.drawBitmap(bmp2, center.x + pos2.x, center.y + pos2.y - offset, clipPaint);
        canvas.drawBitmap(bmp1, center.x + pos1.x, center.y + pos1.y, null);

        canvas.restore();

        if (more) {
            invalidateSelf();
        }
    }

    private boolean evalAnimation() {
        if (!isAnimating) {
            return false;
        }

        float normalized = (frameIndex++-startOffset) / duration;
        boolean expired = normalized >= 1f;
        if (normalized >= 1f) {
            normalized = 1f;
        } else if (normalized < 0f) {
            normalized = 0f;
        }

        float interpolation = getInterpolation(normalized);
        offset = interpolation * distance;

        if (expired) {
            isAnimating = false;
        }

        return !expired;
    }

    public void startAnimation() {
        if (isAnimating) {
            return;
        }

        isAnimating = true;
        offset = 0;
        frameIndex = -1;

        invalidateSelf();
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
    }

    @Override
    public float getInterpolation(float input) {
        return -2.38f * input * input + 3.38f * input;
    }

}
