package rex.sms

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import rex.sms.model.SMSThread
import rex.sms.widget.CircleBackground
import rex.sms.widget.RecyclerAdapter
import rex.sms.widget.RecyclerPresenter
import kotlin.math.max
import kotlin.math.min

/**
 * Created by zy on 2018/3/23.
 */
class ContactsListPresenter(activity: Activity) {
    private val recyclerView : RecyclerView = activity.findViewById(R.id.contacts_list)
    private val indicator = ContactPresenter().apply { create(activity.findViewById(R.id.current_contact)) }
    private val adapter = MyAdapter(activity)

    private var selection = 0

    private var onScrolled: (() -> Unit)? = null

    private var onSelectionChanged: ((Int, SMSThread) -> Unit)? = null

    init {
        recyclerView.layoutManager = LinearLayoutManager(activity, OrientationHelper.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                onScrolled?.invoke()
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                snap(selection)
            }
        })
    }

    fun addThread(item: SMSThread) {
        if (adapter.itemCount == 0) {
            indicator.bind(item)
        }
        adapter.addItem(item)
    }

    fun onScrollChanged(f: () -> Unit) {
        onScrolled = f
    }

    fun onSelectionChanged(f: (Int, SMSThread) -> Unit) {
        onSelectionChanged = f
    }

    private fun snap(position: Int) {
        var holder = recyclerView.findViewHolderForAdapterPosition(position)
        if (holder == null || holder.itemView.parent == null) {
            return
        }
        val tarY = min(max(0, holder.itemView.top), recyclerView.height - holder.itemView.height)
        indicator.view.translationY = tarY.toFloat()
    }


    private inner class MyAdapter(context: Context) : RecyclerAdapter<SMSThread>(context) {
        override fun onCreatePresenter(): RecyclerPresenter<SMSThread> {
            return MyPresenter()
        }

        override fun getLayoutResource(): Int {
            return R.layout.contact_list_item
        }

    }

    private inner class MyPresenter : RecyclerPresenter<SMSThread>() {

        private val contactPresenter = ContactPresenter()

        override fun onCreate(view: View) {
            contactPresenter.create(view)
        }

        override fun onBind(model: SMSThread) {
            contactPresenter.bind(model)
            view.setOnClickListener {
                if (position != selection) {
                    selection = position
                    snap(selection)
                    indicator.bind(model)
                    onSelectionChanged?.invoke(position, model)
                    if (view.top < 0) {
                        recyclerView.smoothScrollBy(0, view.top)
                    } else if (view.bottom > recyclerView.height) {
                        recyclerView.smoothScrollBy(0, view.bottom - recyclerView.height)
                    }
                }
            }
        }

    }

    private class ContactPresenter : RecyclerPresenter<SMSThread>() {
        lateinit var thumb: TextView
        lateinit var full: TextView
        lateinit var background: CircleBackground

        override fun onCreate(view: View) {
            thumb = view.findViewById(R.id.thumb)
            full = view.findViewById(R.id.full)
            background = CircleBackground()
            thumb.background = background
        }

        override fun onBind(model: SMSThread) {
            model.display { displays, fulls ->
                this@ContactPresenter.model?.let {
                    if (model != it) {
                        return@display
                    }
                    thumb.text = displays[0]
                    full.text = fulls[0]
                }
            }
        }
    }
}