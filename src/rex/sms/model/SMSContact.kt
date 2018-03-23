package rex.sms.model

/**
 * Created by zy on 2018/3/23.
 */
class SMSContact(val address: String, val person: Int) {
    var resolved = false

    var displayName: String? = null
    var fullName: String? = null
}