package test.blur;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.FloatMath;
import android.util.Log;

public class BlurUtils {

    // 二维高斯函数
    static float gaussian(int x, int y, float sigma) {
        return (float) (FloatMath.exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * sigma * sigma * Math.PI));
    }

    static int color(int src, int delta, float c) {
        int a = (int) (Color.alpha(src) + Color.alpha(delta) * c);
        int r = (int) (Color.red(src) + Color.red(delta) * c);
        int g = (int) (Color.green(src) + Color.green(delta) * c);
        int b = (int) (Color.blue(src) + Color.blue(delta) * c);
        return Color.argb(a, r, g, b);
    }

    static final int R = 3;
    static final int D = 2 * R + 1;
    static final int N = D * D;

    static float[][] MATRIX = {
        {0.09474165239981544f, 0.11831800083454005f, 0.09474165239981544f},
        {0.11831800083454005f, 0.147761301044102f, 0.11831800083454005f},
        {0.09474165239981544f, 0.11831800083454005f, 0.09474165239981544f}
    };

    static double[] G = new double[N];

    static double[] ALPHA = new double[N];

    static float SIGMA;

    static {
        float s = 0;
        for (int i = 0; i < N; i++) {
            s += (i % D - R) * (i % D - R) + (i / D - R) * (i / D - R);
        }
        s /= N;
        SIGMA = s;

        double[] m = G;

        for (int i = 0; i < N; i++) {
            m[i] = gaussian(i % D - R, i / D - R, SIGMA);
        }

        float total = 0;
        for (int i = 0; i < N; i++) {
            total += m[i];
        }

        for (int i = 0; i < N; i++) {
            m[i] /= total;
        }

        Step step = new Step(R);
        int[] out = new int[2];
        int last = 0;

        double[] a = ALPHA;
        step.next(out);
        last = (out[1] + R) * D + out[0] + R;
        a[N - 1] = 1.0 / N;
        for (int i = N - 2; i >= 0; i--) {
            step.next(out);
            int cur = (out[1] + R) * D + out[0] + R;
            a[i] = a[i + 1] / (1 - a[i + 1]); // * m[cur] / m[last];
            last = cur;
        }
        Log.e("XXXXX", "alphas is " + Arrays.toString(a));
    }

    static void blurBitmap(Bitmap bmp, int r) {
        int d = 2 * r + 1;
        int n = d * d;
        float s = 0;
        for (int i = 0; i < n; i++) {
            s += (i % d - r) * (i % d - r) + (i / d - r) * (i / d - r);
        }
        s /= n;
        Log.e("XXXXXX", "s=" + s);
        float[] g = new float[n];
        for (int i = 0; i < n; i++) {
            g[i] = gaussian(i % d - r, i / d - r, 15f);
        }
        float total = 0;
        for (int i = 0; i < n; i++) {
            total += g[i];
        }
        for (int i = 0; i < n; i++) {
            g[i] /= total;
        }

        int[] c = new int[n];
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] cc = new int[w * h];
        bmp.getPixels(cc, 0, w, 0, 0, w, h);
        for (int i = 110; i < 610; i++) {
            for (int j = 390; j < 890; j++) {
                bmp.getPixels(c, 0, d, i - r, j - r, d, d);
                float as = 0, rs = 0, gs = 0, bs = 0;
                int index = 0;
                for (int ccc : c) {
                    float gi = g[index++];
                    as += ((ccc >>> 24) & 0xff) * gi;
                    rs += ((ccc >>> 16) & 0xff) * gi;
                    gs += ((ccc >>> 8) & 0xff) * gi;
                    bs += (ccc & 0xff) * gi;
                }
                cc[j * w + i] = Color.argb((int) as, (int) rs, (int) gs, (int) bs);
            }
        }
        bmp.setPixels(cc, 0, w, 0, 0, w, h);
    }
}
