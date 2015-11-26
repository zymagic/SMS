package test.blur.water;

import android.graphics.Path;
import android.graphics.PointF;

public class CircleShapeTransform implements TransformShape {

    private PointF center = new PointF();
    private float radius;

    public void updateParams(float ... params) {
        center.set(params[0], params[1]);
        radius = params[2];
    }

    public void updatePath(float t, Path path, float nextX, float[] out) {

    }

}
