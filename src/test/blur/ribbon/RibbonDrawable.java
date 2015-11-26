package test.blur.ribbon;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class RibbonDrawable extends Drawable {

    Ribbon[] ribbons = new Ribbon[4];

    Symbol symbol;

    public RibbonDrawable(Resources res) {
        long globalOffset = 500;
        // red
        ribbons[0] = new Ribbon(0xfffd32f2, 0xffff9700).setOffsetRatio(2.5f, 2.5f)
                        .setScaleAndRotate(5.5f, -15).setCornerAndDirection(0, 3)
                        .setDurationAndOffset(500, 500 + globalOffset).setFloatDirection(-0.5f, 0, 1).setScaleCanvas(true);
        // yellow
        ribbons[1] = new Ribbon(0xffff7270, 0xfffde31d).setOffsetRatio(-3.5f, 1f)
                        .setScaleAndRotate(3.5f, -10).setCornerAndDirection(2, 2)
                        .setDurationAndOffset(500, 1000 + globalOffset).setFloatDirection(1, -0.5f, 4.5f);
        // green
        ribbons[2] = new Ribbon(0xff4bff4b, 0xff2398e8).setOffsetRatio(3.5f, 2.5f)
                        .setScaleAndRotate(4.5f, 18).setCornerAndDirection(0, 1)
                        .setDurationAndOffset(500, 1500 + globalOffset).setFloatDirection(-1f, -0.5f, 3f);
        // blue
        ribbons[3] = new Ribbon(0xff4a76ff, 0xff00ffff).setOffsetRatio(-1f, -2.9f)
                        .setScaleAndRotate(4.5f, 36).setCornerAndDirection(2, 2)
                        .setDurationAndOffset(500, 2000 + globalOffset).setFloatDirection(1.5f, 2f, 6.5f);

        symbol = new Symbol(res).setDurationAndStartOffset(1000, 2500 + globalOffset);
    }

    static final PaintFlagsDrawFilter filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    @Override
    public void draw(Canvas canvas) {
        long drawingTime = System.currentTimeMillis();
        boolean more = false;

        canvas.setDrawFilter(filter);
        for (int i = 0; i < ribbons.length; i++) {
            more |= ribbons[i].draw(canvas, drawingTime);
        }

        more |= symbol.draw(canvas, drawingTime);

        if (more) {
            invalidateSelf();
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
        for (int i = 0; i < ribbons.length; i++) {
            ribbons[i].setBounds(bounds);
        }
        symbol.setBounds(bounds);
    }
}
