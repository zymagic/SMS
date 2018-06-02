package com.zy.utils.download.core

import com.zy.utils.download.Config
import java.io.File

class Checksum(val checksum: Any, val algorithm: File.() -> Any) {

    fun check(file: File) : Boolean {
        return algorithm(file) == checksum
    }
}

fun parseChecksum(checksum: String): Checksum? {
    return Checksum(checksum) {
        Config.checksum?.invoke(this) ?: md5()
    }
}

fun File.md5(): Any {
    return ""
}