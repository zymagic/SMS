package com.zy.kotlinutil.db

import android.provider.BaseColumns
import java.util.*

/**
 * Created by zy on 18-3-22.
 */
class Table(val name: String, private val columns: Array<Column>) {

    fun create() : String {
        columns.joinToString()
        return """
            CREATE TABLE $name IF NOT EXISTS (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                ${columns.joinToString()}
            )
        """
    }

    fun drop() : String {
        return "DROP TABLE $name IF EXISTS"
    }
}

class TableBuilder {
    var name: String? = null
    private var columns: ArrayList<Column> = ArrayList()

    fun column(f: ColumnBuilder.() -> Unit) {
        columns.add(ColumnBuilder().apply(f).build())
    }

    fun column(name: String, type: ColumnType, modifier: ColumnModifier = ColumnModifier.NONE) {
        columns.add(Column(name, type, modifier))
    }

    fun build() : Table {
        val name = name
        val columns = columns
        if (name == null || columns.size == 0) {
            throw IllegalArgumentException("Illegal table")
        }
        return Table(name, columns.toTypedArray())
    }
}

fun table(f: TableBuilder.() -> Unit): Table {
    return TableBuilder().apply(f).build()
}

fun ttestTable() {
    table {
        name = "akjlklkds"
    }
}