package test.blur;

import java.util.Iterator;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.SystemClock;

public class WaveDrawable extends BaseDrawable {

    private static float amplitude = 120;
    private static final float velocity = 2f;
    private static final float WAVES = (float) (Math.PI * 4f);
    private static final float stride = 360;

    private static final int WIDTH_DIMENSION = 9 * 5;
    private static final int HEIGHT_DIMENSION = 16 * 5;
    private static final int COUNT = (WIDTH_DIMENSION + 1) * (HEIGHT_DIMENSION + 1);

    private Bitmap bitmap;

    private float[] originVertices = new float[COUNT * 2];
    private float[] vertices = new float[COUNT * 2];
    private int[] colors = new int[COUNT];

    private LinkedList<Wave> waves = new LinkedList<Wave>();

    private float[] temp = new float[2];

    private Runnable listener;

    public WaveDrawable(Bitmap bmp) {
        this.bitmap = bmp;
        initVertices();
    }

    private void initVertices() {
        final float width = bitmap.getWidth();
        final float height = bitmap.getHeight();
        float unitWidht = width / WIDTH_DIMENSION;
        float unitHeight = height / HEIGHT_DIMENSION;
        for (int j = 0; j <= HEIGHT_DIMENSION; j++) {
            for (int i = 0; i <= WIDTH_DIMENSION; i++) {
                int index = i + j * (WIDTH_DIMENSION + 1);
                originVertices[2 * index] = i * unitWidht;
                originVertices[2 * index + 1] = j * unitHeight;
            }
        }
        System.arraycopy(originVertices, 0, vertices, 0, originVertices.length);
    }

    @Override
    public void draw(Canvas canvas) {
        updateWave();
        canvas.drawBitmapMesh(bitmap, WIDTH_DIMENSION, HEIGHT_DIMENSION, vertices, 0, null, 0, null);
//        for (Wave v : waves) {
//            float scale = v.distance / v.radius;
//            if (scale < 1f) {
//                canvas.save();
//                canvas.scale(scale, scale, v.touchX, v.touchY);
//                canvas.drawCircle(v.touchX, v.touchY, v.radius, v.paint);
//                canvas.restore();
//            }
//        }
    }

    public void waveAt(float x, float y) {
        Rect bounds = getBounds();
        float width = bounds.width();
        float height = bounds.height();
        waves.add(new Wave(x, y, width, height));
        invalidateSelf();
    }

    private void updateWave() {
        if (waves.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Wave> iter = waves.iterator();
        while (iter.hasNext()) {
            Wave w = iter.next();
            if (w.end) {
                iter.remove();
            } else {
                w.update(now);
            }
        }

        if (waves.isEmpty()) {
            notifyWaveStoped();
            return;
        }

        System.arraycopy(originVertices, 0, vertices, 0, originVertices.length);
        Rect bounds = getBounds();
        float width = bounds.width();
        float height = bounds.height();
        for (int i = 0; i < COUNT; i++) {
            float x = vertices[i * 2];
            float y = vertices[i * 2 + 1];
            float z = 0;
            for (Wave w : waves) {
                z += w.getZ(x, y);
            }
            if (z == 0f) {
                colors[i] = 0xffffffff;
                continue;
            }
            if (z > 0) {
                int c = (int) ((0.95f + (amplitude - z) / amplitude * 0.05f) * 255f);
                colors[i] = Color.argb(0xff, c, c, c);
            } else {
                colors[i] = 0xffffffff;
            }
            calculate2D(x, y, width, height, z, temp);
            vertices[i * 2] = temp[0];
            vertices[i * 2 + 1] = temp[1];
        }
        invalidateSelf();
    }

//    static float cameraDistance = 5.5f;

    private static void calculate2D(float x, float y, float width, float height, float z, float[] out) {
//        if (x == 0 || y == 0 || x == width || y == height) {
//            out[0] = x;
//            out[1] = y;
//            return;
//        }
        float centerX = x - width / 2f;
        float centerY = y - height / 2f;

//        float cd = width * cameraDistance;
//        float scale = cd / (cd + z);
//        out[0] = width / 2f + centerX * scale;
//        out[1] = height / 2f + centerY * scale;

        float offset = z / 120f * 10;
        out[0] = (float) (x + centerX / Math.hypot(centerX, centerY) * offset);
        out[1] = (float) (y + centerY / Math.hypot(centerX, centerY) * offset);
        if (x == 0 && out[0] > 0 || x == width && out[0] < width) {
            out[0] = x;
        }
        if (y == 0 && out[1] > 0 || y == height && out[1] < height) {
            out[1] = y;
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return bitmap.getHeight();
    }

    public void setWaveListener(Runnable l) {
        this.listener = l;
    }

    private void notifyWaveStoped() {
        if (listener == null) {
            return;
        }
        scheduleSelf(listener, SystemClock.currentThreadTimeMillis());
        listener = null;
    }

    private class Wave {

        long startTime = -1;
        float radius;
        boolean end = false;
        float distance;
        float touchX, touchY;
        Paint paint;

        Wave(float x, float y, float w, float h) {
            touchX = x;
            touchY = y;
            float lx = Math.max(w - x, x);
            float ly = Math.max(h - y, y);
            radius = (float) (Math.hypot(lx, ly) + stride);
            paint = new Paint();
            paint.setAlpha(255);
            paint.setShader(new RadialGradient(touchX, touchY, radius, new int[] {0x00ffffff, 0x00ffffff, 0x22ffffff, 0x00ffffff}, new float[] {0f, 1 - stride / radius, 1- stride / radius / 2f, 1f}, TileMode.CLAMP));
        }

        float getZ(float x, float y) {
            float delta = distance - PointF.length(x - touchX, y - touchY);
            if (delta <= 0 || delta >= stride) {
                return 0;
            }
            double phase = delta / stride * WAVES;
            float zOffset = (float) (Math.sin(phase) * amplitude);
            return zOffset;
        }

        void update(long now) {
            if (startTime == -1) {
                startTime = now;
            }
            long t = now - startTime;
            float currentVelocity = Math.max(1, velocity - t * 0.001f);
            if (t * 0.001f < velocity - 1f) {
                distance = (velocity + currentVelocity) * t / 2f;
            } else {
                distance = (velocity + 1f) / 2f * 1000 + (t - 1000) * 1f;
            }
//            distance = (now - startTime) * velocity;
            if (distance >= radius) {
                end = true;
            }
        }
    }

}