package rex.sms

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.zy.kotlinutil.permission.permission
import rex.sms.model.SMSContact
import rex.sms.model.SMSThread

/**
 * Created by zy on 2018/3/22.
 */
class MainActivity : FragmentActivity() {

    private lateinit var slidePresenter: SlidePresenter
    private lateinit var contactPresenter: ContactsListPresenter

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

        setupViews()
    }

    private fun setupViews() {
        slidePresenter  = SlidePresenter(this)
        contactPresenter = ContactsListPresenter(this)
        contactPresenter.onScrollChanged { slidePresenter.expand() }
    }

    fun testCursor() {
        loadThreads { contactPresenter.addThread(it) }
    }
}