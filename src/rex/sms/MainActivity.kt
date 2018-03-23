package rex.sms

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zy.kotlinutil.permission.permission
import rex.sms.widget.CircleBackground
import rex.sms.widget.SlideLayer
import kotlin.math.max
import kotlin.math.min

/**
 * Created by zy on 2018/3/22.
 */
class MainActivity : FragmentActivity() {

    lateinit var smsPanel: View
    lateinit var contactsList: RecyclerView
    lateinit var contactIndicor: ContactsViewHolder
    var selection = 0
    var maxSlide: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        permission(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS)
                .onPermitted {
                    Log.e("XXXX", "onPermitted")
                    testCursor()
                }
                .onRefused {
                    Log.e("XXXXX", "refused")
                }

        smsPanel = findViewById(R.id.sms_panel)
        contactsList = findViewById(R.id.contacts_list)
        contactIndicor = ContactsViewHolder(findViewById(R.id.current_contact))
        maxSlide = resources.getDimensionPixelSize(R.dimen.header_size) * 2

        val slideLayer: SlideLayer = findViewById(R.id.root)
        val panel = smsPanel

        slideLayer.callback = object : SlideLayer.SlideCallback {
            override fun isSliding(): Boolean {
                return panel.translationX > 1 && panel.translationX < panel.width - maxSlide - 1
            }

            override fun stop() {
                panel.animate().cancel()
            }

            override fun settle(v: Float) {
                if (v == 0f) {
                    if (panel.translationX >= (panel.width - maxSlide) / 2) {
                        panel.animate().translationX((panel.width - maxSlide).toFloat())
                    } else {
                        panel.animate().translationX(0f)
                    }
                } else if (v > 0f) {
                    panel.animate().translationX((panel.width - maxSlide).toFloat())
                } else if (v < 0f) {
                    panel.animate().translationX(0f)
                }
            }

            override fun offsetBy(x: Float) {
                if (panel.translationX + x < 0) {
                    panel.translationX = 0f
                } else if (panel.translationX + x > panel.width - maxSlide) {
                    panel.translationX = (panel.width - maxSlide).toFloat()
                } else {
                    panel.translationX += x
                }
            }

        }
    }

    fun testCursor() {
        val c = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI, arrayOf(
                Telephony.Sms.Conversations.THREAD_ID
        ), null, null, Telephony.Sms.Conversations.DEFAULT_SORT_ORDER)
        val threads = ArrayList<Int>()
        while (c.moveToNext()) {
            threads.add(c.getInt(0))
        }

        val contactsView = contactsList
        contactsView.layoutManager = LinearLayoutManager(this, OrientationHelper.VERTICAL, false)
        val adapter = ContactsAdapter()
        contactsView.adapter = adapter

        val handler = Handler {
            adapter.add(it.obj as ContactsModel)
            true
        }

        Thread {
            threads.forEach {
                val cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, arrayOf(Telephony.Sms.PERSON, Telephony.Sms.ADDRESS),
                        "${Telephony.Sms.THREAD_ID} = $it", null, Telephony.Sms.DEFAULT_SORT_ORDER)
                val idSet = HashMap<String, Int>()
                while (cursor.moveToNext()) {
                    idSet.put(cursor.getString(1), cursor.getInt(0))
                }
                val contactSet = HashSet<Contact>()
                idSet.forEach {
                    contactSet.add(Contact(it.key, if (it.value == 0) null else it.value.toString()))
                    Log.e("XXXXX", "from ${it.key}, ${it.value}")
                }
                Message.obtain(handler, 0, ContactsModel().apply { contacts = contactSet.toTypedArray() })
                        .sendToTarget()

            }
        }.start()

        contactsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                smsPanel.animate().translationX((smsPanel.width - maxSlide).toFloat())
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                snap(selection)
            }
        })
    }

    private fun snap(position: Int) {
        var holder = contactsList.findViewHolderForAdapterPosition(position)
        if (holder == null || holder.itemView.parent == null) {
            return
        }
        val tarY = min(max(0, holder.itemView.top), contactsList.height - holder.itemView.height)
        contactIndicor.itemView.translationY = tarY.toFloat()
    }

    private fun select(model: ContactsModel) {

    }

    inner class ContactsAdapter : RecyclerView.Adapter<ContactsViewHolder>() {

        private val contacts = ArrayList<ContactsModel>()
        private val inflator = LayoutInflater.from(this@MainActivity)

        fun setList(list: List<ContactsModel>) {
            contacts.addAll(list)
            notifyDataSetChanged()
        }

        fun add(model: ContactsModel) {
            if (contacts.size == 0) {
                contactIndicor.setContact(model)
            }
            contacts.add(model)
            notifyDataSetChanged()
            Log.e("XXXX", "add model ${model.contacts}" )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
            return ContactsViewHolder(inflator.inflate(R.layout.contact_list_item, parent, false))
        }

        override fun getItemCount(): Int {
            return contacts.size
        }

        override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
            val model = contacts[position]
            holder.setContact(model)
            holder.itemView.setOnClickListener {
                if (position != selection) {
                    selection = position
                    snap(selection)
                    select(contacts[position])
                    contactIndicor.setContact(contacts[position])
                    if (holder.itemView.top < 0) {
                        contactsList.smoothScrollBy(0, holder.itemView.top)
                    } else if (holder.itemView.bottom > contactsList.height) {
                        contactsList.smoothScrollBy(0, holder.itemView.bottom - contactsList.height)
                    }
                }
            }
        }

    }

    class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumb : TextView = view.findViewById(R.id.thumb)
        val full: TextView = view.findViewById(R.id.full)

        init {
            thumb.background = CircleBackground()
        }

        fun setContact(model: ContactsModel) {
            val model = model.contacts[0]
            val display: String = model.name ?: model.number.substring(0, max(1, model.number.length - 8))
            thumb.text = display
            full.text = model.name ?: model.number
        }
    }

    class ContactsModel {
        lateinit var contacts: Array<Contact>
    }

    data class Contact(val number: String, val name: String?)
}