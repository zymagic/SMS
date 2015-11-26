package test.blur;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

public class TaskManagerAnimationView extends View {

    private PointF mCenter = new PointF();
    private PointF mOriginScrollPosition = new PointF();
    private View mScrollView;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mOval = new RectF();

    public TaskManagerAnimationView(Context context) {
        super(context);
        mPaint.setColor(0xffffffff);
        setWillNotDraw(false);

        distance = 180;
        distanceOffset = 21;
        bigRadius = 4.5f;
        smallRadius = 3f;
        absorbRadius = 60;

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setCenter(float x, float y, View scrollContainer) {
        mCenter.set(x, y);
        mScrollView = scrollContainer;
        if (scrollContainer != null) {
            mOriginScrollPosition.set(mScrollView.getScrollX(), mScrollView.getScrollY());
        }
    }

    public void onClearFinishing() {
        //TODO: end particles and start screen capture
    }

    private LinkedList<Dot> mDots = new LinkedList<Dot>();
    private static final int MAX_DOTS = 25;
    private static final int DOT_BATCH_SIZE = 5;

    private class Dot {
        float radius;
        long startTime = -1;
        long duration;
        PointF origin;
        boolean end;

        Dot(float x, float y, float radius, long duration) {
            origin = new PointF(x, y);
            this.radius = radius;
            this.duration = duration;
        }

        public boolean draw(Canvas canvas, Paint paint, long drawingTime, float tarX, float tarY) {
            if (end) {
                return false;
            }
            float ratio = calculateAnimation(drawingTime);
            float moveRatio = 1 - (1 - ratio) * (1 - ratio);
            float alphaRatio = 1 - ratio * ratio;
            float scaleRatio = 1 - ratio;
            paint.setAlpha((int) (alphaRatio * 255));
            float rx = tarX - origin.x;
            float ry = tarY - origin.y;
            float length = (float) Math.hypot(rx, ry);
            if (length == 0) {
                return false;
            }
            rx /= length;
            ry /= length;
            float dx = tarX - absorbRadius * rx;
            float dy = tarY - absorbRadius * ry;
            float cx = origin.x + (dx - origin.x) * moveRatio;
            float cy = origin.y + (dy - origin.y) * moveRatio;
            canvas.drawCircle(cx, cy, radius * scaleRatio, mPaint);
            return true;
        }

        private float calculateAnimation(long now) {
            if (startTime == -1) {
                startTime = now;
            }
            float ratio = duration <= 0 ? 1f : (now - startTime) * 1.0f / duration;
            if (ratio >= 1f) {
                ratio = 1f;
                end = true;
            }
            return ratio;
        }
    }

    private static final float RADIAN_PER_DEGREE = (float) (Math.PI / 180);
    private float distance;
    private float distanceOffset;
    private float bigRadius, smallRadius, absorbRadius;
    private boolean isOuter;
    private float degree;

    private Random random = new Random(47);

    private Dot nextDot() {
        float nextDistance = isOuter ? randomFrom(distance, distance + distanceOffset) : randomFrom(distance - distanceOffset, distance);
        isOuter = !isOuter;
        float nextDegree = degree + randomFrom(25, 35) + (isOuter ? 90 : 0);
        degree = nextDegree;
        float nextRadian = nextDegree * RADIAN_PER_DEGREE;
        float nextX = (float) (Math.cos(nextRadian) * nextDistance + mCenter.x);
        float nextY = (float) (Math.sin(nextRadian) * nextDistance + mCenter.y);
        long nextDuration = (long) randomFrom(700, 1000);
        return new Dot(nextX, nextY, randomFrom(smallRadius, bigRadius), nextDuration);
    }

    private float randomFrom(float low, float high) {
        return low + (high - low) * random.nextFloat();
    }

    private long lastAddTime;
    private float rotation;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0x80000000);
        long drawingTime = getDrawingTime();
        if (mDots.size() < MAX_DOTS && drawingTime - lastAddTime > 100) {
            for (int i = Math.min(DOT_BATCH_SIZE, MAX_DOTS - mDots.size()); i > 0; i--) {
                mDots.addLast(nextDot());
            }
            lastAddTime = drawingTime;
        }
        float tarX, tarY;
        if (mScrollView == null) {
            tarX = mCenter.x;
            tarY = mCenter.y;
        } else {
            tarX = mCenter.x + mOriginScrollPosition.x - mScrollView.getScrollX();
            tarY = mCenter.y + mOriginScrollPosition.y - mScrollView.getScrollY();
        }


        mStrokePaint.setColor(0x28000000);
        mStrokePaint.setStrokeWidth(41);
        canvas.drawCircle(tarX, tarY, absorbRadius, mStrokePaint);

        mStrokePaint.setColor(0x66000000);
        mStrokePaint.setStrokeWidth(29);
        canvas.drawCircle(tarX, tarY, absorbRadius, mStrokePaint);

        mStrokePaint.setColor(0xff54d5e8);
        mStrokePaint.setStrokeWidth(9.6f);
//        canvas.drawCircle(tarX, tarY, 60, mStrokePaint);
        canvas.drawArc(mOval, rotation, 100, false, mStrokePaint);
        rotation += 57;
        if (rotation > 360f) {
            rotation -= 360;
        }

        Iterator<Dot> iter = mDots.iterator();
        while (iter.hasNext()) {
            Dot d = iter.next();
            if (!d.draw(canvas, mPaint, drawingTime, tarX, tarY)) {
                iter.remove();
            }
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setCenter(w / 2f, h / 2f, this);
        mOval.set(w / 2f - absorbRadius, h / 2f - absorbRadius, w / 2f + absorbRadius, h / 2f + absorbRadius);
    }
}
