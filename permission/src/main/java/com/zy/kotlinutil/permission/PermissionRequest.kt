package com.zy.kotlinutil.permission

import android.support.v4.app.FragmentActivity

/**
 * Created by zy on 2018/3/22.
 */
class PermissionRequest(val permissions: Array<out String>) {

    var permit: (() -> Unit)? = null
    var refuse: (() -> Unit)? = null

    fun request(activity: FragmentActivity) {
        val fragment = PermissionFragment()
        fragment.request = this
        activity.supportFragmentManager.beginTransaction().add(fragment, "permission").commitAllowingStateLoss()
    }
}