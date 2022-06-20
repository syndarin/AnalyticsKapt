package com.vtiahotenkov.processor

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackingConfig(
    val target: String,
    val overriddenName: String = ""
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AnalyticsEvent(
    val eventName: String,
    val configs: Array<TrackingConfig>
)