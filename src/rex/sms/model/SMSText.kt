package rex.sms.model

/**
 * Created by zy on 2018/3/23.
 */
data class SMSText(val threadId: Int, val content: String, val date: Long, val inbox: Boolean)