package rex.sms

import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.zy.kotlinutil.permission.permission

/**
 * Created by zy on 2018/3/22.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        permission(android.Manifest.permission.READ_SMS)
                .onPermitted {
                    Log.e("XXXX", "onPermitted")
                    testCursor()
                }
                .onRefused {
                    Log.e("XXXXX", "refused")
                }
    }

    fun testCursor() {
        val c = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI, null, null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER)
        c!!.moveToFirst()
        val indexCount = c.columnCount
        while (c.moveToNext()) {
            val sb = StringBuilder()
            sb.append('[')
            for (i in 0 until indexCount) {
                sb.append(c.getColumnName(i))
                sb.append("=")
                val type = c.getType(i)
                when (type) {
                    Cursor.FIELD_TYPE_FLOAT -> sb.append(c.getFloat(i))
                    Cursor.FIELD_TYPE_INTEGER -> sb.append(c.getInt(i))
                    Cursor.FIELD_TYPE_STRING -> sb.append(c.getString(i))
                    else -> sb.append("other_types")
                }
                sb.append(",")
            }
            sb.append("]")
            Log.e("XXXXX", sb.toString())
        }
        Log.e("XXXXX", "read sms count $indexCount")
    }
}