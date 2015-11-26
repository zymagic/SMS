package test.blur;

import java.util.ArrayList;
import java.util.Random;

import test.blur.MainActivity.Choose;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.view.View;

public class WallpaperSwitchDrawable extends Drawable {

    private PointF center;
    private float radius;

    private Paint circlePaint;

    private View host;

    private ArrayList<Piece> pieces;

    Random random = new Random();

    private Path path = new Path();
    private Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    float[] hsv = new float[3];

    float velocity;

    public WallpaperSwitchDrawable(View host) {
        this.host = host;

        center = new PointF();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(0xff000000);
        circlePaint.setStyle(Style.STROKE);

        pieces = new ArrayList<Piece>(20);

        pathPaint.setStyle(Style.FILL);
        pathPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        center.set(bounds.width() / 2f, bounds.height() / 2f);
        radius = bounds.width() * 0.4f;

        path.reset();
        path.addRect(new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius), Direction.CCW);
        path.addCircle(center.x, center.y, radius, Direction.CW);
        path.close();

        velocity = radius * 2f / 500f * 17f;

        if (pieces.isEmpty()) {
            for (int i = 0; i < 38; i++) {
                pieces.add(new Piece());
            }
        }

        int i = 0;
        choose = new Choose(19);
        for (Piece p : pieces) {
            p.init(choose.choose());
            i++;
            if (i >= 19) {
                break;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(0xffffffff);
        canvas.saveLayer(center.x - radius, center.y - radius, center.x + radius, center.y + radius, null, Canvas.ALL_SAVE_FLAG);
//        canvas.save();
//        canvas.clipRect(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        canvas.drawCircle(center.x, center.y, radius, circlePaint);

        canvas.save();
        for (int i = 0; i < 6; i++) {
            canvas.rotate(30, center.x, center.y);
            canvas.drawLine(center.x - radius, center.y, center.x + radius, center.y, circlePaint);
        }
        canvas.restore();

        boolean more = false;
        for (Piece p : pieces) {
            more |= p.draw(canvas);
        }

        canvas.drawPath(path, pathPaint);
        canvas.restore();

//        canvas.restore();

        if (more) {
            invalidateHost();
        }

        if (isAnimating) {
            scheduleNext();
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

    private boolean isAnimating = false;
    private int count = 0;
    private float total = 2000 / 17f;
    private boolean warm = true;

    private Choose choose;
    int reverse = 2;

    public boolean isAnimating() {
        return isAnimating;
    }

    public void startAnimation() {
        startAnimation(true);
    }

    public void startAnimation(boolean click) {
        if (isAnimating) {
            return;
        }
        isAnimating = true;
        count = 0;
        for (Piece p : pieces) {
            if (!p.ready) {
                continue;
            }
            if (click || p.isStoped()) {
                p.prepare();
            }
        }
        invalidateHost();
    }

    public void burstAnimation() {
        if (!isAnimating) {
            return;
        }
        for (Piece p : pieces) {
            p.prepare();
        }
    }

    public void stopAnimation() {
        stopAnimation(false);
    }

    private void stopAnimation(boolean internal) {
        if (!isAnimating) {
            return;
        }
        isAnimating = false;
        if (internal) {
            //host.onAnimStop();
            popNext();
        } else {
            //popNext();
        }
    }

    private void invalidateHost() {
        host.invalidate();
    }

    private void scheduleNext() {
        boolean timeout = true;
        if (isAnimating) {
            count++;

            timeout = count > total;
        }

        boolean more = false;
        for (Piece p : pieces) {
            if (p.ready) {
                p.next(timeout);
                more |= !p.isStoped();
            }
        }

        if (timeout) {
            stopAnimation(true);
        }

        if (more) {
            invalidateHost();
        }
    }

    private void popNext() {
        warm = !warm;
        int size = 0;
        choose = new Choose(19);
        reverse = 2;
        for (Piece p : pieces) {
            if (!p.ready && size < 19) {
                p.init(choose.choose());
                size++;
            }
        }
    }

    class Piece {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PointF pos = new PointF(); // position
        PointF o = new PointF(); // original position
        PointF v = new PointF(); // velocity
        float r; // radius
        int index = 0;
        int offset = 10;

        boolean ready = false;
        boolean reset = true;

        void init(int index) {
            this.index = index;
            if (!ready) {
                float upper = radius / (index > 8 ? 3.5f : 2.5f);
                float lower = radius / (index > 8 ? 8f : 4.5f);
                r = random.nextFloat() * (upper - lower) + lower;
            }
            float c = 0;
            float angle = 0;
            if (index < 3) {
                offset = 20;
                c = radius - random.nextFloat() * r * 0.8f;
                angle = (float) Math.toRadians((30 * Math.pow(-1, index) * FloatMath.ceil(index / 2f)));
                o.set(c * FloatMath.sin(angle), radius - c * FloatMath.cos(angle));
            } else if (index < 8) {
                offset = 13;
                c = radius - radius / 2f - (random.nextFloat() - 0.5f) * r * 0.5f;
                float i = FloatMath.ceil((index - 3f) / 2f);
                angle = (float) Math.toRadians((30 * i - (i > 0 ? (i - 1) * 5 : 0)) * Math.pow(-1, index - 3));
                o.set(c * FloatMath.sin(angle) / FloatMath.cos(angle), radius - c);
            } else if (index < 14) {
                offset = 6;
                c = radius - radius / 1.4f - (random.nextFloat() - 0.25f) * r * 0.6f;
                float i = FloatMath.ceil((index - 7f) / 2f);
                angle = (float) Math.toRadians((32 * i - (i - 1) * 15f) * Math.pow(-1, index - 7));
                o.set(c * FloatMath.sin(angle) / FloatMath.cos(angle), radius - c);
            } else {
                offset = 0;
                c = radius - radius / 1.1f ; // - (random.nextFloat() - 0.1f) * r * 0.5f;
                float i = FloatMath.ceil((index - 14) / 2f);
                angle = (float) Math.toRadians(((75 * i - (i > 0 ? (i - 1) * 67 : 0)) * Math.pow(-1, index - 14)));
                o.set(c * FloatMath.sin(angle) / FloatMath.cos(angle) + (random.nextFloat() - 0.5f) * r, radius - c + (random.nextFloat() - 0.1f) * r * 0.5f);
            }

            if (!ready) {
                pos.x = o.x;
                pos.y = -r;
                initColor();
                v.set(0, 0);
            }

            ready = true;
            reset = true;
        }

        void next(boolean timeout) {
            if (!ready) {
                return;
            }

            float nx = pos.x + v.x;
            float ny = pos.y + v.y;
            float d = (float) Math.hypot(nx, ny - radius);
            float r = radius + this.r;
            if (d > r) {
                if (!timeout) {
                    nx = r * nx / d;
                    ny = r * (ny - radius) / d + radius;
                    reflectAt(nx, ny);
                } else {
                    ready = false;
                    v.set(0, 0);
                }
            }
            pos.set(nx, ny);
        }

        void reflectAt(float x, float y) {
            float cx = x;
            float cy = y - radius;
            float dc = (float) Math.hypot(cx, cy);
            float sin = (v.x * cy + v.y * -cx) / dc;
            float tx = 2 * sin * cy / dc;
            float ty = 2 * sin * -cx / dc;
            v.x = tx - v.x;
            v.y = ty - v.y;
        }

        void initColor() {
            if (warm) {
                if (reverse > 0) {
                    reverse--;
                    hsv[0] = random.nextInt(10) * 3 + 175f;
                } else {
                    hsv[0] = random.nextInt(10) * 12f - 60f;
                    if (hsv[0] < 0) {
                        hsv[0] += 360;
                    }
                }
            } else {
                if (reverse > 0) {
                    reverse--;
                    hsv[0] = random.nextInt(10) * 2.5f + 280f;
                } else {
                    hsv[0] = random.nextInt(10) * 12f + 120f;
                }
            }
            hsv[1] = 1;
            hsv[2] = 1;
            p.setColor(Color.HSVToColor(0x33, hsv));
        }

        void prepare() {
            float radian = (float) (Math.PI * (random.nextFloat() * 0.25f + 0.375f));
            float rv = (random.nextFloat() * 0.5f + 0.5f) * velocity;
            v.set(FloatMath.cos(radian) * rv, FloatMath.sin(radian) * rv);
        }

        boolean draw(Canvas canvas) {
            if (!ready) {
                return false;
            }
            boolean ret = false;
            if (!isAnimating) {
                if (!isStoped()) {
                    next(true);
                    ret = true;
                } else if (reset) {
                    if (offset-- > 0) {
                        ret = true;
                    } else {
                        float d = (float) Math.hypot(pos.x - o.x, pos.y - o.y);
                        if (pos.y < o.y && d > r / 8) {
                            pos.x += (o.x - pos.x) / d * r / 16f;
                            pos.y += (o.y - pos.y) / d * velocity;
                            ret = true;
                        } else {
                            reset = false;
                        }
                    }
                }
            }
            canvas.drawCircle(pos.x + center.x, center.y + radius - pos.y, r, p);
            canvas.drawText(Integer.toString(index), pos.x + center.x, center.y + radius - pos.y - (fm.top + fm.bottom) / 2f, tp);
            return ret;
        }

        boolean isStoped() {
            return v.x * v.y == 0f;
        }
    }

    static Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    static FontMetrics fm;
    static {
        tp.setColor(0xff000000);
        tp.setTextSize(20);
        tp.setTextAlign(Paint.Align.CENTER);
        fm = tp.getFontMetrics();
    }
}
