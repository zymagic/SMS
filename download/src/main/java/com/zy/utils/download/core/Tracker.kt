package com.zy.utils.download.core

import android.util.SparseArray
import com.zy.utils.download.Config
import com.zy.utils.download.DownloadTask
import com.zy.utils.download.flush
import com.zy.utils.download.parseTask

interface Tracker {
    fun add(task: DownloadTask)
    fun get(id: Int) : DownloadTask?
    fun remove(id: Int): DownloadTask?
    fun clear()
}

class DefaultTracker : Tracker {

    val memoryTracker = MemoryTracker()
    val persistTracker = PersistTracker()

    init {
        Config.storage?.all()?.forEach {
            val task = parseTask(it)
            if (task != null) {
                memoryTracker.add(task)
            }
        }
    }

    override fun add(task: DownloadTask) {
        memoryTracker.add(task)
        persistTracker.add(task)
    }

    override fun get(id: Int): DownloadTask? {
        return memoryTracker.get(id)
    }

    override fun remove(id: Int): DownloadTask? {
        val task = memoryTracker.remove(id)
        persistTracker.remove(id)
        return task
    }

    override fun clear() {
        memoryTracker.clear()
        persistTracker.clear()
    }
}

class MemoryTracker : Tracker {

    private val map = SparseArray<DownloadTask>()

    override fun add(task: DownloadTask) {
        map.put(task.id, task)
    }

    override fun get(id: Int): DownloadTask? {
        return map.get(id)
    }

    override fun remove(id: Int): DownloadTask? {
        val task = map.get(id)
        map.remove(id)
        return task
    }

    override fun clear() {
        map.clear()
    }
}

class PersistTracker : Tracker {

    override fun add(task: DownloadTask) {
        Config.storage?.save(task.id, task.flush())
    }

    override fun get(id: Int): DownloadTask? {
        return null
    }

    override fun remove(id: Int): DownloadTask? {
        Config.storage?.delete(id)
        return null
    }

    override fun clear() {
        Config.storage?.clear()
    }

}