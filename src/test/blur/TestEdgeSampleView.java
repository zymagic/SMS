package test.blur;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TestEdgeSampleView extends LinearLayout implements OnItemClickListener {

    private ListView mListView;
    private ImageView mImageView;
    private MyAdapter mAdapter;

    private Paint messagePaint;
    private static class LoadingDots {
        LoadingDots next;
        String dot;
    }
    private LoadingDots dots;

    public TestEdgeSampleView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        float density = context.getResources().getDisplayMetrics().density;
        mImageView = new ImageView(context);
        mImageView.setScaleType(ScaleType.CENTER_INSIDE);
        addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, (int) (60 * density)));
        mListView = new ListView(context);
        addView(mListView, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        mAdapter = new MyAdapter(context);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        LoadingDots dot = new LoadingDots();
        dots = dot;
        dot.dot = "loading.";
        dot.next = new LoadingDots();
        dot = dot.next;
        dot.dot = "loading..";
        dot.next = new LoadingDots();
        dot = dot.next;
        dot.dot = "loading...";
        dot.next = new LoadingDots();
        dot = dot.next;
        dot.dot = "loading...";
        dot.next = new LoadingDots();
        dot = dot.next;
        dot.dot = "loading....";
        dot.next = dots;
        messagePaint = new Paint();
        messagePaint.setTextSize(18 * density);
        messagePaint.setColor(0xffffffff);
        messagePaint.setShadowLayer(0.5f, 0, 1f, 0x80000000);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawColor(0x80000000);
        super.dispatchDraw(canvas);
        if (mAdapter == null || mAdapter.loading) {
            canvas.drawText(dots.dot, getWidth() * 0.45f, getHeight() / 2f, messagePaint);
            dots = dots.next;
            invalidate();
        }
    }

    private class MyAdapter extends BaseAdapter {

        private ArrayList<Item> items;
        private Context context;
        private boolean loading = true;

        MyAdapter(final Context context) {
            this.context = context;
            new Thread() {
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    PackageManager pm = context.getPackageManager();
                    List<ResolveInfo> rl = pm.queryIntentActivities(intent, 0);
                    final ArrayList<Item> res;
                    if (rl != null) {
                        ArrayList<Item> items = new ArrayList<TestEdgeSampleView.Item>(rl.size());
                        for (ResolveInfo info : rl) {
                            Item it = new Item();
                            it.label = info.loadLabel(pm);
                            it.icon = info.loadIcon(pm);
                            items.add(it);
                        }
                        res = items;
                    } else {
                        res = null;
                    }
                    post(new Runnable() {
                       public void run() {
                           items = res;
                           notifyDataSetChanged();
                       }
                    });
                    loading = false;
                }
            }.start();
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public Item getItem(int position) {
            return items == null ? null : items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = null;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = new TextView(context);
                float density = context.getResources().getDisplayMetrics().density;
                tv.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, (int) (60 * density)));
                int padding = (int) (6 * density);
                tv.setPadding(padding, padding, padding, padding);
                tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }
            Item item = items.get(position);
            tv.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
            tv.setText(item.label);
            return tv;
        }

    }

    private static class Item {
        Drawable icon;
        CharSequence label;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = mAdapter.getItem(position);
        int size = (int) (getResources().getDisplayMetrics().density * 48);
        mImageView.setImageBitmap(dealWidth(item.icon, size, size));
    }

    private static Bitmap dealWidth(Drawable d, int width, int height) {
        Bitmap bmp = dtb(d, width, height);
        int[] colors = new int[width * height];
        int[] out = new int[width * height];
        bmp.getPixels(colors, 0, width, 0, 0, width, height);
        int baseColor = Color.argb(0xff, (int) (0xff * 0.25), (int) (0xff * 0.84), 0xff);
        int sumr = 0, sumg = 0, sumb = 0;
        int count = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (Color.alpha(colors[j * width + i]) == 0xff) {
                    sumr += Color.red(colors[j * width + i]);
                    sumg += Color.green(colors[j * width + i]);
                    sumb += Color.blue(colors[j * width + i]);
                    count++;
                }
                out[j * width + i] = filter(colors, width, height, i, j, 1);
            }
        }
        baseColor = count == 0 ? 0xff000000 : Color.argb(0xff, sumr / count, sumg / count, sumb / count);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                colors[j * width + i] = sample13(out, width, height, i, j, 1);
            }
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                out[j * width + i] = denoise(colors, width, height, i, j, 1);
            }
        }
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//                out[j * width + i] = filter(colors, width, height, i, j, 1) * baseColor;
//            }
//        }
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//                colors[j * width + i] = smooth(out, width, height, i, j, 1);
//            }
//        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                colors[j * width + i] = (smooth(out, width, height, i, j, 1) << 24) | (baseColor & 0x00ffffff);
            }
        }
//        colors = out;
        bmp.recycle();
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int filter(int[] a, int w, int h, int i, int j, int r) {
        if (a[j * w + i] == 0) {
            return 0;
        }
        float color = gray(a[j * w + i]);
        float leftColor = gray(i == 0 ? 0 : a[j * w + i - 1]);
        float rightColor = gray(i == w - 1 ? 0 : a[j * w + i + 1]);
        float topColor = gray(j == 0 ? 0 : a[(j - 1) * w + i]);
        float bottomColor = gray(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        float leftTopColor = gray(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        float leftBottomColor = gray(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        float rightTopColor = gray(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        float rightBottomColor = gray(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);
        float averGray = color * gau1 + (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + leftBottomColor + rightTopColor + rightBottomColor) * gau3;

        return Color.argb(0xff, (int) (averGray * 0.3f), (int) (averGray * 0.6f), (int) (averGray * 0.1f));
    }



    private static int sample(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        float firstDelta = Math.abs(gray(leftColor) - gray(color));
        float secondDelta = Math.abs(gray(rightColor) - gray(color));
        if (Math.abs(firstDelta - secondDelta) > thres) {
            if (firstDelta > secondDelta) {
                if (Color.alpha(color) != 0 && (gray(color) < gray(leftColor) || Color.alpha(leftColor) == 0)) {
                    return 1;
                }
            } else {
                if (Color.alpha(color) != 0 && (gray(color) < gray(rightColor) || Color.alpha(rightColor) == 0)) {
                    return 1;
                }
            }
        }
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        firstDelta = Math.abs(gray(topColor) - gray(color));
        secondDelta = Math.abs(gray(bottomColor) - gray(color));
        if (Math.abs(firstDelta - secondDelta) > thres) {
            if (firstDelta > secondDelta) {
                if (Color.alpha(color) != 0 && (gray(color) < gray(topColor) || Color.alpha(topColor) == 0)) {
                    return 1;
                }
            } else {
                if (Color.alpha(color) != 0 && (gray(color) < gray(bottomColor) || Color.alpha(bottomColor) == 0)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    private static int sample2(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        float firstDelta = Math.abs(gray(leftColor) - gray(color));
        float secondDelta = Math.abs(gray(rightColor) - gray(color));
        if (Math.abs(firstDelta - secondDelta) > thres) {
            return 1;
        }
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        firstDelta = Math.abs(gray(topColor) - gray(color));
        secondDelta = Math.abs(gray(bottomColor) - gray(color));
        if (Math.abs(firstDelta - secondDelta) > thres) {
            return 1;
        }
        return 0;
    }

    private static int sample3(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        if (Math.abs(gray(leftColor) - 2*gray(color) + gray(rightColor)) > thres) {
            return 1;
        }
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        if (Math.abs(gray(topColor) - 2 * gray(color) + gray(bottomColor)) > thres) {
            return 1;
        }
        return 0;
    }

    private static int sample4(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int count = 4;
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        if (Color.red(leftColor) + Color.red(rightColor) + Color.red(topColor) + Color.red(bottomColor) - count * Color.red(color) > thres
                || Color.green(leftColor) + Color.green(rightColor) + Color.green(topColor) + Color.green(bottomColor) - count * Color.green(color) > thres
                || Color.blue(leftColor) + Color.blue(rightColor) + Color.blue(topColor) + Color.blue(bottomColor) - count * Color.blue(color) > thres)
            return 1;
        return 0;
    }

    private static int sample5(int[] a, int w, int h, int i, int j, int r) {
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        float thres = 0xff * 0.03f;
        if (gray(leftColor) + gray(rightColor) + gray(topColor) + gray(bottomColor) - 4 * gray(color) > thres)
            return 1;
        return 0;
    }

    private static int sample6(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        float first = gray(leftColor) - 2 * gray(color) + gray(rightColor);
        float second = gray(topColor) - 2 * gray(color) + gray(bottomColor);
        if (first * first + second * second > thres * thres)
            return 1;
        return 0;
    }

    private static int sample7(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        float first = Math.abs(gray(leftColor) - gray(color)) + Math.abs(gray(color) - gray(rightColor));
        float second = Math.abs(gray(topColor) - gray(color)) + Math.abs(gray(color) - gray(bottomColor));
        if (first * first + second * second > thres * thres)
            return 1;
        return 0;
    }

    private static int sample8(int[] a, int w, int h, int i, int j, int r) {
        float thres = 0xff * 0.03f;
        float color = gray(a[j * w + i]);
        float leftColor = gray(i == 0 ? 0 : a[j * w + i - 1]);
        float rightColor = gray(i == w - 1 ? 0 : a[j * w + i + 1]);
        float topColor = gray(j == 0 ? 0 : a[(j - 1) * w + i]);
        float bottomColor = gray(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        float leftTopColor = gray(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        float leftBottomColor = gray(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        float rightTopColor = gray(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        float rightBottomColor = gray(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        float first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        float second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;

        if (first * first + second * second > color * color / 4)
            return 1;
        return 0;
    }

    static float gau1 = 0.147761f, gau2 = 0.118318f, gau3 = 0.0947416f;

    private static int sample9(int[] a, int w, int h, int i, int j, int r) {
        float color = Color.red(a[j * w + i]);
        float leftColor = Color.red(i == 0 ? 0 : a[j * w + i - 1]);
        float rightColor = Color.red(i == w - 1 ? 0 : a[j * w + i + 1]);
        float topColor = Color.red(j == 0 ? 0 : a[(j - 1) * w + i]);
        float bottomColor = Color.red(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        float leftTopColor = Color.red(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        float leftBottomColor = Color.red(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        float rightTopColor = Color.red(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        float rightBottomColor = Color.red(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        float first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        float second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        float thres = (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3 + color * gau1;
        if (first * first + second * second > thres * thres)
            return 1;

        color = Color.green(a[j * w + i]);
        leftColor = Color.green(i == 0 ? 0 : a[j * w + i - 1]);
        rightColor = Color.green(i == w - 1 ? 0 : a[j * w + i + 1]);
        topColor = Color.green(j == 0 ? 0 : a[(j - 1) * w + i]);
        bottomColor = Color.green(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        leftTopColor = Color.green(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        leftBottomColor = Color.green(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        rightTopColor = Color.green(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        rightBottomColor = Color.green(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        thres = (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3 + color * gau1;
        if (first * first + second * second > thres * thres)
            return 1;

        color = Color.blue(a[j * w + i]);
        leftColor = Color.blue(i == 0 ? 0 : a[j * w + i - 1]);
        rightColor = Color.blue(i == w - 1 ? 0 : a[j * w + i + 1]);
        topColor = Color.blue(j == 0 ? 0 : a[(j - 1) * w + i]);
        bottomColor = Color.blue(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        leftTopColor = Color.blue(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        leftBottomColor = Color.blue(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        rightTopColor = Color.blue(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        rightBottomColor = Color.blue(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        thres = (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3 + color * gau1;
        if (first * first + second * second > thres * thres)
            return 1;

        return 0;
    }

    private static int sample10(int[] a, int w, int h, int i, int j, int r) {
        float color = gray(a[j * w + i]);
        float leftColor = gray(i == 0 ? 0 : a[j * w + i - 1]);
        float rightColor = gray(i == w - 1 ? 0 : a[j * w + i + 1]);
        float topColor = gray(j == 0 ? 0 : a[(j - 1) * w + i]);
        float bottomColor = gray(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        float leftTopColor = gray(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        float leftBottomColor = gray(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        float rightTopColor = gray(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        float rightBottomColor = gray(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        float first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        float second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        float thres = color * gau1 + (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3;

        if (first * first + second * second > thres * thres / 2f)
            return 1;
        return 0;
    }

    private static int sample11(int[] a, int w, int h, int i, int j, int r) {
        float color = gray(a[j * w + i]);
        float leftColor = gray(toWebColor(i == 0 ? 0 : a[j * w + i - 1]));
        float rightColor = gray(toWebColor(i == w - 1 ? 0 : a[j * w + i + 1]));
        float topColor = gray(toWebColor(j == 0 ? 0 : a[(j - 1) * w + i]));
        float bottomColor = gray(toWebColor(j == h - 1 ? 0 : a[(j + 1) * w + i]));
        float leftTopColor = gray(toWebColor(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]));
        float leftBottomColor = gray(toWebColor(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]));
        float rightTopColor = gray(toWebColor(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]));
        float rightBottomColor = gray(toWebColor(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]));

        float first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        float second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        float thres = color * gau1 + (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3;

        if (first * first + second * second > thres * thres / 2f)
            return 1;
        return 0;
    }

    private static int sample12(int[] a, int w, int h, int i, int j, int r) {
        float color = gray(a[j * w + i]);
        float leftColor = gray(i == 0 ? 0 : a[j * w + i - 1]);
        float rightColor = gray(i == w - 1 ? 0 : a[j * w + i + 1]);
        float topColor = gray(j == 0 ? 0 : a[(j - 1) * w + i]);
        float bottomColor = gray(j == h - 1 ? 0 : a[(j + 1) * w + i]);
        float leftTopColor = gray(i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1]);
        float leftBottomColor = gray(i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1]);
        float rightTopColor = gray(i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1]);
        float rightBottomColor = gray(i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1]);

        float first = -leftTopColor + rightTopColor - leftBottomColor + rightBottomColor - 2 * leftColor + 2 * rightColor;
        float second = -leftTopColor - 2 * topColor - rightTopColor + leftBottomColor + 2 * bottomColor + rightBottomColor;
        float thres = color * gau1 + (leftColor + rightColor + topColor + bottomColor) * gau2 + (leftTopColor + rightTopColor + leftBottomColor + rightBottomColor) * gau3;

        if (Math.max(Math.abs(first), Math.abs(second)) > thres / 2f)
            return 1;
        return 0;
    }

    private static int sample13(int[] a, int w, int h, int i, int j, int r) {
        if (i == 0 || i == w - 1 || j == 0 || j == h - 1) {
            return sample12(a, w, h, i, j, r);
        }
        int color = a[j * w + i];
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        float thres = 0xff * 0.01f;
        if (gray(leftColor) + gray(rightColor) + gray(topColor) + gray(bottomColor) - 4 * gray(color) > thres)
            return 1;
        return 0;
    }

    private static int denoise(int[] a, int w, int h, int i, int j, int r) {
        if (true) {
            return a[j * w + i];
        }
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        int leftTopColor = i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1];
        int leftBottomColor = i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1];
        int rightTopColor = i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1];
        int rightBottomColor = i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1];
        if (leftColor + rightColor + topColor + bottomColor + leftTopColor + rightTopColor + leftBottomColor + rightBottomColor < 2) {
            return 0;
        }
        return a[j * w + i];
    }

    private static int smooth(int[] a, int w, int h, int i, int j, int r) {
        if (true) {
            return a[j * w + i] * 0xff;
        }
        if (a[j * w + i] != 0) {
            return 0xff;
        }
        int leftColor = i == 0 ? 0 : a[j * w + i - 1];
        int rightColor = i == w - 1 ? 0 : a[j * w + i + 1];
        int topColor = j == 0 ? 0 : a[(j - 1) * w + i];
        int bottomColor = j == h - 1 ? 0 : a[(j + 1) * w + i];
        int leftTopColor = i == 0 || j == 0 ? 0 : a[(j - 1) * w + i - 1];
        int leftBottomColor = i == 0 || j == h - 1 ? 0 : a[(j + 1) * w + i - 1];
        int rightTopColor = i == w - 1 || j == 0 ? 0 : a[(j - 1) * w + i + 1];
        int rightBottomColor = i == w - 1 || j == h - 1 ? 0 : a[(j + 1) * w + i + 1];
        return (int) (0xff * (a[j * w + i] * 0.147761f + (leftColor + rightColor + topColor + bottomColor) * 0.118318f + (leftTopColor + leftBottomColor + rightTopColor + rightBottomColor) * 0.0947416f));
    }

    private static float gray(int color) {
        return Color.red(color) * 0.3f + Color.green(color) * 0.6f + Color.blue(color) * 0.1f;
    }

    private static int applyAlpha(int color, int alphaColor) {
        return (color & 0x00ffffff) | (alphaColor & 0xff000000);
    }

    private static int toWebColor(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, r / 0x33 * 0x33, g / 0x33 * 0x33, b / 0x33 * 0x33);
    }

    private static Bitmap dtb(Drawable d, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Rect bounds = d.getBounds();
        float scale = Math.min(width * 1.0f / bounds.width(), height * 1.0f / bounds.height());
        float sw = bounds.width() * scale;
        float sh = bounds.height() * scale;
        canvas.translate(-bounds.left, -bounds.top);
        canvas.scale(scale, scale);
        canvas.translate((width - sw) / 2, (height - sh) / 2);
        d.draw(canvas);
        return bmp;
    }
}
