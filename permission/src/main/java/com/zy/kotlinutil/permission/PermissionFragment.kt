package com.zy.kotlinutil.permission

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by zy on 2018/3/22.
 */
class PermissionFragment : Fragment() {

    companion object {
        const val REQUEST_CODE = 1001
    }

    var request: PermissionRequest? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permitted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (!permitted && permissions.any { shouldShowRequestPermissionRationale(it) }) {
            doRequest()
            return
        }

        request?.let {
            if (permitted) {
                it.permit?.invoke()
            } else {
                it.refuse?.invoke()
            }
        }

        fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doRequest()
    }

    private fun doRequest() {
        request?.let {
            requestPermissions(it.permissions, REQUEST_CODE)
        }
    }
}