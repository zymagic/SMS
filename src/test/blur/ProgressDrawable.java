package test.blur;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

public class ProgressDrawable extends Drawable {

    private Paint mLinePaint = new Paint();
    private Paint mGlowPaint = new Paint();
    private int mStartColor, mEndColor;
    private int glowIndex = 0;
    private float glowTotal = 1500 / 17f;

    public ProgressDrawable(int startColor, int endColor) {
        mStartColor = startColor;
        mEndColor = endColor;
        setLevel(0);
    }

    /**
     * 0-100
     * */
    public void setProgress(int progress) {
        setLevel(progress * 100);
    }

    @Override
    public void draw(Canvas canvas) {
        int level = getLevel();
        float ratio = level / 10000f;
        Rect bounds = getBounds();

        float glowWidth = bounds.width() / 8f;
        float glowOffsetRatio = glowIndex++/glowTotal;
        float glowOffset = (bounds.width() + glowWidth) * glowOffsetRatio;

        canvas.save();
        canvas.clipRect(bounds.left, bounds.top, bounds.left + (bounds.width() * ratio), bounds.top + 10);
        canvas.drawPaint(mLinePaint);
        if (glowOffset > 0 && glowOffset < bounds.width() * ratio + glowWidth) {
            canvas.translate(glowOffset, 0);
            canvas.drawPaint(mGlowPaint);
        }
        canvas.restore();

        if (glowOffset >= bounds.width() * ratio + glowWidth) {
            glowIndex = 0;
        }

        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mLinePaint.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.top, mStartColor, mEndColor, TileMode.CLAMP));

        float glowWidth = bounds.width() / 8f;
//        mGlowPaint.setShader(new LinearGradient(bounds.left - glowWidth, 0, bounds.left, 0, new int[] {0x00ffffff, 0x80ffffff, 0xeeffffff, 0x80ffffff, 0x00ffffff}, new float[] {0f, 0.35f, 0.5f, 0.65f, 1f}, TileMode.CLAMP));
        mGlowPaint.setShader(new LinearGradient(bounds.left - glowWidth, 0, bounds.left, 0, new int[] {0x00ffffff, 0xeeffffff, 0x00ffffff}, new float[] {0f, 0.5f, 1f}, TileMode.CLAMP));
//        mGlowPaint.setShader(new RadialGradient(bounds.left - glowWidth / 2f, bounds.exactCenterY(), glowWidth / 2f, 0xeeffffff, 0x00ffffff, TileMode.CLAMP));

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
