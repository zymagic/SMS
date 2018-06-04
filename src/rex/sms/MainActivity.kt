package rex.sms

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.zy.kotlinutil.permission.permission
import rex.sms.loader.loadThreads

/**
 * Created by zy on 2018/3/22.
 */
class MainActivity : FragmentActivity() {

    private lateinit var slidePresenter: SlidePresenter
    private lateinit var contactPresenter: ContactsListPresenter
    private lateinit var threadContentPresenter: ThreadContentPresenter
    private lateinit var titlePresenter: ThreadTitlePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()

        permission(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS)
                .onPermitted {
                    Log.e("XXXX", "onPermitted")
                    testCursor()
                }
                .onRefused {
                    Log.e("XXXXX", "refused")
                }
    }

    private fun setupViews() {
        slidePresenter  = SlidePresenter(this)
        contactPresenter = ContactsListPresenter(this)
        threadContentPresenter = ThreadContentPresenter(this)
        titlePresenter = ThreadTitlePresenter(this)

        contactPresenter.onScrollChanged { slidePresenter.expand() }
        contactPresenter.onSelectionChanged { _, smsThread ->
            threadContentPresenter.show(smsThread)
            titlePresenter.show(smsThread)
            slidePresenter.fold()
        }
    }

    fun testCursor() {
        loadThreads { contactPresenter.addThreads(it) }
    }
}