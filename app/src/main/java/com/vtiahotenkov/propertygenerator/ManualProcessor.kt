package com.vtiahotenkov.propertygenerator

import com.vtiahotenkov.processor.MapSerializer


/*
fun <T> getMapper(key: T): MapSerializer<T> {
    require(key != null) { "Null values aren't allowed" }
    val clazz = key!!::class.java
    return (mapping[clazz] as? MapSerializer<T>) ?: error("Mapper for ${clazz} not found")
}

val mapping = hashMapOf<Class<*>, MapSerializer<*>>(
    Other::class.java to MapSerializer<Other> { other -> hashMapOf("some" to other.some) },
    Stub::class.java to MapSerializer<Stub> { stub -> hashMapOf("some" to stub.some) },
    UserDto::class.java to MapSerializer<UserDto> { stub -> hashMapOf("username" to stub.username, "age" to stub.age) },
)*/
