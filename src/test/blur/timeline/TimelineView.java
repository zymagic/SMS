package test.blur.timeline;

import static test.blur.timeline.TimelineView.Moment.baselineCenterOffset;
import static test.blur.timeline.TimelineView.Moment.dotPaint;
import static test.blur.timeline.TimelineView.Moment.dotRadius;
import static test.blur.timeline.TimelineView.Moment.ringPaint;
import static test.blur.timeline.TimelineView.Moment.ringRadius;
import static test.blur.timeline.TimelineView.Moment.ringStroke;
import static test.blur.timeline.TimelineView.Moment.textPaint;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TimelineView extends View {

    private ArrayList<Moment> moments = new ArrayList<TimelineView.Moment>();
    private Paint mLinePaint;

    private float mLastY;

    public TimelineView(Context context) {
        this(context, null);
    }

    public TimelineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        dotRadius = density * 5;
        ringRadius = density * 36;
        ringStroke = density;
        if (ringPaint == null) {
            ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeWidth(ringStroke);
            ringPaint.setColor(0x80ffffff);
        }
        if (dotPaint == null) {
            dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotPaint.setStyle(Paint.Style.FILL);
            dotPaint.setColor(0xccffffff);
        }
        if (textPaint == null) {
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(0xffffffff);
            textPaint.setTextSize(39);
            FontMetrics fm = textPaint.getFontMetrics();
            baselineCenterOffset = -(fm.top + fm.bottom) / 2f;
        }
        mLinePaint = new Paint();
        mLinePaint.setColor(0xffffffff);
        mLinePaint.setStrokeWidth(density);

        addMoment(new Moment("2014.11.15", "birth or hola", 0xac79f8));
        addMoment(new Moment("2014.12.01", "hola shine", 0x4aefa3));
        addMoment(new Moment("2015.01.26", "you join", 0x4aefed));
        addMoment(new Moment("2014.03.05", "jellyfish release", 0xe1ef4a));
        addMoment(new Moment("2014.06.01", "boost+", 0x56ef4a));
    }

    public void addMoment(Moment m) {
        Moment last = moments.isEmpty() ? null : moments.get(moments.size() - 1);
        if (last != null) {
            last.next = m;
            m.prev = last;
        }
        moments.add(m);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int count = moments.size();
        float divide = h / 4f;
        for (int i = 0; i < count; i++) {
            Moment m = moments.get(i);
            m.y = (i + 1) * divide;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(0, -Moment.sGlobalOffset);
        float width = getWidth();
        float height = getHeight();
        long drawingTime = getDrawingTime();
        boolean more = false;
        final int count = moments.size();
        for (int i = 0; i < count; i++) {
            Moment m = moments.get(i);
            if (i == 0) {
                m.start(width, height);
            }
            more |= m.draw(canvas, drawingTime, width, height, true);
        }
        canvas.drawLine(width / 2f, Moment.sGlobalOffset, width / 2f, height + Moment.sGlobalOffset, mLinePaint);
        for (int i = 0; i < count; i++) {
            Moment m = moments.get(i);
            more |= m.draw(canvas, drawingTime, width, height, false);
        }
        canvas.restore();
        if (more) {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(event);
    }

    static class Moment {
        static final int STATE_PENDING = 0;
        static final int STATE_RUNNING = 1;
        static final int STATE_END = 2;
        static final long DURATION = 3000;
        static final long START_OFFSET = 300;

        static float dotRadius, ringRadius, ringStroke;
        static float baselineCenterOffset;
        static Paint dotPaint;
        static Paint ringPaint;
        static Paint textPaint;
        static float sGlobalOffset;

        String date;
        String desc;
        Moment next, prev;

        Paint bubblePaint;
        int startColor, endColor;

        long startTime = -1;

        float y;

        int state;

        public Moment(String date, String desc, int color) {
            this.date = date;
            this.desc = desc;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            startColor = Color.rgb((int) (r * 0.7f), (int) (g * 0.7f), (int) (b * 0.7f));
            endColor = Color.rgb((int) Math.min(r * 1.5f, 255), (int) Math.min(g * 1.5f, 255), (int) Math.min(b * 1.5f, 255));
        }

        public boolean draw(Canvas canvas, long drawingTime, float width, float height, boolean bubbleOnly) {
            if (state == STATE_PENDING) {
                return true;
            }
            if (state == STATE_RUNNING) {
                if (startTime == -1) {
                    startTime = drawingTime;
                }
                float passed = drawingTime - START_OFFSET - startTime;
                if (passed <= 0) {
                    return true;
                }
                float ratio = passed / DURATION;
                boolean expired = ratio >= 1f;
                if (ratio >= 1f) {
                    ratio = 1f;
                }

                if (bubbleOnly) {
                    float bubbleRatio = ratio < 0.6f ? 1 - (1 - (ratio - 0.3f) / 0.3f) * (1 - (ratio - 0.3f) / 0.3f) : 1f;
                    drawBubble(canvas, bubbleRatio, width, height);
                } else {
                    float dotRatio = ratio < 0.3f ? 1 - (1 - ratio / 0.3f) * (1 - ratio / 0.3f) : 1f;
                    float textRatio = 1 - (1 - (ratio - 0.6f) / 0.4f) * (1 - (ratio - 0.6f) / 0.4f);
                    drawCircle(canvas, dotRatio, width, height);
                    drawText(canvas, textRatio, width, height);
                }

                if (expired) {
                    state = STATE_END;
                    if (next != null) {
                        next.start(width, height);
                    }
                }
                return true;
            } else {
                if (bubbleOnly) {
                    if (next == null || next.state == STATE_RUNNING) {
                        drawBubble(canvas, 1f, width, height);
                    }
                } else {
                    drawCircle(canvas, 1f, width, height);
                    drawText(canvas, 1f, width, height);
                }
                return false;
            }
        }

        public void drawCircle(Canvas canvas, float ratio, float width, float height) {
            if (ratio <= 0) {
                return;
            }
            float cy = y + ((prev == null ? 0 : prev.y) - y) * (1 - ratio);
            if (next != null && prev != null) {
                sGlobalOffset = cy - height / 2f;
            }
            canvas.drawCircle(width / 2f, cy, dotRadius, dotPaint);
        }

        public void drawText(Canvas canvas, float ratio, float width, float height) {
            if (ratio <= 0) {
                return;
            }
            float offset = width / 4f * (1 - ratio);
            int alpha = (int) (ratio * ratio * ratio * 255);
            textPaint.setAlpha(alpha);
            canvas.drawText(date, width / 4f + offset, y + baselineCenterOffset, textPaint);
            canvas.drawText(desc, width * 0.75f - offset, y + baselineCenterOffset, textPaint);
        }

        public void drawBubble(Canvas canvas, float ratio, float width, float height) {
            if (ratio < 0) {
                return;
            }
            if (ratio < 1f) {
                float radius = (float) (Math.hypot(Math.max(y, height - y), width / 2f) * ratio);
                canvas.drawCircle(width / 2f, y, radius, bubblePaint);
            } else {
                canvas.drawPaint(bubblePaint);
            }

            float ringAlpha = 1 - ratio * 2;
            if (ringAlpha > 0) {
                canvas.drawCircle(width / 2f, y, ringRadius * (1 - ringAlpha), ringPaint);
            }
        }

        public void start(float width, float height) {
            if (bubblePaint == null) {
                bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }
            if (state == STATE_PENDING) {
                state = STATE_RUNNING;
                bubblePaint.setShader(new LinearGradient(0, 0, 0, height, startColor, endColor, TileMode.CLAMP));
            }
        }

    }
}
