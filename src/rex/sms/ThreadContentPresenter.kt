package rex.sms

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.zy.kotlinutils.core.*
import rex.sms.loader.loadText
import rex.sms.model.SMSText
import rex.sms.model.SMSThread
import rex.sms.widget.RecyclerAdapter
import rex.sms.widget.RecyclerPresenter

/**
 * Created by zy on 18-3-26.
 */
class ThreadContentPresenter(activity: Activity) {
    private val recyclerView: RecyclerView = activity.findViewById(R.id.sms_list)
    private val adapter = MyAdapter(activity)

    companion object {
        private var inboxColor = 0
        private var selfColor = 0
    }

    init {
        recyclerView.layoutManager = LinearLayoutManager(activity, OrientationHelper.VERTICAL, false)
        recyclerView.adapter = adapter
        inboxColor = Color.argb(0xff, 0x33, 0x33, 0x33)
        selfColor = Color.argb(0xff, 0x99, 0x99, 0x99)
    }

    fun show(thread: SMSThread) {
        async {
            val contents = thread.loadText()
            uiThread {
                adapter.setItems(contents)
                recyclerView.scrollToPosition(contents.size - 1)
            }
        }
    }

    class MyAdapter(context: Context) : RecyclerAdapter<SMSText>(context) {

        override fun getLayoutResource(): Int {
            return R.layout.thread_item_layout
        }

        override fun onCreatePresenter(): RecyclerPresenter<SMSText> {
            return MyPresenter()
        }
    }

    class MyPresenter : RecyclerPresenter<SMSText>() {

        lateinit var timeView: TextView
        lateinit var contentView: TextView
        lateinit var layout: LinearLayout

        override fun onCreate(view: View) {
            timeView = view.findViewById(R.id.time)
            contentView = view.findViewById(R.id.message)
            layout = timeView.parent as LinearLayout
        }

        override fun onBind(model: SMSText) {
            timeView.text = model.date.toDate("M月dd日")
            contentView.text = model.content
            if (!model.inbox) {
                contentView.setTextColor(selfColor)
                layout.leftPadding = 48.dp()
            } else {
                contentView.setTextColor(inboxColor)
                layout.leftPadding = 0
            }
        }
    }
}