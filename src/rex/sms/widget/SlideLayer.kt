package rex.sms.widget

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import kotlin.math.absoluteValue

/**
 * Created by zy on 2018/3/23.
 */
class SlideLayer : RelativeLayout {

    companion object {
        const val TOUCH_STATE_REST = 0
        const val TOUCH_STATE_SCROLLING = 1
        const val TOUCH_STATE_SETTLING = 2
    }

    private var flingTres = 300
    private var maxFlingVelocity = 1000
    private var touchState = 0
    private var touchSlop: Int = 30
    private var velocityTracker: VelocityTracker? = null
    private var point: PointF = PointF()
    var callback: SlideCallback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyle: Int) : super(context, attr, defStyle) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2
        flingTres = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        callback?.let {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (it.isSliding()) {
                        touchState = TOUCH_STATE_SCROLLING
                        it.stop()
                    } else {
                        touchState = TOUCH_STATE_REST
                    }
                    point.set(ev.x, ev.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - point.x
                    val dy = ev.y - point.y
                    if (dx.absoluteValue > touchSlop && dx.absoluteValue > dy.absoluteValue) {
                        touchState = TOUCH_STATE_SCROLLING
                        point.set(ev.x, ev.y)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    touchState = TOUCH_STATE_REST
                }

            }
        }
        return touchState != TOUCH_STATE_REST
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        callback?.let {
            trackMotionEvent(event)
            val x = event.x
            val y = event.y
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchState = TOUCH_STATE_SCROLLING
                    if (it.isSliding()) {
                        it.stop()
                    }
                    point.set(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (touchState == TOUCH_STATE_SCROLLING) {
                        onSlide(x - point.x)
                    }
                    point.set(x, y)
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    velocityTracker?.let {
                        it.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                        val vx = it.xVelocity
                        if (vx.absoluteValue > flingTres) {
                            onFling(vx)
                        } else {
                            onSettle()
                        }
                    } ?: onSettle()
                    releaseVelocityTracker()
                }
                MotionEvent.ACTION_CANCEL -> {
                    touchState = TOUCH_STATE_REST
                    releaseVelocityTracker()
                }
            }
        }
        return true
    }

    private fun trackMotionEvent(event: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun onSlide(dx: Float) {
        callback?.offsetBy(dx)
    }

    private fun onSettle() {
        touchState = if (callback == null) {
            TOUCH_STATE_REST
        } else {
            callback?.settle(0f)
            TOUCH_STATE_SETTLING
        }
    }

    private fun onFling(vx: Float) {
        touchState = if (callback == null) {
            TOUCH_STATE_REST
        } else {
            callback?.settle(vx)
            TOUCH_STATE_SETTLING
        }
    }

    interface SlideCallback {
        fun isSliding(): Boolean
        fun stop()
        fun settle(v: Float)
        fun offsetBy(x: Float)
    }
}