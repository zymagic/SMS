package com.zy.utils.download.core

import android.util.SparseArray
import com.zy.utils.download.*
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class Worker(val downloader: DownloadManager) {

    val id = AtomicInteger(0)
    val executor = Executors.newFixedThreadPool(10, { Thread(it, "download-${id.getAndIncrement()}") })

    val tasks = SparseArray<Future<*>>()

    fun start(task: DownloadTask) {
        if (task.state != STATE_WAITING) {
            return
        }
        val future = executor.submit {
            try {
                Config.loader!!.invoke(task.request.url, task.request.targetFile, { task.state == STATE_RUNNING }) { progress ->
                    downloader.listenerMap.get(task.id)?.keys?.forEach {
                        it.progress(task.id, progress, task.info.size)
                    }
                }
                task.takeIf { it.state == STATE_RUNNING }
                        ?.apply { state = STATE_COMPLETE }
                        ?.notifyListener { onComplete(it, File(task.request.targetFile)) }
            } catch (interrupt: InterruptedException) {
                // ignore
            } catch (io: IOException) {
                task.takeIf { it.state == STATE_RUNNING }
                        ?.apply { state = STATE_COMPLETE }
                        ?.notifyListener { onError(it, io) }
            } catch (e: Throwable) {
                task.takeIf { it.state == STATE_RUNNING }
                        ?.apply { state = STATE_COMPLETE }
                        ?.notifyListener { id ->
                            Config.exceptionHandler?.invoke(e)?.let { onError(id, it) }
                        }
            }
            tasks.remove(task.id)
            downloader.tracker.remove(task.id)
            downloader.listenerMap.remove(task.id)
        }
        tasks.put(task.id, future)
        task.apply { state = STATE_RUNNING }.notifyListener { onStart(it) }
    }

    fun cancel(task: DownloadTask) {
        val future = tasks.get(task.id)
        future?.cancel(true)
        tasks.remove(task.id)
    }

    private fun DownloadTask.notifyListener(f: DownloadListener.(id: Int) -> Unit) {
        downloader.listenerMap.get(id)?.keys?.forEach {
            it.f(id)
        }
    }
}