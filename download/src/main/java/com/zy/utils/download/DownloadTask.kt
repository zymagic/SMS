package com.zy.utils.download

import com.zy.utils.download.core.Checksum
import com.zy.utils.download.core.parseChecksum

internal const val TASK_SPLITTER = "#@--"
internal const val REQUEST_SPLITTER = "#-@-"
internal const val INFO_SPLITTER = "#--@"

const val STATE_CANCEL = -1
const val STATE_RUNNING = 1
const val STATE_WAITING = 0
const val STATE_COMPLETE = 2

class DownloadTask(val id: Int, val request: DownloadRequest) {
    var state: Int = 0
    var info = DownloadInfo(request.url)
}

data class DownloadRequest(val url: String, val targetFile: String) {
    override fun toString(): String {
        return "$url$REQUEST_SPLITTER$targetFile"
    }
}

data class DownloadInfo(val url: String, val checksum: Checksum? = null, val size: Long = 0) {
    override fun toString(): String {
        return "$url$INFO_SPLITTER$checksum$INFO_SPLITTER$size"
    }
}

internal fun parseRequest(str: String) : DownloadRequest {
    val items = str.split(REQUEST_SPLITTER)
    return DownloadRequest(items[0], items[1])
}

internal fun parseInfo(str: String) : DownloadInfo {
    val items = str.split(INFO_SPLITTER)
    return DownloadInfo(items[0], parseChecksum(items[1]), items[2].toLong())
}

internal fun DownloadTask.flush(): String {
    return "$id$TASK_SPLITTER$state$TASK_SPLITTER$info$TASK_SPLITTER$request"
}

internal fun parseTask(str: String) : DownloadTask? {
    val items = str.split(TASK_SPLITTER)

    val id = items[0].toInt()
    val state = items[1].toInt()
    val info = parseInfo(items[2])
    val request = parseRequest(items[3])

    val task = DownloadTask(id, request)
    task.state = state
    task.info = info

    return task
}