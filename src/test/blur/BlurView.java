package test.blur;

import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class BlurView extends View {

    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PointF center = new PointF();

    private float radius;

    private LinkedList<Item> items = new LinkedList<Item>();

    private boolean isSizeResolved = false;

    private float[] hsv = new float[3];

    private long startTime;

    private boolean isTimeout = false;

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circlePaint.setStyle(Style.STROKE);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        center.set(getMeasuredWidth() / 2f,  getMeasuredHeight() / 2f);
        radius = center.x * 0.8f;
        isSizeResolved = true;
        if (items.isEmpty()) {
            Random r = new Random();
            float upper = (int) (radius / 3f);
            float lower = (int) (radius / 6f);
            float seed = upper - lower;
            for (int i = 0; i < 20; i++) {
                Item item = new Item();
                item.r = r.nextFloat() * seed + lower;
                item.c = radius - r.nextFloat() * radius / 3;
                double radian = Math.PI * r.nextDouble();
                item.pos.x = (float) ((item.c - item.r) * Math.cos(radian));
                item.pos.y = (float) (radius - (item.c - item.r) * Math.sin(radian));
                int red = r.nextInt(256);
                int green = r.nextInt(256);
                int blue = r.nextInt(256);
                item.p.setColor(Color.rgb(red, green, blue));
                item.cw = r.nextInt(2) == 0;
                item.sp = r.nextInt(2) == 0;
                item.vp = r.nextInt(2) == 0;
                items.add(item);
            }
        }
    }

    public void start() {
        if (!isSizeResolved) {
            return;
        }

        startTime = System.currentTimeMillis();
        isTimeout = false;

        float v = getResources().getDisplayMetrics().density * 4;

        Random r = new Random();
        for (Item item : items) {
            float radian = (float) (Math.PI * 2 * r.nextFloat());
            float rv = v * (r.nextFloat() / 2f + 0.5f);
            item.v.set((float) (Math.cos(radian) * rv), (float) (Math.sin(radian) * rv));
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean more = false;
        for (Item item : items) {
            canvas.drawCircle(item.pos.x + center.x, center.y + radius - item.pos.y, item.r, item.p);
            if (item.v.x * item.v.y != 0f) {
                more = true;
            }
        }

        canvas.drawCircle(center.x, center.y, radius, circlePaint);

        if (!isTimeout) {
            isTimeout = System.currentTimeMillis() - startTime > 3000;
        }

        if (more) {
            next();
        } else {
            Toast.makeText(getContext(), "end", Toast.LENGTH_SHORT).show();
        }
    }

    private void next() {
        for (Item item : items) {
            float nx = item.pos.x + item.v.x;
            float ny = item.pos.y + item.v.y;
            float d = (float) Math.hypot(nx, ny - radius);
            float r = radius - item.r;
            if (d > r) {
                nx = r * nx / d;
                ny = r * (ny - radius) / d + radius;
                if (!isTimeout || ny > radius) {
                    float rx = item.v.x;
                    float ry = item.v.y;
                    float cx = nx;
                    float cy = ny - radius;
                    float dc = (float) Math.hypot(cx, cy);
                    float s = (rx * cy + ry * -cx) / dc;
                    float tx = 2 * s * cy / dc;
                    float ty = 2 * s * -cx / dc;
                    float vx = tx - rx;
                    float vy = ty - ry;
                    item.v.x = vx;
                    item.v.y = vy;
                } else {
                    item.v.set(0, 0);
                }
            } else if (isTimeout && d > item.c - item.r && ny < radius) {
                item.v.set(0, 0);
            }
            item.pos.set(nx, ny);

            if (!isTimeout) {
                int color = item.p.getColor();
                Color.colorToHSV(color, hsv);
                hsv[0] += item.cw ? 0.977f : -0.977f;
                hsv[1] += item.sp ? 0.0047f : -0.0047f;
                hsv[2] += item.vp ? 0.0089f : -0.0089f;

                if (hsv[0] >= 360) {
                    hsv[0] -= 360;
                } else if (hsv[0] < 0) {
                    hsv[0] += 360;
                }

                if (hsv[1] > 1.0f) {
                    hsv[1] = 1.0f;
                    item.sp = false;
                } else if (hsv[1] < 0.75f) {
                    item.sp = true;
                }

                if (hsv[2] > 1.0f) {
                    hsv[2] = 1.0f;
                    item.vp = false;
                } else if (hsv[2] < 0.75f) {
                    item.vp = true;
                }

                item.p.setColor(Color.HSVToColor(hsv));
                item.p.setAlpha(0xcc);
            }
        }
        invalidate();
    }

    static class Item {
        PointF pos = new PointF();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float r;
        float c;
        boolean cw = true;
        boolean sp = true;
        boolean vp = true;
        PointF v = new PointF();
    }
}
