package rex.sms.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView
import com.zy.kotlinutils.core.maxTo

class ScaleText(context: Context, attributeSet: AttributeSet) : TextView(context, attributeSet) {

    var eWidth: Float = 14f
    private val fontMetrics = Paint.FontMetrics()
    private val myPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        eWidth = paint.measureText("0")
        paint.getFontMetrics(fontMetrics)
        myPaint.set(paint)
        myPaint.color = textColors.defaultColor
        myPaint.textSize = paint.textSize
        myPaint.style = paint.style
        myPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        if (eWidth == 0f || text == null || text.isEmpty()) {
            super.onDraw(canvas)
            return
        }
        val space = width - paddingLeft - paddingRight
        val desired = (text.length).maxTo(6) * eWidth
        if (desired < space) {
            super.onDraw(canvas)
            return
        }
        val scale = space / desired
        canvas.save()
        canvas.scale(scale, scale, width / 2f, height / 2f)
        canvas.drawText(text, 0, minOf(6, text.length), width / 2f, height / 2 - (fontMetrics.bottom + fontMetrics.top) / 2, myPaint)
        canvas.restore()
    }
}