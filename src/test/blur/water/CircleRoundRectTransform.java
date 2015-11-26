package test.blur.water;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

public class CircleRoundRectTransform implements PathTransform {

    private PointF c1 = new PointF();
    private PointF c2 = new PointF();
    private float radius;
    private RectF rect = new RectF();

    public CircleRoundRectTransform(float c1x, float c2x, float y, float radius) {
        c1.set(c1x, y);
        c2.set(c2x, y);
        this.radius = radius;
    }

    public void update(float t, Path path) {
        if (t <= 0) {
            rect.set(c1.x - radius, c1.y - radius, c1.x + radius, c1.y + radius);
            path.addRoundRect(rect, radius * 0.25f, radius * 0.25f, Path.Direction.CCW);
            return;
        } else if (t >= 1f) {
            path.addCircle(c2.x, c2.y, radius, Path.Direction.CCW);
            return;
        }

        float c1x = t > 0.5f ? c1.x + (c2.x - c1.x - radius) * (t - 0.5f) / 0.5f : c1.x;
        float c2x = t < 0.5f ? c1.x + radius + (c2.x - c1.x - radius) * t / 0.5f : c2.x;

        float r1 = radius * (1 - t);
        float r2 = radius * t;

        if (t < 0.5f) {
            float rr = r1 * (0.25f + 1.5f * t);
            float crx = c1x + r1 - rr;
            float cry = c1.y - r1 + rr;

            float rx = c2x - crx;
            float ry = c2.y - cry;
            float nx = -ry;
            float ny = rx;
            float cl = (float) Math.hypot(rx, ry);
            nx /= cl;
            ny /= cl;

            float lcl = cl * rr / (rr + r2);
            float lrl = rr * rr / lcl;
            float lnl = (float) Math.sqrt(rr * rr - lrl * lrl);
            if (ny > 0) {
                ny *= -1;
                nx *= -1;
            }
            float s1x = crx + rx / cl * lrl + nx * lnl;
            float s1y = cry + ry / cl * lrl + ny * lnl;

            float rcl = cl * r2 / (rr + r2);
            float rrl = r2 * r2 / rcl;
            float rnl = (float) Math.sqrt(r2 * r2 - rrl * rrl);
            if (ny < 0) {
                ny *= -1;
                nx *= -1;
            }
            float s2x = c2x + nx * rnl - rx / cl * rrl;
            float s2y = c2.y + ny * rnl - ry / cl * rrl;

            float cpx = s1x + (s2x - s1x) * (s1y - c1.y) / (s1y - c1.y + c1.y - s2y);
            float cpy = c1.y;

            rect.set(c1x - r1, c1.y - r1, c1x + r1, c1.y + r1);
            path.addRoundRect(rect, rr, rr, Path.Direction.CW);
            path.addCircle(c2x, c2.y, r2, Path.Direction.CW);
            path.moveTo(s1x, s1y);
            path.quadTo(cpx, cpy, s2x, 2 * c2.y - s2y);
            path.lineTo(s2x, s2y);
            path.quadTo(cpx, cpy, s1x, 2 * c1.y - s1y);
        } else {
            float cpx = c1x + (c2x - c1x) * r1 / (r1 + r2);
            float s1x = c1x + r1 * r1 / (cpx - c1x);
            float dy = (float) Math.sqrt(r1 * r1 - (s1x - c1x) * (s1x - c1x));
            float s1y = c1.y - dy;
            float s2x = c2x - r2 * r2 / (c2x - cpx);
            dy = (float) Math.sqrt(r2 * r2 - (s2x - c2x) * (s2x - c2x));
            float s2y = c2.y + dy;

            path.addCircle(c1x, c1.y, r1, Path.Direction.CW);
            path.addCircle(c2x, c2.y, r2, Path.Direction.CW);
            path.moveTo(s1x, s1y);
            path.quadTo(cpx, c1.y, s2x, 2 * c2.y - s2y);
            path.lineTo(s2x, s2y);
            path.quadTo(cpx, c1.y, s1x, 2 * c1.y - s1y);
        }
    }
}
