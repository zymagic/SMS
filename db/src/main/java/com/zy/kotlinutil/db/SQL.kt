package com.zy.kotlinutil.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

fun Context.db(uri: Uri) = Sql(this, uri)

class Sql(internal val context: Context, internal val uri: Uri)

fun Sql.withId(id: String) = Sql(context, Uri.withAppendedPath(uri, id))

class SqlQuery(private val sql: Sql, private val projection: Array<out String>?) {
    internal var where: Q? = null
    internal var order: String? = null
    fun query(): Cursor {
        return sql.context.contentResolver
                .query(sql.uri, projection, where?.statement, null, order)
    }
}

fun Sql.select(vararg columns: String) = SqlQuery(this, columns)

fun SqlQuery.filter(query: Q): SqlQuery {
    this.where = query
    return this
}

fun SqlQuery.orderBy(key: String): SqlQuery {
    this.order = key
    return this
}

inline fun <reified T> SqlQuery.map(f: Cursor.() -> T) : List<T> {
    val cursor = query()
    val list = ArrayList<T>(10)
    while (cursor.moveToNext()) {
        list.add(cursor.f())
    }
    return list
}

inline fun <T> SqlQuery.fill(r: T, f: T.(Cursor) -> Unit) : T {
    val cursor = query()
    while (cursor.moveToNext()) {
        r.f(cursor)
    }
    return r
}

fun SqlQuery.count(): Int {
    return query().count
}

class SqlUpdate(val sql: Sql, val values: ContentValues) {
    internal var where: Q? = null
}

fun Sql.update(values: ContentValues) = SqlUpdate(this, values)

fun SqlUpdate.filter(query: Q): SqlUpdate {
    where = query
    return this
}

fun SqlUpdate.map() : Int {
    return sql.context.contentResolver.update(sql.uri, values, where?.statement, null)
}

fun Sql.delete(where: Q? = null): Int {
    return context.contentResolver.delete(uri, where?.statement, null)
}

fun Sql.insert(values: ContentValues): Uri {
    return context.contentResolver.insert(uri, values)
}