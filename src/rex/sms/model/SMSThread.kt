package rex.sms.model

/**
 * Created by zy on 2018/3/23.
 */
class SMSThread(val id: Int) {
    val contacts = ArrayList<SMSContact>()
    val contents = ArrayList<SMSText>()
}