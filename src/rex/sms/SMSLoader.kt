package rex.sms

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import rex.sms.model.SMSContact
import rex.sms.model.SMSThread
import rex.sms.utils.async
import rex.sms.utils.saveStringPref
import rex.sms.utils.stringPref
import rex.sms.utils.uiThread
import kotlin.math.max
import kotlin.math.min

/**
 * Created by zy on 2018/3/23.
 */
fun Context.loadThreads(f: (SMSThread) -> Unit) {
    async {
        val cursor = contentResolver.query(
                Telephony.Sms.Conversations.CONTENT_URI,
                arrayOf(Telephony.Sms.Conversations.THREAD_ID),
                null, null,
                Telephony.Sms.Conversations.DEFAULT_SORT_ORDER
        )
        val threads = ArrayList<Int>()

        while (cursor.moveToNext()) {
            threads.add(cursor.getInt(0))
        }

        threads.forEach {
            val c = contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.PERSON),
                    "${Telephony.Sms.THREAD_ID} = $it", null,
                    Telephony.Sms.DEFAULT_SORT_ORDER
            )
            val contactsMap = HashMap<String, Int>()
            while (c.moveToNext()) {
                contactsMap.put(c.getString(0), c.getInt(1))
            }

            val contacts = contactsMap.map { SMSContact(it.key, it.value) }

            uiThread {
                f(SMSThread(it).apply { this.contacts.addAll(contacts) })
            }
        }
    }
}

fun SMSContact.display() : Pair<String, String> {
    if (resolved || person == 0) {
        return displayDefault(!resolved && person == 0)
    }
    val stored = "$ADDRESS_PREF_PREFIX$address".stringPref(null)
    if (stored == null) {
        val cursor = App.app.contentResolver.query(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Uri.encode(person.toString())),
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                null, null, null
        )
        if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            "$ADDRESS_PREF_PREFIX$address".saveStringPref(name)
            return displayNormal(name)
        } else {
            return displayDefault(true)
        }
    } else {
        return displayNormal(stored)
    }
}

private fun SMSContact.displayDefault(resolve: Boolean = false): Pair<String, String> {
    val d = displayName
    val f = fullName
    if (d != null && f != null) {
        if (resolve) {
            resolved = true
        }
        return d to f
    }

    val display = address.substring(0,
            when {
                address.length >= 11 -> max(6, address.length - 8)
                address.length <= 8 -> min(6, address.length)
                else -> max(3, address.length - 8)
            }
    )
    val full = address
    displayName = display
    fullName = address

    if (resolve) {
        resolved = true
    }

    return display to full
}

private fun SMSContact.displayNormal(name: String): Pair<String, String> {
    val display = name.thumb()
    val full = name
    displayName = display
    fullName = full
    resolved = true
    return display to full
}

fun SMSThread.display(f: (Array<String>, Array<String>) -> Unit) {
    val display = ArrayList<String>(contacts.size)
    val full = ArrayList<String>(contacts.size)

    var needResolve = false

    contacts.forEach {
        val (d, f) = it.displayDefault()
        display.add(d)
        full.add(f)

        if (!it.resolved) {
            needResolve = true
        }
    }

    val da = display.toTypedArray()
    val fa = full.toTypedArray()

    f(da, fa)

    if (needResolve) {
        async {
            contacts.forEachIndexed { i, s ->
                val (d, f) = s.display()
                da[i] = d
                fa[i] = f
            }

            uiThread {
                f(da, fa)
            }
        }
    }
}

private const val THREAD_PREF_PREFIX = "THREAD_"
private const val ADDRESS_PREF_PREFIX = "addr_"

private fun String.thumb(): String {
    val c = get(0).toInt()
    return if ((c and 0xff) != 0 && ((c ushr 4) and 0xff) != 0) {
        substring(0, 1)
    } else {
        substring(0, min(2, length))
    }
}