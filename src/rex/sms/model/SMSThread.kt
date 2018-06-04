package rex.sms.model

/**
 * Created by zy on 2018/3/23.
 */
class SMSThread(val id: Int) {
    val contacts = ArrayList<SMSContact>()
    val thumbs = ArrayList<String>()
    val displays = ArrayList<String>()
    val contents = ArrayList<SMSText>()

    var state: Int = STATE_INIT

    companion object {
        const val STATE_INIT = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
    }
}