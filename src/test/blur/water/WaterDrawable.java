package test.blur.water;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

public class WaterDrawable extends Drawable {

    PathTransform ct;
    Path path;
    Paint p;

    Paint pm;

    boolean reverse = false;
    float ratio;
    float duration = 1000f;

    public WaterDrawable() {
        ct = new CircleTransform(260, 460, 540, 70);
        ct = new CircleRoundRectTransform(360, 720, 540, 160);
        path = new Path();
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xff00ff00);
        p.setStyle(Style.FILL);
//        p.setStrokeWidth(3);
        ct.update(0.25f, path);

        pm = new Paint(Paint.ANTI_ALIAS_FLAG);
        pm.setColor(0x80ffffff);
        pm.setStyle(Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path, p);

        if (reverse) {
            ratio -= 17f / duration;
            if (ratio <= 0) {
                ratio = 0;
                reverse = false;
            }
        } else {
            ratio += 17f / duration;
            if (ratio >= 1) {
                ratio = 1;
                reverse = true;
            }
        }

//        canvas.drawCircle(260, 540, 70, pm);
//        canvas.drawCircle(460, 540, 70, pm);
        canvas.drawCircle(360, 540, 160, pm);
        canvas.drawCircle(720, 540, 160, pm);

        path.reset();
        ct.update(ratio, path);

        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO Auto-generated method stub

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

}
