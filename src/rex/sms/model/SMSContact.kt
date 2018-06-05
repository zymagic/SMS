package rex.sms.model

import rex.sms.loader.canonicalAddress

/**
 * Created by zy on 2018/3/23.
 */
class SMSContact(val address: String, val person: Int) {
    val canonicalAddress = address.canonicalAddress()
}