package com.zy.kotlinutil.db

import java.util.*

/**
 * Created by zy on 18-3-22.
 */
class Table(val name: String) {
    lateinit var columns: Array<Column>
}

class TableBuilder {
    var name: String? = null
    var columns: ArrayList<Column> = ArrayList()

    fun column(f: ColumnBuilder.() -> Unit) {
        columns.add(ColumnBuilder().apply(f).build())
    }

    fun column(name: String, type: ColumnType, modifier: ColumnModifier? = null) {
        columns.add(Column(name, type).apply { this.modifier = modifier })
    }

    fun build() : Table {
        val name = name
        val columns = columns
        if (name == null || columns.size == 0) {
            throw IllegalArgumentException("Illegal table")
        }
        val table = Table(name)
        table.columns = columns.toArray(arrayOfNulls<Column>(0))
        return table
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