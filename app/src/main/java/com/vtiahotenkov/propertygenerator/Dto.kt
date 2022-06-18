package com.vtiahotenkov.propertygenerator

import com.vtiahotenkov.processor.PayloadEvent

interface ConsumableDto

@PayloadEvent
data class Other(val some: String): ConsumableDto

@PayloadEvent
data class UserDto(
    val username: String,
    val age: Int
): ConsumableDto

@PayloadEvent
data class Stub(val some: String): ConsumableDto

@PayloadEvent
data class Animal(val kind: String, val color: String)