package com.vtiahotenkov.propertygenerator

import getMapper

const val MIXPANEL = "mixpanel"
const val FIREBASE = "firebase"
const val SINGULAR = "singular"

interface Event

class Analytics(private val trackers: List<Tracker>) {

    fun track(e: Event) {
        trackers.forEach { it.track(e) }
    }
}

class Tracker(
    private val tag: String,
    private val namesMapping: Map<Class<*>, String>
) {

    fun track(e: Event) {
        namesMapping[e::class.java]?.let { eventName ->
            val propertyMapper = getMapper(e)
            println("Tracker [$tag] >>> $eventName to ${propertyMapper?.toMap(e)}")
        }
    }
}