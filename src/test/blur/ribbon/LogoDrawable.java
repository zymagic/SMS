package test.blur.ribbon;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;

public class LogoDrawable extends Drawable {

    private Path path;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    RadialGradient[] gradients = new RadialGradient[4];
    private Rect clip = new Rect();

    private Paint clipPaint = new Paint();

    float ox = 2.7f;
    float oy = 2.5f;
    float scale = 4.5f;
    float rotate = -15;
    int cf = 0xfffd32f2;
    int ct = 0xffff9700;

    public LogoDrawable() {
        path = new Path();
        paint.setStyle(Style.FILL);

        clipPaint.setStyle(Style.STROKE);
        clipPaint.setColor(0xff000000);
        clipPaint.setStrokeWidth(0);
    }

    @Override
    public void draw(Canvas canvas) {

        final Rect bounds = getBounds();
        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();

        final float d = Math.min(bounds.width(), bounds.height()) * 0.1f; // * scale;

        canvas.save();
        canvas.clipRect(clip);

        float dx = d * ox;
        float dy = d * oy;
        canvas.translate(dx, dy);
        canvas.rotate(rotate, cx - dx, cy - dy);
        canvas.scale(4.5f, 4.5f, cx - dx, cy - dy);


        for (int i = 0; i < 4; i++) {
            paint.setShader(gradients[0]);
            canvas.drawPath(path, paint);
            canvas.rotate(90, cx, cy);
        }
        canvas.restore();

        canvas.drawRect(clip, clipPaint);
        canvas.drawLine(cx, 0, cx, bounds.height(), clipPaint);
        canvas.drawLine(0, cy, bounds.width(), cy, clipPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getOpacity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        float d = Math.min(bounds.width(), bounds.height()) * 0.1f;
        clip.set(bounds);
        clip.inset((int) d, (int) (bounds.height() * 0.1f));

//        d *= scale;
        float cx = bounds.width() / 2f;
        float cy = bounds.height() / 2f;
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

        float r = FloatMath.sqrt(52) * d;
        gradients[0] = new RadialGradient(cx - d * 3, cy - d * 3, d * 5, cf, ct, TileMode.CLAMP);

//        gradients[0] = new RadialGradient(cx + d * 2, cy - d * 4, r, 0xffee9900, 0xffffdd00, TileMode.CLAMP);
//        gradients[1] = new RadialGradient(cx + d * 2, cy - d * 4, r, 0xffcc0000, 0xffff6600, TileMode.CLAMP);
//        gradients[2] = new RadialGradient(cx + d * 2, cy - d * 4, r, 0xff0066aa, 0xff33ccff, TileMode.CLAMP);
//        gradients[3] = new RadialGradient(cx + d * 2, cy - d * 4, r, 0xff008000, 0xff00dd00, TileMode.CLAMP);

    }
}
