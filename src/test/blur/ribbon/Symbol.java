package test.blur.ribbon;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.FloatMath;

public class Symbol {

    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private FontMetrics fm = new FontMetrics();
    private Path path = new Path();

    private final float rotation = 20;
    private float radius;
    private float delta;
    private PointF center = new PointF();

    private long startTime = - 1;
    private long duration = 900;
    private long startOffset;

    private float textWidth;
    private String title = "360手机桌面";

    public Symbol(Resources res) {
        radius = res.getDisplayMetrics().density * 90;
        float radian = (float) Math.toRadians(rotation);
        delta = radius * (FloatMath.cos(radian) - FloatMath.sin(radian));

        textPaint.setColor(0);
        textPaint.setTextSize(res.getDisplayMetrics().density * 150);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        textPaint.setTypeface(Typeface.createFromFile("/system/fonts/Roboto-Thin.ttf"));
        textPaint.getFontMetrics(fm);
        textPaint.setStyle(Paint.Style.FILL);

        ringPaint.setColor(0xecffffff);
        ringPaint.setStyle(Paint.Style.FILL);

        clipPaint.setStyle(Paint.Style.FILL);
        clipPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

        subPaint.setTextSize(res.getDisplayMetrics().density * 30);
        subPaint.setFakeBoldText(true);
        subPaint.setTextAlign(Paint.Align.CENTER);
        subPaint.setColor(0xecffffff);
        subPaint.setTextSkewX(-0.25f);
        textWidth = subPaint.measureText(title);
    }

    public Symbol setDurationAndStartOffset(long duration, long startOffset) {
        this.duration = duration;
        this.startOffset = startOffset;
        return this;
    }

    public boolean draw(Canvas canvas, long drawingTime) {
        boolean more = duration > 0;
        if (startTime == -1) {
            startTime = drawingTime;
        }

        long passed = drawingTime - startOffset - startTime;
        if (passed <= 0) {
            return true;
        }

        float normalized = 0;
        float ratio = 1f;
        float div = 0.66f;
        if (duration > 0) {
            normalized = passed * 1.0f / duration;
            more = normalized < 1f;
            if (normalized > 1f) {
                normalized = 1f;
            }

            ratio = normalized < div ?
                    (float)(1.0f - Math.pow((1.0f - normalized / div), 4))
                    : 1.0f - (1.0f - (normalized - div) / (1 - div)) * (1.0f - (normalized - div) / (1 - div));
        }

        canvas.saveLayer(center.x - radius, center.y - radius, center.x + radius, center.y + radius, null, Canvas.ALL_SAVE_FLAG);

        if (more && normalized < div) {
            float tx = radius * 2 * ratio;
            canvas.rotate(-rotation, center.x, center.y);
//            canvas.translate(tx, 0);
            canvas.clipRect(center.x - radius - radius - (delta + radius) / 2f + tx, center.y - radius, center.x - radius + tx, center.y + radius);
//            canvas.translate(-tx, 0);
            canvas.rotate(rotation, center.x, center.y);
        }

        canvas.drawCircle(center.x, center.y, radius, ringPaint);
        canvas.drawText("6", center.x, center.y - (fm.ascent + fm.descent) / 2, textPaint);

        if (more && normalized >= div) {
            float tx = 2.5f * radius * ratio;
            canvas.translate(tx, 0);
            canvas.drawPath(path, textPaint);
        }

        canvas.restore();

        if (more) {
            if (normalized > div) {
                canvas.save();
                float tx = textWidth * ratio;
                canvas.clipRect(center.x - textWidth / 2f, center.y + radius, center.x - textWidth / 2f + tx, center.y + radius + 200);
                canvas.drawText(title, center.x, center.y + radius + 160, subPaint);
                canvas.restore();
            }
        } else {
            canvas.drawText(title, center.x, center.y + radius + 160, subPaint);
        }

        return more;
    }

    public void setBounds(Rect bounds) {
        center.set(bounds.exactCenterX(), bounds.exactCenterY() * 0.9f);
        path.moveTo(center.x - radius, center.y - radius);
        float radian = (float) Math.toRadians(rotation);
        path.lineTo((float) (center.x + radius * (Math.tan(radian) * 2 - 1)), center.y + radius);
        path.lineTo(center.x - radius * 1.2f, center.y + radius);
        path.lineTo(center.x - radius * 1.35f, center.y - radius);
        path.close();
    }
}
