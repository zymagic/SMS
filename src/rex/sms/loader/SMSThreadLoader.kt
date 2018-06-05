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
import rex.sms.storage.*
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

fun SMSThread.loadReads(f: (Boolean) -> Unit) {
    async {
        App.app.db(Telephony.Sms.CONTENT_URI)
                .select(Telephony.Sms._ID)
                .filter((Telephony.Sms.THREAD_ID eq id) and (Telephony.Sms.READ eq 0))
                .count()
                .uiThreadCall {
                    f(this > 0)
                }
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
        val personSet = HashSet<String>()
        val contactList = ArrayList<SMSContact>()
        contacts.addAll(App.app.loadContacts(id))

        contacts.forEach {
            val person = if (it.person != 0) cachedPerson(it.person) else cachedAddress(it.canonicalAddress)

            if (person != null) {
                if (!personSet.contains(person)) {
                    displays.add(person)
                    thumbs.add(person.thumb())
                    personSet.add(person)
                }
            } else {
                if (!personSet.contains(it.canonicalAddress)) {
                    displays.add(it.address)
                    thumbs.add(it.address.display())
                    contactList.add(it)
                    personSet.add(it.canonicalAddress)
                }
            }
        }

        uiThread { f(displays, thumbs) }

        state = SMSThread.STATE_LOADED

        if (contactList.isEmpty()) {
            return@async
        }

        var updated = false

        contactList.forEach {
            val person = loadPerson(it.person, it.canonicalAddress)
            if (person != null) {
                val index = displays.indexOf(it.address)
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
    return cachedContacts(id)
        ?: db(Telephony.Sms.CONTENT_URI)
            .select(Telephony.Sms.ADDRESS, Telephony.Sms.PERSON)
            .filter(Telephony.Sms.THREAD_ID eq id)
            .orderBy(Telephony.Sms.DEFAULT_SORT_ORDER)
            .fill(HashMap<String, Pair<String, Int>>()) {
                val address = it.getString(0)
                val person = it.getInt(0)
                val cached = get(address.canonicalAddress())
                if (cached == null || cached.second == 0) {
                    put(address.canonicalAddress(), address to person)
                }
            }.map {
                SMSContact(it.value.first, it.value.second)
            }.also {
                cacheContacts(id, it)
            }
}

private fun loadPerson(id: Int, address: String): String? {
    return cachedAddress(address)
            ?: App.app.db(ContactsContract.PhoneLookup.CONTENT_FILTER_URI).withId(address)
                    .select(ContactsContract.Contacts.DISPLAY_NAME)
                    .firstOrNul {
                        getString(0)
                    }.also {
                        it?.apply {
                            cacheAddress(address, this)
                            if (id != 0) {
                                cachePerson(id, this)
                            }
                        }
                    }
}