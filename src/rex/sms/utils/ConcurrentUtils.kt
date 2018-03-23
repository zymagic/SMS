package rex.sms.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*

/**
 * Created by zy on 2018/3/23.
 */
val executor: Executor = Executors.newCachedThreadPool {
    Thread(it, "executor")
}

val uiHandler = Handler(Looper.getMainLooper())

fun async(r: () -> Unit) {
    executor.execute(r)
}

fun uiThread(r: () -> Unit) {
    uiHandler.post(r)
}