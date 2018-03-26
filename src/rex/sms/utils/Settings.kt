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

fun String.saveIntPref(value: Int) {
    pref.edit().putInt(this, value).apply()
}

fun String.floatPref(def: Float): Float {
    return pref.getFloat(this, def)
}

fun String.saveFloatPref(value: Float) {
    pref.edit().putFloat(this, value).apply()
}

fun String.stringPref(def: String?): String? {
    return pref.getString(this, def)
}

fun String.saveStringPref(value: String) {
    pref.edit().putString(this, value).apply()
}

fun String.booleanPref(def: Boolean): Boolean {
    return pref.getBoolean(this, def)
}

fun String.saveBooleanPref(value: Boolean) {
    pref.edit().putBoolean(this, value).apply()
}

fun String.longPref(def: Long): Long {
    return pref.getLong(this, def)
}

fun String.saveLongPref(value: Long) {
    pref.edit().putLong(this, value).apply()
}


