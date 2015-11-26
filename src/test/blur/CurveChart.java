package test.blur;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class CurveChart extends View implements OnClickListener {

    private Path mUpperPath;
    private Path mLowerPath;

    private Paint mUpperPaint;
    private Paint mLowerPaint;

    private Point mUpperPoints;
    private Point mLowerPoints;

    private ArrayList<Point> mPoints = new ArrayList<CurveChart.Point>();

    private float maxTemp = -1000, minTemp = 1000;

    private boolean needUpdatePath = true;

    private Paint mTextPaint;
    private FontMetrics fm;

    private Paint mLinePaint;

    private Paint mDotPaint;
    private Paint mRingPaint;

    private Point current;

    private PointF mTouchPoint = new PointF();
    private PointF mDownPoint = new PointF();

    private boolean isEntering = true;
    private long enterDuration = 1000;
    private long enterTime = -1;

    private boolean isFading = true;
    private long fadeDuration = 500;
    private long fadeTime = -1;

    private Scroller mSnapScroller;
    private float mTouchOffset;
    private int mTouchSlop;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_DRAG = 1;
    private static final int TOUCH_STATE_CLICK = 2;
    private int mTouchState = TOUCH_STATE_REST;

    public CurveChart(Context context) {
        this(context, null);
    }

    public CurveChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(context.getResources().getDisplayMetrics().density * 12);
        mTextPaint.setColor(0xccffffff);
        fm = mTextPaint.getFontMetrics();

        mUpperPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUpperPaint.setColor(0xffffffff);
        mUpperPaint.setStyle(Paint.Style.FILL);

        mLowerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLowerPaint.setColor(0xffffffff);
        mLowerPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(context.getResources().getDisplayMetrics().density * 1);
        mLinePaint.setColor(0xffffffff);
        mLinePaint.setPathEffect(new DashPathEffect(new float[] {context.getResources().getDisplayMetrics().density * 5, context.getResources().getDisplayMetrics().density * 2}, 0));

        mDotPaint = new Paint();
        mDotPaint.setColor(0xffffffff);
        mDotPaint.setStyle(Paint.Style.FILL);

        mRingPaint = new Paint();
        mRingPaint.setColor(0xffffffff);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(context.getResources().getDisplayMetrics().density * 1);

        mSnapScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;

        setOnClickListener(this);
    }

    public void setData(ArrayList<Data> data) {
        mUpperPoints = null;
        mLowerPoints = null;
        mPoints.clear();
        current = null;
        if (data != null && data.size() > 0) {
            final int count = data.size();
            Point up = mUpperPoints = new Point();
            Point lp = mLowerPoints = new Point();
            for (int i = 0; i < count; i++) {
                Data d = data.get(i);
                if (d.upper > maxTemp) {
                    maxTemp = d.upper;
                }
                if (d.lower < minTemp) {
                    minTemp = d.lower;
                }
                if (i == 0) {
                    up.setData(d.upper);
                    lp.setData(d.lower);
                } else {
                    up = up.next(d.upper);
                    lp = lp.next(d.lower);
                }
                lp.isCurrent = !d.isStroke;
                lp.isToday = !d.isStroke;
                lp.pair = up;
                up.pair = lp;
                mPoints.add(lp);
            }
        }
        needUpdatePath = true;
        invalidate();
    }

    private void updatePath() {
        if (mUpperPoints == null || mLowerPoints == null) {
            mUpperPath = null;
            mLowerPath = null;
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            invalidate();
            return;
        }

        if (mUpperPath == null || mLowerPath == null || needUpdatePath) {
            if (mUpperPath == null) {
                mUpperPath = new Path();
            }
            if (mLowerPath == null) {
                mLowerPath = new Path();
            }
            float bottom = updatePath(mLowerPoints, mLowerPath, mLowerPaint, false, getHeight() - getPaddingBottom());
            updatePath(mUpperPoints, mUpperPath, mUpperPaint, true, bottom);
        }
        needUpdatePath = false;
    }

    private float updatePath(Point point, Path path, Paint paint, boolean upper, float base) {
        int dayCount = mPoints.size();
        float width = getWidth();
        float height = getHeight();
        float xStart = -width / 2f / dayCount;
        float xStep = width / dayCount;
        float yCenter = height / 2f;
        float yRange = (height - getPaddingTop() - getPaddingBottom()) / 2f - (fm.bottom - fm.top) * 1.5f - paint.getStrokeWidth() - mTouchSlop;
        float aValue = (maxTemp + minTemp) / 2f;
        float vRange = (maxTemp - minTemp) / 2f;

        Point start = new Point();
        Point end = new Point();

        float maxY = -10000;
        float minY = 10000;

        start.next = point;
        point.prev = start;
        start.setData(point.next == null ? (point.value + aValue) / 2f : (point.value + (point.value - point.next.value) * 0.2f));

        Point p = start;
        while (p != null) {
            p.x = xStart;
            p.y = yCenter - (vRange == 0 ? 0 : (p.value - aValue) / vRange) * yRange;
            if (maxY < p.y) {
                maxY = p.y;
            }
            if (minY > p.y) {
                minY = p.y;
            }
            if (p.next == null && p != end) {
                p.next = end;
                end.prev = p;
                end.setData(p.prev == null ? (p.value + aValue) / 2f : (p.value + (p.value - p.prev.value) * 0.2f));
            }
            p = p.next;
            xStart += xStep;
        }

        paint.setShader(new LinearGradient(0, minY, 0, base, upper ? 0xaaffffff : 0x80ffffff, 0x00ffffff, TileMode.CLAMP));

        p = start;
        while (p != null) {
            p.initPoint();
            p = p.next;
        }

        path.reset();
        path.moveTo(start.x, start.y);

        p = point;
        float x = p.x;
        while (p != null) {
            x = p.x;
            path.cubicTo(p.prev.nx, p.prev.ny, p.px, p.py, p.x, p.y);
            p = p.next;
        }

        path.lineTo(x, base);
        path.lineTo(start.x, base);
        path.close();

        point.prev = null;
        end.prev.next = null;

        return maxY;
    }

    @Override
    public void computeScroll() {
        if (mSnapScroller.computeScrollOffset()) {
            Point current = findCurrentPoint();
            if (current == null) {
                mSnapScroller.abortAnimation();
                return;
            }
            float step = getWidth() * 1.0f / mPoints.size();
            current.ratio = mSnapScroller.getCurrX() / step;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updatePath();

        if (mUpperPath == null || mLowerPath == null) {
            return;
        }

        boolean isAnimating = isEntering;
        if (isAnimating) {
            float ratio = computeEnter();
            canvas.clipRect(0, 0, getWidth() * ratio, getHeight());
        }

        canvas.drawPath(mUpperPath, mUpperPaint);
        canvas.drawPath(mLowerPath, mLowerPaint);

//        Point p = mUpperPoints;
//        while (p != null) {
//            canvas.drawText(p.txt, p.x, p.y - (fm.bottom - fm.top) / 2f, mTextPaint);
//            p = p.next;
//        }
//
//        p = mLowerPoints;
//        while (p != null) {
//            canvas.drawText(p.txt, p.x, p.y + (fm.bottom - fm.top) / 2f - fm.bottom - fm.top, mTextPaint);
//            canvas.drawLine(p.x, p.y, p.pair.x, p.pair.y, mLinePaint);
//            p = p.next;
//        }
        if (!isAnimating) {
            Point p = findCurrentPoint();
            if (p != null && p.pair != null) {

                boolean isFading = this.isFading;
                if (isFading) {
                    float ratio = computeFade();
                    int alpha = (int) (ratio * 255);
                    mLinePaint.setAlpha(alpha);
                    mTextPaint.setAlpha(alpha);
                    mDotPaint.setAlpha(alpha);
                    mRingPaint.setAlpha(alpha);
                    invalidate();
                }

                float x, lowy, upy;
                if (p.ratio >= 0) {
                    x = p.next == null ? p.x : bezier(p.x, p.nx, p.next.px, p.next.x, p.ratio);
                    lowy = p.next == null ? p.y : bezier(p.y, p.ny, p.next.py, p.next.y, p.ratio);
                    upy = p.pair.next == null ? p.pair.y : bezier(p.pair.y, p.pair.ny, p.pair.next.py, p.pair.next.y, p.ratio);
                } else {
                    x = p.prev == null ? p.x : bezier(p.x, p.px, p.prev.nx, p.prev.x, -p.ratio);
                    lowy = p.prev == null ? p.y : bezier(p.y, p.py, p.prev.ny, p.prev.y, -p.ratio);
                    upy = p.pair.prev == null ? p.pair.y : bezier(p.pair.y, p.pair.py, p.pair.prev.ny, p.pair.prev.y, -p.ratio);
                }

                canvas.drawLine(x, lowy, x, upy + mTouchSlop / 2f, mLinePaint);
                canvas.drawCircle(x, upy, mTouchSlop / 3f, mDotPaint);
                canvas.drawCircle(x, upy, mTouchSlop / 2f, mRingPaint);

                canvas.drawText(p.txt, x, lowy + (fm.bottom - fm.top) / 2f - fm.bottom - fm.top, mTextPaint);
                canvas.drawText(p.pair.txt, x, upy - (fm.bottom - fm.top) / 2f - mTouchSlop / 2f, mTextPaint);

//                canvas.drawText(p.txt, p.x, p.y + (fm.bottom - fm.top) / 2f - fm.bottom - fm.top, mTextPaint);
//                canvas.drawLine(p.x, p.y, p.pair.x, p.pair.y, mLinePaint);
//                canvas.drawText(p.pair.txt, p.pair.x, p.pair.y - (fm.bottom - fm.top) / 2f, mTextPaint);
            }
        }

        if (isAnimating) {
            canvas.restore();
            invalidate();
        }
    }

    private float computeEnter() {
        long now = getDrawingTime();
        if (enterTime == -1) {
            enterTime = now;
        }
        float passed = now - enterTime;
        float ratio = passed / enterDuration;
        if (ratio >= 1f) {
            ratio = 1f;
            isEntering = false;
        }
        return ratio;
    }

    private float computeFade() {
        long now = getDrawingTime();
        if (fadeTime == -1) {
            fadeTime = now;
        }
        float passed = now - fadeTime;
        float ratio = passed / fadeDuration;
        if (ratio >= 1f) {
            ratio = 1f;
            isFading = false;
        }
        return ratio;
    }

    private void startFade() {
        fadeTime = -1;
        isFading = true;
        invalidate();
    }

    private Point findCurrentPoint() {
        if (current != null && current.isCurrent) {
            return current;
        }
        current = null;
        Point p = mLowerPoints;
        if (p == null) {
            return null;
        }
        if (p.next == null) {
            current = p;
        } else {
            while (p != null) {
                if (p.isCurrent) {
                    current = p;
                    break;
                }
                p = p.next;
            }
        }
        return current;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLowerPoints == null || mLowerPoints.next == null) {
            return false;
        }

        mTouchPoint.set(event.getX(), event.getY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown();
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove();
                break;
            case MotionEvent.ACTION_UP:
                handleTouchUp();
                break;
            case MotionEvent.ACTION_CANCEL:
                handleTouchCancel();
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    private void handleTouchDown() {
        mDownPoint.set(mTouchPoint);
        Point current = findCurrentPoint();
        Point pair = current.pair;
        float lowx, lowy, upy;
        if (current.ratio >= 0) {
            lowx = current.next == null ? current.x : bezier(current.x, current.nx, current.next.px, current.next.x, current.ratio);
            lowy = current.next == null ? current.y : bezier(current.y, current.ny, current.next.py, current.next.y, current.ratio);
            upy = pair.next == null ? pair.y : bezier(pair.y, pair.ny, pair.next.py, pair.next.y, current.ratio);
        } else {
            lowx = current.prev == null ? current.x : bezier(current.x, current.px, current.prev.nx, current.prev.x, -current.ratio);
            lowy = current.prev == null ? current.y : bezier(current.y, current.py, current.prev.ny, current.prev.y, -current.ratio);
            upy = pair.prev == null ? pair.y : bezier(pair.y, pair.py, pair.prev.ny, pair.prev.y, -current.ratio);
        }

        if (mDownPoint.x > lowx - mTouchSlop && mDownPoint.x < lowx + mTouchSlop && mDownPoint.y > upy - mTouchSlop && mDownPoint.y < lowy + mTouchSlop) {
            mTouchState = TOUCH_STATE_DRAG;
            if (!mSnapScroller.isFinished()) {
                mSnapScroller.abortAnimation();
            }
            mTouchOffset = current.x + current.ratio * getWidth() / mPoints.size() - mDownPoint.x;
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        } else {
            int step = (int) (getWidth() * 1f / mPoints.size());
            float index = (mDownPoint.x - step / 2f) / step;
            if (index < 0 || index > mPoints.size() - 1) {
                mTouchState = TOUCH_STATE_REST;
                return;
            }
            int left = (int) Math.floor(index);
            float ratio = index - left;
            Point p = mPoints.get(left);
            lowx = p.next == null ? p.y : bezier(p.y, p.ny, p.next.py, p.next.y, ratio);
            p = p.pair;
            upy = p.next == null ? p.y : bezier(p.y, p.ny, p.next.py, p.next.y, ratio);
            if (mDownPoint.y < lowx + mTouchSlop && mDownPoint.y > upy - mTouchSlop) {
                mTouchState = TOUCH_STATE_CLICK;
            } else {
                mTouchState = TOUCH_STATE_REST;
            }
        }
    }

    private void handleTouchMove() {
        float dx = mTouchPoint.x - mDownPoint.x;
        float dy = mTouchPoint.y - mDownPoint.y;
        if (mTouchState == TOUCH_STATE_CLICK) {
            if (Math.abs(dx) > mTouchSlop / 2f || Math.abs(dy) > mTouchSlop / 2f) {
                mTouchState = TOUCH_STATE_REST;
            }
        } else if (mTouchState == TOUCH_STATE_DRAG) {
            float x = mTouchPoint.x + mTouchOffset;
            float step = getWidth() * 1.0f / mPoints.size();
            float index = (x - step / 2f) / step;
            int snap = (int) (index + 0.5f);
            float ratio = index - snap;
            if (snap < 0) {
                snap = 0;
                ratio = 0;
            } else if (snap >= mPoints.size()) {
                snap = mPoints.size() - 1;
                ratio = 0;
            }
            Point newCurrent = mPoints.get(snap);
            findCurrentPoint();
            if (newCurrent != current) {
                if (current != null) {
                    current.ratio = 0;
                    current.isCurrent = false;
                }
                current = newCurrent;
                current.isCurrent = true;
            }
            current.ratio = ratio;
            invalidate();
        }
    }

    private void handleTouchUp() {
        if (mTouchState == TOUCH_STATE_CLICK) {
            performClick();
        } else {
            snapDrag();
        }
        mTouchState = TOUCH_STATE_REST;
    }

    private void handleTouchCancel() {
        if (mTouchState == TOUCH_STATE_DRAG) {
            snapDrag();
        }
        mTouchState = TOUCH_STATE_REST;
    }

    private void snapDrag() {
        float x = mTouchPoint.x + mTouchOffset;
        float step = getWidth() * 1.0f / mPoints.size();
        float index = (x - step / 2f) / step;
        int snap = (int) (index + 0.5f);
        float ratio = index - snap;
        if (snap < 0) {
            snap = 0;
            ratio = 0;
        } else if (snap >= mPoints.size()) {
            snap = mPoints.size() - 1;
            ratio = 0;
        }
        Point newCurrent = mPoints.get(snap);
        findCurrentPoint();
        if (newCurrent != current) {
            if (current != null) {
                current.ratio = 0;
                current.isCurrent = false;
            }
            current = newCurrent;
            current.isCurrent = true;
        }
        current.ratio = ratio;
        if (ratio != 0) {
            int delta = (int) (step * ratio);
            mSnapScroller.startScroll(delta, 0, -delta, 0, Math.min(Math.abs(delta * 4), 500));
            invalidate();
        }
    }

    @Override
    public void onClick(View v) {
        if (isEntering) {
            return;
        }

        if (mPoints.isEmpty()) {
            return;
        }
        Point current = findCurrentPoint();

        int step = (int) (getWidth() * 1f / mPoints.size());
        int index = (int) (mTouchPoint.x / step);
        if (index < 0) {
            index = 0;
        } else if (index >= mPoints.size()) {
            index = mPoints.size() - 1;
        }
        Point newCurrent = mPoints.get(index);
        if (newCurrent != current && newCurrent != null) {
            newCurrent.isCurrent = true;
            if (current != null) {
                current.isCurrent = false;
            }
            startFade();
        }
    }

    private static float bezier(float p1, float p2, float p3, float p4, float t) {
        return (1-t)*(1-t)*(1-t)*p1 + 3*(1-t)*(1-t)*t*p2 + 3*(1-t)*t*t*p3 + t*t*t*p4;
    }

    public static class Data {
        public final float upper, lower;
        public final String day, night;
        public final boolean isStroke;
        public Data(int upper, int lower, String day, String night, boolean isToday) {
            this.upper = upper;
            this.lower = lower;
            this.day = day;
            this.night = night;
            this.isStroke = isToday;
        }
    }

    private static class Point {
        Point prev, next, pair;
        String txt;
        float data;
        float value;
        float x, y;
        float px, py;
        float nx, ny;
        boolean isMid = false;
        boolean isCurrent = false;
        boolean isToday;
        float ratio;

        public Point next(float data) {
            Point point = new Point();
            point.setData(data);
            point.prev = this;
            next = point;
            return point;
        }

        public void setData(float data) {
            this.data = data;
            this.value = data;
            this.txt = Integer.toString((int) data) + "°C";
        }

        public void initPoint() {
            if (prev == null || next == null) {
                px = x;
                py = y;
                nx = x;
                ny = y;
            } else {
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
                    px = (prev.x + x) / 2f;
                    py = y;
                    nx = (next.x + x) / 2f;
                    ny = y;
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
                float oldX = px;
                float oldY = py;
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
}
