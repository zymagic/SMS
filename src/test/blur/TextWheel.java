package test.blur;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class TextWheel extends View {

    private String[] contents;
    private PointF center = new PointF();
    private Paint zoomPaint;
    private Paint normalPaint;
    private FontMetrics fontMetrics;
    private float lineWidth, lineHeight, contentHeight;
    private float endSpacing;

    // these can load from xml
    private float zoomFactor = 2f;
    private float fadeFactor = 1.0f;
    private float lineGap = 15;
    private int sideCount = 2;

    private static final boolean ENABLE_CENTER_LINE = true;//EnvConstants.LOCAL_DEBUG;
    private Paint centerLinePaint;

    private boolean isLayouting = true;

    public TextWheel(Context context) {
        this(context, null);
    }

    public TextWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // the attributes that can read from XML:
        //  textAppearance, zoomFactor, fadeFactor, lineGap, sideCount

        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

//        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextWheel, defStyleAttr, 0);

        float textSize = dm.density * 15; //a.getDimension(R.styleable.TextWheel_android_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, dm));
        int textColor = 0xffffffff; //a.getColor(R.styleable.TextWheel_android_textColor, 0xffffffff);
        lineGap = dm.density * 15; //a.getDimension(R.styleable.TextWheel_lineGap, dm.density * 15);
//        sideCount = a.getInt(R.styleable.TextWheel_sideCount, sideCount);

//        TypedValue v = a.peekValue(R.styleable.TextWheel_zoomFactor);
//        if (v != null) {
//            if (v.type == TypedValue.TYPE_FLOAT) {
//                zoomFactor = v.getFloat();
//            } else {
//                zoomFactor = TypedValue.complexToFloat(v.data);
//            }
//        }
//
//        v = a.peekValue(R.styleable.TextWheel_fadeFactor);
//        if (v != null) {
//            if (v.type == TypedValue.TYPE_FLOAT) {
//                fadeFactor = v.getFloat();
//            } else {
//                fadeFactor = TypedValue.complexToFloat(v.data);
//            }
//        }

//        a.recycle();

        zoomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zoomPaint.setColor(textColor);
        zoomPaint.setTextAlign(Paint.Align.CENTER);
        zoomPaint.setTextSize(textSize);

        normalPaint = new Paint(zoomPaint);
        normalPaint.setAlpha((int) (0xff * fadeFactor));

        fontMetrics = new FontMetrics();
        normalPaint.getFontMetrics(fontMetrics);
        lineHeight = fontMetrics.bottom - fontMetrics.top;

        initTouch(context);

        if (ENABLE_CENTER_LINE) {
            centerLinePaint = new Paint();
            centerLinePaint.setColor(0x8000ffff);
        }
    }

    public void setContents(String[] content) {
        this.contents = content;
        onContentChanged();
    }

    public void setSideCount(int count) {
        if (sideCount != count) {
            sideCount = count;
            requestLayout();
            invalidate();
        }
    }

    // 设置中心文字的缩放系数
    public void setZoomFactor(float f) {
        if (zoomFactor != f) {
            zoomFactor = f;
            requestLayout();
            invalidate();
        }
    }

    // 设置非中心文字的透明度
    public void setFadeFactor(float f) {
        if (fadeFactor != f) {
            fadeFactor = f;
            invalidate();
        }
    }

    public void setTextSize(float size) {
        if (size > 0 && size != zoomPaint.getTextSize()) {
            zoomPaint.setTextSize(size);
            normalPaint.setTextSize(size);
            normalPaint.getFontMetrics(fontMetrics);
            lineHeight = fontMetrics.bottom - fontMetrics.top;
            requestLayout();
            invalidate();
        }
    }

    public void setLineGap(float gap) {
        if (lineGap != gap) {
            lineGap = gap;
            requestLayout();
        }
    }

    public void setTextColor(int color) {
        if (color != zoomPaint.getColor()) {
            zoomPaint.setColor(color);
            normalPaint.setColor(color);
            normalPaint.setAlpha((int) (Color.alpha(color) * fadeFactor));
            invalidate();
        }
    }

    public void setTypeface(Typeface ttf) {
        zoomPaint.setTypeface(ttf);
        normalPaint.setTypeface(ttf);
        normalPaint.getFontMetrics(fontMetrics);
        lineHeight = fontMetrics.bottom - fontMetrics.top;
        requestLayout();
        invalidate();
    }

    private boolean isInvalid() {
        return contents == null || contents.length < 3;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        isLayouting = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInvalid()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int lineCount = 2 * sideCount + 1;

        int height = (int) ((lineCount - 1) * lineHeight + (lineCount - 1) * lineGap + lineHeight * zoomFactor + 0.5f);
        height += getPaddingTop() + getPaddingBottom();
        height = resolveSize(height, heightMeasureSpec);

        int width = (int) (lineWidth * zoomFactor + getPaddingLeft() + getPaddingRight() + 0.5f);
        width = resolveSize(width, widthMeasureSpec);

        setMeasuredDimension(width, height);

        center.set(getMeasuredWidth() / 2f, getMeasuredHeight() / 2f);
        endSpacing = sideCount * (lineHeight + lineGap);

        isLayouting = false;

        setCurrentPosition(currentPosition);
    }

    // override this to enable vertical fading edge
    @Override
    protected int computeVerticalScrollRange() {
        if (isInvalid()) {
            return super.computeVerticalScrollRange();
        }
        return (int) (contentHeight + 2 * endSpacing + 0.5f);
    }

    private void onContentChanged() {
        if (isInvalid()) {
            return;
        }

        final Paint p = normalPaint;

        if (p == null) {
            return;
        }

        float max = 0;

        for (String s : contents) {
            final float w = p.measureText(s);
            if (w > max) {
                max = w;
            }
        }

        final float contentHeight = contents.length * lineHeight + lineGap * (contents.length - 1) + (zoomFactor - 1) * lineHeight;


        if (lineWidth != max || contentHeight != this.contentHeight) {
            lineWidth = max;
            this.contentHeight = contentHeight;
            requestLayout();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInvalid()) {
            return;
        }

        final int sy = getScrollY();

        canvas.save();
        canvas.clipRect(getPaddingLeft(), sy + getPaddingTop(), getWidth() - getPaddingRight(), sy + getHeight() - getPaddingBottom());

        final int bottom = getHeight() - getPaddingBottom();
        int startIndex = (int) ((sy - endSpacing) / (lineHeight + lineGap));

        if (startIndex < 0) {
            startIndex = 0;
        } else if (startIndex >= contents.length) {
            startIndex = contents.length - 1;
        }

        // 因子f为文字中线到布局中线的距离与M的比值
        // 其中M = H * zoom + H + lineGap, H = lineHeight / 2, 即M为静止后中心文字的中线与上下相邻的文字的中线的间距
        // 计算因子f的方程为top + H * scale - cY = M * f
        // 其中 scale = zoom - (zoom - 1) * |f|, 在中线上方时，f为负值;
        // 所以 f = (top + H * zoom - cY) / [M + H * (a - 1) * (up ? -1 : 1)]
        final float H = lineHeight / 2;
        final float M = lineHeight * (zoomFactor + 1) / 2f + lineGap;

        float startY = getPaddingTop() + startIndex * (lineHeight + lineGap) + endSpacing;
        for (int i = startIndex; i < contents.length; i++) {
            final boolean up = startY + H * zoomFactor - sy < center.y;
            final float factor = (startY - sy - center.y + zoomFactor * H) / (M + H * (zoomFactor - 1) * (up ? -1 : 1));
            if (factor <= -0.999f || factor >= 0.999f) {
                canvas.drawText(contents[i], center.x, startY - fontMetrics.top, normalPaint);
            } else {
                float f = Math.abs(factor);
                if (f < 0.0001f) {
                    f = 0f;
                }

                if (fadeFactor < 1f) {
                    zoomPaint.setAlpha((int) ((1 - (1 - fadeFactor) * f) * 0xff));
                }

                if (zoomFactor != 1f) {
                    final float scale = zoomFactor - (zoomFactor - 1) * f;
                    canvas.save();
                    canvas.translate(0, startY - fontMetrics.top * scale);
                    canvas.scale(scale, scale, center.x, 0);
                    canvas.drawText(contents[i], center.x, 0, zoomPaint);
                    canvas.restore();
                    startY += lineHeight * (scale - 1);
                } else {
                    canvas.drawText(contents[i], center.x, startY, zoomPaint);
                }
            }
            startY += lineHeight;
            if (startY - sy > bottom) {
                break;
            }
            startY += lineGap;
        }

        canvas.restore();

        if (ENABLE_CENTER_LINE) {
            canvas.drawLine(0, center.y + sy, getWidth(), center.y + sy, centerLinePaint);
        }
    }

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_DOWN = 1;
    private static final int TOUCH_STATE_SCROLL = 2;

    private float lastMotionX, lastMotionY;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private static final int MIN_FLING_VELOCITY = 300;
    private int currentPosition, nextPosition;
    private int touchState;
    private int touchSlop;
    private boolean isScrolling = false;

    private void initTouch(Context context) {
        scroller = new Scroller(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (contents == null || contents.length < 3) {
            return false;
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                touchState = scroller.isFinished() ? TOUCH_STATE_DOWN : TOUCH_STATE_SCROLL;

                lastMotionX = event.getX();
                lastMotionY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                final int dy = (int) (y - lastMotionY);
                if (touchState != TOUCH_STATE_SCROLL) {
                    final int dx = (int) (event.getX() - lastMotionX);
                    if (dy > touchSlop || -dy > touchSlop || dx > touchSlop || -dx > touchSlop) {
                        touchState = TOUCH_STATE_SCROLL;
                        lastMotionY = y;
                    }
                } else {
                    if (isOverScroll()) {
                        scrollBy(0, -dy / 2);
                    } else {
                        scrollBy(0, -dy);
                    }
                    lastMotionY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                final boolean moved = touchState == TOUCH_STATE_SCROLL;
                touchState = TOUCH_STATE_REST;
                if (moved) {
                    if (isOverScroll()) {
                        snap();
                    } else {
                        velocityTracker.computeCurrentVelocity(1000);
                        final float vy = velocityTracker.getYVelocity();
                        if (vy > MIN_FLING_VELOCITY || -vy > MIN_FLING_VELOCITY) {
                            onFling(-vy);
                        } else {
                            snap();
                        }
                    }
                } else {
                    final float curY = event.getY();
                    final int clickedIndex;
                    if (curY < center.y) {
                        clickedIndex = (int) ((curY - getPaddingTop() + getScrollY() - endSpacing + lineGap / 2) / (lineHeight + lineGap));
                    } else {
                        clickedIndex = (int) ((curY - getPaddingTop() + getScrollY() - endSpacing - lineGap / 2 - lineHeight * zoomFactor) / (lineHeight + lineGap) + 1);
                    }
                    snapTo(clickedIndex);
                    performClick();
                }
                velocityTracker.recycle();
                velocityTracker = null;
                touchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                snap();
                touchState = TOUCH_STATE_REST;
                break;
        }
        return true;
    }

    private boolean isOverScroll() {
        return getScrollY() < 0 || getScrollY() + getHeight() - getPaddingTop() - getPaddingBottom() > contentHeight + 2 * endSpacing;
    }

    private void onFling(float vy) {
        scroller.fling(0, getScrollY(), 0, (int) vy / 2, 0, 0, 0, (int) (contentHeight + 2 * endSpacing - getHeight() + getPaddingTop() + getPaddingBottom()));
        nextPosition = -1;
        invalidate();
    }

    private void snap() {
        final int index = (int) ((getScrollY() - endSpacing + center.y - getPaddingTop()) / (lineHeight + lineGap));
        snapTo(index);
    }

    private void snapTo(int index) {
        index = Math.max(0, Math.min(index, contents.length - 1));
        final int sy = (int) ((lineHeight + lineGap) * index + lineHeight * zoomFactor / 2 + endSpacing - center.y) + getPaddingTop();

        nextPosition = index;

        scroller.startScroll(0, getScrollY(), 0, sy - getScrollY(), 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (!isInvalid()) {
            if (touchState == TOUCH_STATE_REST) {
                if (scroller.computeScrollOffset()) {
                    isScrolling = true;
                    scrollTo(0, scroller.getCurrY());
                    invalidate();
                } else {
                    if (nextPosition != currentPosition) {
                        snap();
                        currentPosition = nextPosition;
                        performChangePosition();
                    } else {
                        isScrolling = false;
                    }
                }
            }
        }
        super.computeScroll();
    }

    public int getCurrentPosition() {
        return nextPosition;
    }

    public int setCurrentPosition(int pos) {
        if (isInvalid()) {
            return currentPosition;
        }

        pos = Math.max(0, Math.min(pos, contents.length - 1));

        if (isLayouting) {
            currentPosition = pos;
            return pos;
        }

        final int sy = (int) ((lineHeight + lineGap) * pos + lineHeight * zoomFactor / 2 + endSpacing - center.y) + getPaddingTop();

        nextPosition = pos;

        scrollTo(0, sy);
        invalidate();
        return pos;
    }

    public int scrollToPosition(int pos) {
        snapTo(pos);
        return nextPosition;
    }

    public static interface OnPositionChangeListener {
        void onPositionChanged(int pos);
    }

    private OnPositionChangeListener posListener;

    public void setOnPositionChangeListener(OnPositionChangeListener l) {
        posListener = l;
    }

    public static interface OnConfigurationChangeListener {
        void onConfigurationChanged();
    }

    private OnConfigurationChangeListener configurationChangeListener;

    public void setOnConfigurationChangeListener(OnConfigurationChangeListener l) {
        configurationChangeListener = l;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (configurationChangeListener != null) {
            configurationChangeListener.onConfigurationChanged();
        }
    }

    private Runnable changePosition = new Runnable() {

        @Override
        public void run() {
            if (posListener != null) {
                if (touchState == TOUCH_STATE_REST && !isScrolling) {
                    posListener.onPositionChanged(currentPosition);
                } else {
                    postDelayed(this, 300);
                }
            }
        }
    };

    private void performChangePosition() {
        if (posListener == null) {
            return;
        }
        removeCallbacks(changePosition);
        postDelayed(changePosition, 300);
    }
}
