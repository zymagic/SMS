package com.zy.kotlinutil.db

data class Q(val statement: String) {
    constructor(q: Q) : this(q.statement)
}

infix fun <K, T> K.gt(value: T) = Q("$this > $value")
infix fun <K, T> K.gte(value: T) = Q("$this >= $value")
infix fun <K, T> K.lt(value: T) = Q("$this < $value")
infix fun <K, T> K.lte(value: T) = Q("$this <= $value")
infix fun <K, T> K.eq(value: T) = Q("$this = $value")
infix fun <K, T> K.ne(value: T) = Q("$this <> $value")
infix fun <K, T> K._in(value: Iterable<T>) = "$this IN ${value.joinToString(",", "(", ")")}"

infix fun Q.and(other: Q) = Q("($statement) AND (${other.statement})")
infix fun Q.or(other: Q) = Q("$statement OR ${other.statement}")

class QuerySet {
    var where: Q? = null

    fun filter(q: Q) : QuerySet {
        where = where?.and(q) ?: q
        return this
    }

}

fun testQuery() {
    (1 gt 0) and (2 ne 3)
}