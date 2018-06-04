package rex.sms.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Created by zy on 2018/3/23.
 */
class CircleBackground : Drawable() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius = 0f
    private val point = PointF()

    init {
        paint.color = 0xff999999.toInt()
        paint.style = Paint.Style.FILL
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        radius = min(bounds.width().toFloat(), bounds.height().toFloat()) / 2f
        point.set(bounds.exactCenterX(), bounds.exactCenterY())
    }

    override fun draw(canvas: Canvas) {
        if (radius == 0f) {
            return
        }
        canvas.drawCircle(point.x, point.y, radius, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

}