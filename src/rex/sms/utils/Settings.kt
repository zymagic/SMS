package rex.sms.utils

import android.content.Context
import rex.sms.App

/**
 * Created by zy on 2018/3/23.
 */

private const val PREF_NAME = "settings"

private val pref = App.app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

fun String.intPref(def: Int): Int {
    return pref.getInt(this, def)
}

fun String.floatPref(def: Float): Float {
    return pref.getFloat(this, def)
}

fun String.stringPref(def: String?): String? {
    return pref.getString(this, def)
}