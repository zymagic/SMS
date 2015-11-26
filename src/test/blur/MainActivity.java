
package test.blur;

import java.util.Random;

import test.blur.game.BallDrawable;
import test.blur.game.PopDrawable;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MainActivity extends Activity implements OnTouchListener {

    Random r = new Random();
    int index = 0;
    Bitmap src, buffer;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(0x40000004);
//        getWindow().addFlags(0x480080);

//        CurveChart chart = new CurveChart(this);
//        chart.setPadding(0, 600, 0, 600);
//        setContentView(chart);
//        ArrayList<Data> data = new ArrayList<Data>();
//        data.add(new Data(26, 18, null, null, true));
//        data.add(new Data(22, 15, null, null, false));
//        data.add(new Data(28, 18, null, null, false));
//        data.add(new Data(30, 19, null, null, false));
//        data.add(new Data(32, 16, null, null, false));
//        data.add(new Data(20, 14, null, null, false));
//        chart.setData(data);
//
//        chart.setBackground(new WaterDrawable());

//        final WaveDrawable d = new WaveDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.device));
//        View v = new View(this);
//        v.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    d.waveAt(event.getX(), event.getY());
//                }
//                return true;
//            }
//        });
//        v.setBackground(d);
//
//        setContentView(new TestEdgeSampleView(this), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        View v = new View(this);
        v.setBackground(new PopDrawable());
        setContentView(v);

        /**** important *****/
//        v.setBackground(new RibbonDrawable(getResources()));
//        v.setBackground(new LogoDrawable());
//        setContentView(R.layout.ribbon);
//        final View logo = findViewById(R.id.splash_logo);
//        final View title = findViewById(R.id.splash_title);
//        long  duration = 1200;
//        final AnimationSet set = new AnimationSet(true);
//        Animation rotate = new RotateAnimation(0, -70, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.4f);
//        rotate.setDuration(duration);
//        set.addAnimation(rotate);
//        Animation scale = new ScaleAnimation(1, 12, 1, 12, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.4f);
//        scale.setDuration(duration);
//        set.addAnimation(scale);
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                logo.setVisibility(View.INVISIBLE);
//                logo.startAnimation(set);
//            }
//        }, 0);
//
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                title.setVisibility(View.INVISIBLE);
//            }
//        }, 200);
//
//        View v = findViewById(R.id.ribbon);
//        v.setBackground(new RibbonDrawable(getResources()));
//
//        int cameras = android.hardware.Camera.getNumberOfCameras();
//        if (cameras == 0) {
//            Log.e("XXXXX", "no camera");
//        } else {
//            CameraInfo info = new CameraInfo();
//            for (int i = 0; i < cameras; i++) {
//                android.hardware.Camera.getCameraInfo(i, info);
//                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
//                    Log.e("XXXXX", "camera " + i + " is back camera");
//                } else if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//                    Log.e("XXXXX", "camera " + i + " is front camera");
//                } else {
//                    Log.e("XXXXX", "camera " + i + " is unknown camera");
//                }
//            }
//        }
//
//        final Calendar calender = Calendar.getInstance();
//        receiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.e("XXXXXX", intent.getAction() + DateFormat.format("dd:mm:ss", calender));
//            }
//
//        };
//        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
//        filter.addAction(Intent.ACTION_TIME_CHANGED);
//        registerReceiver(receiver, filter);

//        src = BitmapFactory.decodeResource(getResources(), R.drawable.device);
//        buffer = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
//        final Paint paint = new Paint();
//        final Canvas c = new Canvas();
//        c.setBitmap(buffer);
//        final Paint rp = new Paint();
//        rp.setColor(0xffff0000);
//        rp.setStyle(Style.STROKE);
//        final int ox = buffer.getWidth() / 2;
//        final int oy = buffer.getHeight() / 2;
//        final Step step = new Step(100);
//        buffer.setHasAlpha(false);
//        View v = new View(this) {
//            int[] delta = new int[2];
//
//            @Override
//            protected void onDraw(Canvas canvas) {
//                super.onDraw(canvas);
//                if (index < BlurUtils.N) {
//                    step.next(delta);
//                    int dx = delta[0];
//                    int dy = delta[1];
//                    paint.setAlpha((int) (255 * BlurUtils.ALPHA[index]));
//                    c.translate(dx, dy);
//                    c.drawBitmap(src, 0, 0, paint);
//                    c.drawRect(ox - 5, oy - 5, ox + 5, oy + 5, rp);
//                    c.translate(-dx, -dy);
//                    invalidate();
//                    index++;
//                }
//                canvas.drawBitmap(buffer, 0, 0, null);
//            }
//        };
//        v.setWillNotDraw(false);
//        setContentView(v);
//        v.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                startBlur();
//            }
//        });
    }

    private void startBlur() {
        Bitmap bmp = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
//        bmp.setHasAlpha(false);
        Canvas c = new Canvas();
        c.setBitmap(bmp);
        c.drawColor(0, Mode.CLEAR);
        int[] colors = new int[9];
        double as = 0, rs = 0, gs = 0, bs = 0;
        for (int i = 0; i < 9; i++) {
            colors[i] = (0xff << 24) | (r.nextInt(256) << 16) | (r.nextInt(256) << 8) | r.nextInt(256);
            float g = BlurUtils.MATRIX[i / 3][i % 3];
            as += 0xff * g;
            rs += Color.red(colors[i]) * g;
            gs += Color.green(colors[i]) * g;
            bs += Color.blue(colors[i]) * g;
        }

        int gc = Color.argb((int) as, (int) rs, (int) gs, (int) bs);
        Log.e("XXXXX", "gauss color is " + Integer.toHexString(gc));

        float[] alpha = new float[9];
        for (int i = 0; i < 9; i++) {
            if (i == 0) {
                alpha[i] = BlurUtils.MATRIX[0][0];
            } else {
                alpha[i] = alpha[i - 1] / (1 - alpha[i - 1]) * BlurUtils.MATRIX[i / 3][i % 3]/ BlurUtils.MATRIX[(i - 1) / 3][(i - 1) % 3];
            }
            c.drawColor((colors[i] & 0x00ffffff) | ((int) (0xff * alpha[i]) << 24));
        }
        int cc = bmp.getPixel(0, 0);
        Log.e("XXXXX", "draw color is " + Integer.toHexString(cc));
    }

    public static class Choose {

        private final int total;

        private Random random;

        private int[] candidates;

        private int choosed = 0;

        public Choose(int total) {
            this.total = total;

            candidates = new int[total];
            for (int i = 0; i < total; i++) {
                candidates[i] = i;
            }

            random = new Random();
        }

        public int choose() {
            int index = random.nextInt(total - choosed);
            int c = candidates[index];
            candidates[index] = candidates[total - choosed - 1];
            choosed++;
            return c;
        }

        public int remain() {
            return total - choosed;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(receiver);
    }

    int state = 0;
    PointF p1 = new PointF();
    PointF p2 = new PointF();

    boolean trigger = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int count = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (count < 2) {
                    return false;
                }

                p1.set(event.getX(0), event.getY(0));
                p2.set(event.getX(1), event.getY(1));
                break;
            case MotionEvent.ACTION_MOVE:
                if (count < 2) {
                    return false;
                }

                if (trigger) {
                    return false;
                }


                float p1x = event.getX(0);
                float p1y = event.getY(0);

                float p2x = event.getX(1);
                float p2y = event.getY(1);

                float d1 = (float) Math.hypot(p1x - p1.x, p1y - p1.y);
                float d2 = (float) Math.hypot(p2x - p2.x, p2y - p2.y);

                if (d1 > 60 || d2 > 60) {
                    trigger = true;

                    float r1x = p1x - p1.x;
                    float r1y = p1y - p1.y;
                    float r2x = p2x - p2.x;
                    float r2y = p2y - p2.y;


                    if (d1 > 20 && d2 > 20) {
                        float rp = (r1x * r2x + r1y * r2y) / (d1 * d2);
                        if (rp > 0.7f) { // 双指同向
                            float rx = r1x + r2x;
                            float ry = r1y + r2y;
                            if (Math.abs(ry) > Math.abs(rx)) {
                                if (ry > 0) {
                                    Log.e("XXXXX", "direction down");
                                } else {
                                    Log.e("XXXXX", "direction up");
                                }
                            } else {
                                if (rx > 0) {
                                    Log.e("XXXXX", "direction right");
                                } else {
                                    Log.e("XXXXXX", "direction left");
                                }
                            }
                        } else if (rp < -0.7f) {
                            if (Math.hypot(p1.x - p2.x, p1.y - p2.y) > Math.hypot(p1x - p2x, p1y - p2y)) {
                                Log.e("XXXXXX", "close");
                            } else {
                                Log.e("XXXXX", "far");
                            }
                        } else {
                            if (Math.hypot(p1.x - p2.x, p1.y - p2.y) > Math.hypot(p1x - p2x, p1y - p2y)) {
                                Log.e("XXXXXX", "invalid close");
                            } else {
                                Log.e("XXXXX", "invalid far");
                            }
                        }
                    } else {
                        float rx;
                        float ry;
                        float dx;
                        float dy;
                        if (d1 > 60) {
                            dx = r1x;
                            dy = r1y;
                            rx = p2.x - p1.x;
                            ry = p2.y - p1.y;
                        } else {
                            dx = r2x;
                            dy = r2y;
                            rx = p1.x - p2.x;
                            ry = p1.y - p2.y;
                        }
                        float r = (float) ((rx * dx + ry * dy) / Math.hypot(rx, ry) / Math.hypot(dx, dy));
                        if (r > 0.7f) {
                            Log.e("XXXXXX", "approach");
                        } else if (r < -0.7f) {
                            Log.e("XXXXXXX", "apart");
                        } else {
                            Log.e("XXXXXXX", "vert");
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.e("XXXXXX", "pointer up");
                break;
            case MotionEvent.ACTION_UP:
                Log.e("XXXXXX", "up");
                trigger = false;
                break;
        }
        return false;
    }
}
