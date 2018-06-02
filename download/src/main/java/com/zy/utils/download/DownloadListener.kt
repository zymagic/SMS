package com.zy.utils.download

import java.io.File

interface DownloadListener {
    fun onStart(id: Int) {}
    fun progress(id: Int, got: Long, total: Long) {}
    fun onError(id: Int, throwable: Throwable) {}
    fun onCancel(id: Int) {}
    fun onComplete(id: Int, file: File) {}
}

val EMPTY_LISTENER = object : DownloadListener {}