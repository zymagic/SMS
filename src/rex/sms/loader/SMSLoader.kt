package rex.sms.loader

import android.provider.Telephony
import com.zy.kotlinutil.db.*
import rex.sms.App
import rex.sms.model.SMSText
import rex.sms.model.SMSThread
import kotlin.math.max
import kotlin.math.min

internal fun String.thumb(): String {
    val c = get(0).toInt()
    return if ((c and 0xff) != 0 && ((c ushr 4) and 0xff) != 0) {
        substring(0, 1)
    } else {
        substring(0, min(2, length))
    }
}

internal fun String.display() = substring(0,
        when {
            length >= 11 -> max(6, length - 8)
            length <= 8 -> min(6, length)
            else -> max(3, length - 8)
        }
)

fun SMSThread.loadText(): List<SMSText> {
    return App.app.db(Telephony.Sms.CONTENT_URI)
            .select(Telephony.Sms.DATE, Telephony.Sms.BODY, Telephony.Sms.TYPE)
            .filter(Telephony.Sms.THREAD_ID eq id)
            .orderBy("${Telephony.Sms.DATE} asc")
            .map {
                SMSText(id, getString(1), getLong(0), getInt(2) == 1)
            }
}

fun String.trimAddress() = replace("[- ]", "")

fun String.canonicalAddress() = with(trimAddress()) {
    when (length) {
        11 -> this
        14 -> if (this.startsWith('+')) substring(3) else this
        15 -> if (this.startsWith('0')) substring(4) else this
        16 -> if (this.startsWith('1')) substring(5) else this
        else -> this
    }
}