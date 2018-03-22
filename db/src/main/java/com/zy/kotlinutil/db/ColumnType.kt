package com.zy.kotlinutil.db

/**
 * Created by zy on 18-3-22.
 */
enum class ColumnType(val value: String) {
    TEXT("TEXT"),
    BLOB("BLOB"),
    INTEGER("INTEGER");
}

enum class ColumnModifier(val value: String) {
    PRIMARY_KEY("PRIMARY KEY"),
    NOT_NULL("NOT NULL")
}

class Column(val name: String, val type: ColumnType) {
    var modifier: ColumnModifier? = null
}

class ColumnBuilder {
    var name: String? = null
    var type: ColumnType? = null
    var modifier: ColumnModifier? = null

    fun build() : Column {
        val n = name
        val t = type
        if (n == null || t == null) {
            throw IllegalArgumentException("Invalid Column")
        }
        val c = Column(n, t)
        c.modifier = modifier
        return c
    }
}