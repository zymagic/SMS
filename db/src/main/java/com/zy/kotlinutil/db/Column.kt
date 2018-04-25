package com.zy.kotlinutil.db

/**
 * Created by zy on 18-3-22.
 */
class Column(val name: String,
             val type: ColumnType,
             var modifier: ColumnModifier = ColumnModifier.NONE) {

    override fun toString(): String {
        return "$name $type$modifier"
    }
}

enum class ColumnType(val value: String) {
    TEXT("TEXT"),
    BLOB("BLOB"),
    INTEGER("INTEGER");
}

enum class ColumnModifier(val value: String) {
    PRIMARY_KEY(" PRIMARY KEY"),
    NOT_NULL(" NOT NULL"),
    NONE("");

    override fun toString(): String {
        return value
    }
}

class ColumnBuilder {
    var name: String? = null
    var type: ColumnType = ColumnType.TEXT
    var modifier: ColumnModifier = ColumnModifier.NONE

    fun build() : Column {
        val n = name
        val t = type
        if (n == null) {
            throw IllegalArgumentException("Invalid Column")
        }
        return Column(n, t, modifier)
    }
}