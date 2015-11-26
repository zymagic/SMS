package test.blur.water;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Path.Direction;

public class CircleTransform implements PathTransform {

    PointF c1 = new PointF();
    PointF c2 = new PointF();
    PointF cp = new PointF();

    PointF s1 = new PointF();
    PointF s2 = new PointF();
    PointF s3 = new PointF();
    PointF s4 = new PointF();

    float r;
    float f;

    public CircleTransform(float c1x, float c2x, float y, float radius) {
        c1.set(c1x, y);
        c2.set(c2x, y);
        cp.y = y;
        r = radius;
    }

    public void update(float t, Path path) {
        if (t <= 0) {
            path.addCircle(c1.x, c1.y, r, Direction.CCW);
            return;
        } else if (t >= 1) {
            path.addCircle(c2.x, c2.y, r, Direction.CCW);
            return;
        }

        float c1x = t > 0.5f ? c1.x + (c2.x - c1.x - r) * (t - 0.5f) / 0.5f : c1.x;
        float c2x = t < 0.5f ? c1.x + r + (c2.x - c1.x - r) * t / 0.5f : c2.x;

        float r1 = r * (1 - t);
        float r2 = r * t;
        cp.x = c1x + (c2x - c1x) * r1 / (r1 + r2);

        s1.x = s2.x = c1x + r1 * r1 / (cp.x - c1x);
        float dy = (float) Math.sqrt(r1 * r1 - (s1.x - c1x) * (s1.x - c1x));
        s1.y = cp.y - dy;
        s2.y = cp.y + dy;

        s3.x = s4.x = c2x - r2 * r2 / (c2x - cp.x);
        dy = (float) Math.sqrt(r2 * r2 - (s3.x - c2x) * (s3.x - c2x));
        s3.y = cp.y - dy;
        s4.y = cp.y + dy;

        path.addCircle(c1x, c1.y, r1, Direction.CW);
        path.addCircle(c2x, c2.y, r2, Direction.CW);
        path.moveTo(s1.x, s1.y);
        path.quadTo(cp.x, cp.y, s3.x, s3.y);
        path.lineTo(s4.x, s4.y);
        path.quadTo(cp.x, cp.y, s2.x, s2.y);
    }
}
