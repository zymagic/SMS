package com.zy.kotlinutil.permission

import android.support.v4.app.FragmentActivity

/**
 * Created by zy on 2018/3/22.
 */
fun FragmentActivity.permission(vararg permissions: String): PermissionObject {
    return PermissionObject(PermissionRequest(permissions), this)
}

fun FragmentActivity.permission(vararg permissions: String, f: () -> Unit) {
    PermissionRequest(permissions).apply { permit = f }.request(this)
}

class PermissionObject(private val request: PermissionRequest, private val activity: FragmentActivity) {

    private var onSuccess: (() -> Unit)? = null
    private var onFail: (() -> Unit)? = null

    private var succeed: Boolean = false
    private var finished: Boolean = false
    private var called: Boolean = false

    fun successCallback() {
        finished = true
        succeed = true
        onSuccess?.invoke()
    }
    fun failCallback() {
        finished = true
        succeed = false
        onFail?.invoke()
    }

    fun onPermitted(f: () -> Unit): PermissionObject {
        if (!called) {
            called = true
            request.permit = f
            request.refuse = this::failCallback
            request.request(activity)
        } else {
            if (finished) {
                if (succeed) {
                    f.invoke()
                }
            } else {
                onSuccess = f
            }
        }
        return this
    }

    fun onRefused(f: () -> Unit): PermissionObject {
        if (!called) {
            called = true
            request.permit = this::successCallback
            request.refuse = f
            request.request(activity)
        } else {
            if (finished) {
                if (!succeed) {
                     f.invoke()
                }
            } else {
                onFail = f
            }
        }
        return this
    }

}

fun testP() {
}