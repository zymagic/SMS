package rex.sms

import android.app.Application

/**
 * Created by zy on 2018/3/23.
 */
class App : Application() {

    companion object {
        lateinit var app: App
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}