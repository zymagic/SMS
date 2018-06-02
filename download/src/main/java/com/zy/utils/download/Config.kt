package com.zy.utils.download

import com.zy.utils.download.core.Storage
import java.io.File

object Config {
    var storage: Storage? = null
    var checksum: (File.() -> Any)? = null
    var loader: ((url: String, file: String, state: () -> Boolean, progress: (Long) -> Unit) -> Unit)? = null
    var exceptionHandler: ((Throwable) -> Throwable?)? = null
}