package rex.sms

import android.app.Activity
import android.view.View
import rex.sms.widget.SlideLayer

/**
 * Created by zy on 2018/3/23.
 */
class SlidePresenter(activity: Activity) {

    private val slidePanel = activity.findViewById<View>(R.id.sms_panel)
    private val maxSlideRetain = activity.resources.getDimensionPixelSize(R.dimen.header_size) * 2

    init {
        val slideLayer: SlideLayer = activity.findViewById(R.id.root)
        slideLayer.callback = object: SlideLayer.SlideCallback {

            override fun isSliding(): Boolean {
                return slidePanel.translationX > 0 && slidePanel.translationX < slidePanel.endPos
            }

            override fun stop() {
                slidePanel.animate().cancel()
            }

            override fun settle(v: Float) {
                if (v == 0f) {
                    if (slidePanel.translationX >= slidePanel.endPos / 2) {
                        slidePanel.slideToEnd()
                    } else {
                        slidePanel.slideToStart()
                    }
                } else if (v > 0f) {
                    slidePanel.slideToEnd()
                } else if (v < 0f) {
                    slidePanel.slideToStart()
                }
            }

            override fun offsetBy(x: Float) {
                slidePanel.translationX =
                        when {
                            slidePanel.translationX + x < 0 -> 0f
                            slidePanel.translationX + x > slidePanel.endPos -> slidePanel.endPos
                            else -> slidePanel.translationX + x
                        }
            }

        }
    }

    fun fold() {
        slidePanel.slideToStart()
    }

    fun expand() {
        slidePanel.slideToEnd()
    }

    private fun View.slideToStart() = animate().translationX(0f)
    private fun View.slideToEnd() = animate().translationX((width - maxSlideRetain).toFloat())
    private val View.endPos: Float
        get() = (width - maxSlideRetain).toFloat()
}