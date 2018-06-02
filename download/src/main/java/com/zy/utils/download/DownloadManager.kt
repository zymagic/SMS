package com.zy.utils.download

import android.util.SparseArray
import com.zy.utils.download.core.DefaultTracker
import com.zy.utils.download.core.Worker
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

val DOWNLOADER = DownloadManager()

class DownloadManager {

    private val lastId: AtomicInteger = AtomicInteger(0)
    internal val listenerMap = SparseArray<WeakHashMap<DownloadListener, Int>>()
    private val worker: Worker = Worker(this)
    internal val tracker = DefaultTracker()

    fun start(request: DownloadRequest, vararg listeners: DownloadListener): Int {
        val id = request.url.hashCode()
        val task = DownloadTask(id, request)
        if (listeners.isNotEmpty()) {
            val map = WeakHashMap<DownloadListener, Int>()
            listenerMap.put(id, map)
            listeners.forEach { map[it] = id }
        }
        worker.start(task)
        return id
    }

    fun listen(id: Int, vararg listeners: DownloadListener) {
        val task = tracker.get(id)
        if (task != null && listeners.isNotEmpty()) {
            val map = listenerMap.get(id) ?: WeakHashMap<DownloadListener, Int>().apply { listenerMap.put(id, this)}
            listeners.forEach { map[it] = id }
        }
    }

    fun cancel(id: Int) {
        tracker.remove(id)?.apply { state = STATE_CANCEL }?.also {
            worker.cancel(it)
            listenerMap.get(id)?.keys?.forEach {
                it.onCancel(id)
            }
        }
    }

    fun isRunning(id: Int): Boolean {
        val task = tracker.get(id)
        return task != null && task.state >= 0
    }
}