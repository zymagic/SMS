package com.zy.utils.download.core

interface Storage {
    fun save(id: Int, value: String)
    fun delete(id: Int): String?
    fun all(): List<String>
    fun clear()
}