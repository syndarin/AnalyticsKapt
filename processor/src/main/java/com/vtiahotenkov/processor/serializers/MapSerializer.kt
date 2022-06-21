package com.vtiahotenkov.processor.serializers

fun interface MapSerializer<T> {
    fun toMap(t: T): Map<String, Any?>
}