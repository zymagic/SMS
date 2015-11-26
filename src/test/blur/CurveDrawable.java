package test.blur;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class CurveDrawable extends Drawable {

    private Path mPath = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCirclePaint = new Paint();

    private float[] coords = new float[10];

    private Point mPoints;

    public CurveDrawable() {
        mPaint.setColor(0xff000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mCirclePaint.setColor(0x80ff0000);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(0x80ffffff);
        mLinePaint.setStyle(Paint.Style.STROKE);

        Random r = new Random(47);
        for (int i = 0; i < coords.length; i++) {
            coords[i] = r.nextFloat() - 0.5f;
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        float width = bounds.width();
        float height = bounds.height();

        mPoints = new Point();
        Point p = mPoints;
        Random r = new Random();
        for (int i = 0; i < coords.length; i++) {
            float cx = width / (coords.length + 1) * (i + 1);
            float cy = height / 4f; // * coords[i];
            if (i == 0) {
                p.x = cx;
                p.ay = cy;
                p.y = height / 2f;
                p.v = (float) ((5000 + r.nextInt(200)) * Math.pow(-1, r.nextInt(2)));
                continue;
            }
            p = p.next(cx, cy);
            p.v = (float) ((5000 + r.nextInt(200)) * Math.pow(-1, r.nextInt(2)));
        }

        buildPath(mPoints);

    }

    private void buildPath(Point point) {
        Point p = point;
        while (p != null) {
            p.initPoint();
            p = p.next;
        }

        mPath.reset();
        mPath.moveTo(point.x, point.y);

        p = point.next;

        while (p != null) {
            mPath.cubicTo(p.prev.nx, p.prev.ny, p.px, p.py, p.x, p.y);
            p = p.next;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        next();
        canvas.drawPath(mPath, mPaint);
        Point p = mPoints;
        while (p != null) {
            canvas.drawCircle(p.x, p.y, 10, mCirclePaint);
            if (p.px != p.nx || p.py != p.ny) {
                canvas.drawLine(p.px, p.py, p.nx, p.ny, mLinePaint);
            }
            p = p.next;
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

    private long startTime = -1;

    private void next() {
        if (!isAnimating) {
            return;
        }
        long now = System.currentTimeMillis();
        if (startTime == -1) {
            startTime = now;
        }
        long passed = now - startTime;
        Point p = mPoints;
        while (p != null) {
            p.y = getBounds().height() / 2f + (float) (p.ay * Math.sin((passed / p.v) * Math.PI * 2f));
            p = p.next;
        }
        buildPath(mPoints);
        invalidateSelf();
    }

    private static class Point {
        Point prev, next;
        float ay;
        float x, y;
        float px, py;
        float nx, ny;
        float v;
        boolean isMid = false;
        public Point next(float x, float y) {
            Point point = new Point();
            point.x = x;
            point.ay = y;
            point.y = this.y;
            point.prev = this;
            next = point;
            return point;
        }

        public void initPoint() {
            if (prev == null || next == null) {
                px = x;
                py = y;
            } else {
                px = (prev.x + x) / 2f;
                py = y;
            }
            if (next == null || prev == null) {
                nx = x;
                ny = y;
            } else {
                nx = (next.x + x) / 2f;
                ny = y;
            }
            if (prev != null && next != null) {
                if ((y - prev.y) * (y - next.y) < 0) {
                    isMid = true;
                    float cx = (prev.x + next.x) / 2f;
                    float cy = (prev.y + next.y) / 2f;
                    float cpx = (x + prev.x) / 2f;
                    float cpy = (y + prev.y) / 2f;
                    float cnx = (x + next.x) / 2f;
                    float cny = (y + next.y) / 2f;
                    float ccpx = (cx + prev.x) / 2f;
                    float ccpy = (cy + prev.y) / 2f;
                    float ccnx = (cx + next.x) / 2f;
                    float ccny = (cy + next.y) / 2f;
                    px = 2 * cpx - ccpx;
                    py = 2 * cpy - ccpy;
                    nx = 2 * cnx - ccnx;
                    ny = 2 * cny - ccny;
                } else {
                    isMid = false;
                }
            }
            if (prev != null) {
                adjustP();
            }
            if (prev != null && prev.isMid) {
                prev.adjustN();
            }
        }

        void adjustP() {
            while ((py - prev.ny) * (y - prev.ny) < 0 || (px - prev.nx) * (x - prev.nx) < 0) {
                float oldX = nx;
                float oldY = ny;
                px = x + (px - x) * 0.9f;
                py = y + (py - y) * 0.9f;
                if (Math.hypot(oldX - px, oldY - py) < 1f) {
                    px = x + (oldX - x) * 0.5f;
                    py = y + (oldY - y) * 0.5f;
                }
            }
        }

        void adjustN() {
            while ((ny - next.py) * (y - next.py) < 0 || (nx - next.px) * (x - next.px) < 0) {
                float oldX = nx;
                float oldY = ny;
                nx = x + (nx - x) * 0.9f;
                ny = y + (ny - y) * 0.9f;
                if (Math.hypot(oldX - nx, oldY - ny) < 1f) {
                    nx = x + (oldX - x) * 0.5f;
                    ny = y + (oldY - y) * 0.5f;
                }
            }
        }
    }

    boolean isAnimating = false;

    public void toggle() {
        isAnimating = !isAnimating;
        if (isAnimating) {
            invalidateSelf();
        }
    }
}
