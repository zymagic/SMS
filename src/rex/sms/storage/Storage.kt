package rex.sms.storage

import rex.sms.model.SMSContact
import rex.sms.utils.saveStringPref
import rex.sms.utils.stringPref

internal const val THREAD_PREF_PREFIX = "THREAD_"
internal const val ADDRESS_PREF_PREFIX = "ADDRESS_"
internal const val PERSON_PREF_PREFIX = "PERSON_"

fun cachedContacts(threadId: Int) : List<SMSContact>? {
    val cached = "$THREAD_PREF_PREFIX$threadId".stringPref(null)
    return cached?.run {
        split(",").map {
            val (a, b) = it.split(";")
            SMSContact(a, b.toInt())
        }
    }
}

fun cacheContacts(threadId: Int, contacts: List<SMSContact>) {
    "$THREAD_PREF_PREFIX$threadId".saveStringPref(contacts.joinToString(",") { "${it.address};${it.person}" })
}

fun cachedPerson(id: Int): String? = "$PERSON_PREF_PREFIX$id".stringPref(null)

fun cachePerson(id: Int, person: String) = "$PERSON_PREF_PREFIX$id".saveStringPref(person)

fun cachedAddress(addr: String): String? = "$ADDRESS_PREF_PREFIX$addr".stringPref(null)

fun cacheAddress(addr: String, person: String) = "$ADDRESS_PREF_PREFIX$addr".saveStringPref(person)