package rex.sms

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.zy.kotlinutils.core.scheduleCall
import rex.sms.model.SMSThread

/**
 * Created by zy on 18-3-26.
 */
class ThreadTitlePresenter(activity: Activity) {

    val titleView: TextView = activity.findViewById(R.id.session_title)
    val titleMore: View = activity.findViewById(R.id.title_more)

    fun show(thread: SMSThread) {
        if (thread.state == SMSThread.STATE_LOADED) {
            titleView.text = thread.displays.joinToString(",")
            titleView.tag = null
        } else if (thread.state == SMSThread.STATE_LOADING) {
            if (titleView.tag == null || titleView.tag == thread) {
                titleView.tag = thread
                thread.scheduleCall(100) {
                    show(this)
                }
            }
        }
    }

}