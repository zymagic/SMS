package rex.sms

import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.util.Log

/**
 * Created by zy on 2018/3/22.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (checkUriPermission(Telephony.Sms.Conversations.CONTENT_URI, android.os.Process.myPid(), android.os.Process.myUid(), 0) != PackageManager.PERMISSION_GRANTED) {
            grantUriPermission(packageName, Telephony.Sms.Conversations.CONTENT_URI, 0)
            Log.e("XXXX", "no permission ")
            return
        }

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