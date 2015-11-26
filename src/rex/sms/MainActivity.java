
package rex.sms;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony.Sms;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cursor c = getContentResolver().query(Sms.Conversations.CONTENT_URI, null, null, null, Sms.Inbox.DEFAULT_SORT_ORDER);
        c.moveToFirst();
        int indexCount = c.getColumnCount();
        while (c.moveToNext()) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < indexCount; i++) {
                sb.append(c.getColumnName(i));
                sb.append("=");
                int type = c.getType(i);
                switch (type) {
                    case Cursor.FIELD_TYPE_FLOAT:
                        sb.append(c.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        sb.append(c.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        sb.append(c.getString(i));
                        break;
                    default:
                        sb.append("other_types");
                }
                sb.append(",");
            }
            sb.append("]");
            Log.e("XXXXX", sb.toString());
        }
    }

}
