package rex.sms.loader

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.zy.kotlinutil.db.*
import com.zy.kotlinutils.core.async
import com.zy.kotlinutils.core.uiThread
import com.zy.kotlinutils.core.uiThreadCall
import rex.sms.App
import rex.sms.model.SMSContact
import rex.sms.model.SMSThread
import rex.sms.utils.saveStringPref
import rex.sms.utils.stringPref

/**
 * Created by zy on 2018/3/23.
 */

fun Context.loadThreads(f: (List<SMSThread>) -> Unit) {
    async {
        db(Telephony.Sms.Conversations.CONTENT_URI)
                .select(Telephony.Sms.Conversations.THREAD_ID)
                .orderBy(Telephony.Sms.Conversations.DEFAULT_SORT_ORDER)
                .map {
                    SMSThread(getInt(0))
                }
                .uiThreadCall(f)
    }
}

fun SMSThread.loadContacts(f: (displays: List<String>, thumbs: List<String>) -> Unit) {
    if (state != SMSThread.STATE_INIT) {
        if (state == SMSThread.STATE_LOADED) {
            f(displays, thumbs)
        }
        return
    }
    state = SMSThread.STATE_LOADING

    async {
        val personSet = HashSet<Int>()
        val contactMap = HashMap<Int, String>()
        contacts.addAll(App.app.loadContacts(id))

        contacts.forEach {
            if (it.person != 0) {
                if (!personSet.contains(it.person)) {
                    val person: String? = "$PERSON_PREF_PREFIX${it.person}".stringPref(null)
                    if (person != null) {
                        displays.add(person)
                        thumbs.add(person.thumb())
                        personSet.add(it.person)
                    } else {
                        displays.add(it.address)
                        thumbs.add(it.address.display())
                        contactMap[it.person] = it.address
                    }
                }
            } else {
                displays.add(it.address)
                thumbs.add(it.address.display())
            }
        }

        uiThread { f(displays, thumbs) }

        state = SMSThread.STATE_LOADED

        if (contactMap.isEmpty()) {
            return@async
        }

        var updated = false

        contactMap.forEach { (key, value) ->
            val person = loadPerson(key, value)
            if (person != null) {
                val index = displays.indexOf(value)
                displays[index] = person
                thumbs[index] = person.thumb()
                updated = true
            }
        }

        if (updated) {
            uiThread { f(displays, thumbs) }
        }
    }
}

private fun Context.loadContacts(id: Int): Iterable<SMSContact> {
    val cached = "$THREAD_PREF_PREFIX$id".stringPref(null)
    if (cached != null) {
        return cached.split(",").map {
            val (a, b) = cached.split(";")
            SMSContact(a, b.toInt())
        }
    }
    return db(Telephony.Sms.CONTENT_URI)
            .select(Telephony.Sms.ADDRESS, Telephony.Sms.PERSON)
            .filter(Telephony.Sms.THREAD_ID eq id)
            .orderBy(Telephony.Sms.DEFAULT_SORT_ORDER)
            .fill(HashMap<String, Int>()) {
                put(it.getString(0), it.getInt(1))
            }.map {
                SMSContact(it.key, it.value)
            }.also {
                "$THREAD_PREF_PREFIX$id".saveStringPref(it.joinToString(",") { "${it.address};${it.person}" })
            }
}

private fun loadPerson(id: Int, address: String): String? {
    return "$ADDRESS_PREF_PREFIX$address".stringPref(null)
            ?: App.app.db(ContactsContract.PhoneLookup.CONTENT_FILTER_URI).withId(address)
                    .select(ContactsContract.Contacts.DISPLAY_NAME)
                    .firstOrNul {
                        getString(0)
                    }
}

private fun loadAddress(id: Int, address: String): String? {
    return App.app.db(Uri.parse("content://mms-sms/canonical-addresses")).withId(address)
            .select(Telephony.CanonicalAddressesColumns.ADDRESS)
            .firstOrNul {
                getString(0)
            }
}