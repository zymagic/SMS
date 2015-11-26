package test.blur;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class Dice extends Drawable {

    private PointF center = new PointF();
    private RectF face = new RectF();
    private Camera camera = new Camera();
    private Matrix[] matrix = new Matrix[6];
    private int[] color = new int[] {0x80ff0000, 0x80ffff00, 0x8000ff00, 0x800000ff, 0x8000ffff, 0x80ff00ff};

    private Paint paint = new  Paint();

    private Matrix mm = new Matrix();
    private Matrix mmm = new Matrix();

    public Dice() {
        paint.setStyle(Paint.Style.FILL);
    }

    float degree;

    @Override
    public void draw(Canvas canvas) {

        canvas.save();
        camera.save();
        camera.rotate(degree, degree, 0);
        mm.reset();
        camera.getMatrix(mm);
        camera.restore();
//
//        canvas.translate(center.x, center.y);
//        canvas.concat(mm);
//        canvas.translate(-center.x, -center.y);

        for (int i = 0; i < matrix.length; i++) {
            canvas.save();
            canvas.translate(center.x, center.y);
            mmm.set(mm);
            mmm.postConcat(matrix[i]);
            canvas.concat(mmm);
            canvas.translate(-center.x, -center.y);
            paint.setColor(color[i]);
            canvas.drawRect(face, paint);
            canvas.restore();
        }
        canvas.restore();
        degree += 1f;
        if (degree >= 360f) {
            degree = 0f;
        }
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        center.set(bounds.exactCenterX(), bounds.exactCenterY());
        float size = bounds.width() / 20f;
        face.set(center.x - size, center.y - size, center.x + size, center.y + size);

        Matrix m = getMatrix(0);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.getMatrix(m);
        camera.restore();

        m = getMatrix(1);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.rotate(0, -90, 0);
        camera.getMatrix(m);
        camera.restore();

        m = getMatrix(2);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.rotate(0, 90, 0);
        camera.getMatrix(m);
        camera.restore();

        m = getMatrix(3);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.rotate(-90, 0, 0);
        camera.getMatrix(m);
        camera.restore();

        m = getMatrix(4);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.rotate(90, 0, 0);
        camera.getMatrix(m);
        camera.restore();

        m = getMatrix(5);
        m.reset();
        camera.save();
        camera.translate(0, 0, size);
        camera.rotate(0, 180, 0);
        camera.getMatrix(m);
        camera.restore();
    }

    private Matrix getMatrix(int i) {
        if (i < 0 || i >= matrix.length) {
            return null;
        }
        if (matrix[i] == null) {
            matrix[i] = new Matrix();
        }
        return matrix[i];
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
