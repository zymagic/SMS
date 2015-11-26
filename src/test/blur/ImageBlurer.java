
package test.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class ImageBlurer {
    private RenderScript mRS;

    private ScriptIntrinsicBlur mBlur;

    private Allocation mTmpIn;

    private Allocation mTmpOut;

    public ImageBlurer(Context context) {
        this.mRS = RenderScript.create(context);
        this.mBlur = ScriptIntrinsicBlur.create(mRS, Element.U8_4(this.mRS));
    }

    public void blurBitmap(Bitmap bmp, float radius) {
        if (radius == 0f) {
            return;
        }

        if (this.mTmpIn != null)
            this.mTmpIn.destroy();
        if (this.mTmpOut != null)
            this.mTmpOut.destroy();

        this.mTmpIn = Allocation.createFromBitmap(this.mRS, bmp);
        this.mTmpOut = Allocation.createFromBitmap(this.mRS, bmp);
        this.mBlur.setRadius(radius);
        this.mBlur.setInput(mTmpIn);
        this.mBlur.forEach(mTmpOut);
        this.mTmpOut.copyTo(bmp);
    }

    public void destroy() {
        this.mBlur.destroy();
        if (this.mTmpIn != null)
            this.mTmpIn.destroy();
        if (this.mTmpOut != null)
            this.mTmpOut.destroy();
        this.mRS.destroy();
    }
}
