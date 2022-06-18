package com.vtiahotenkov.processor

fun interface MapSerializer<T> {
    fun toMap(t: T): Map<String, Any?>
}