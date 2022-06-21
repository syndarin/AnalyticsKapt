package com.vtiahotenkov.propertygenerator

import com.vtiahotenkov.processor.annotations.AnalyticsEvent
import com.vtiahotenkov.processor.annotations.TrackingConfig

@AnalyticsEvent(
    eventName = "first_event",
    configs = [
        TrackingConfig(target = FIREBASE),
        TrackingConfig(target = MIXPANEL, overriddenName = "first_event_overridden_name")
    ]
)
data class FirstEvent(
    val payload: String
) : Event

@AnalyticsEvent(
    eventName = "second_event",
    configs = [
        TrackingConfig(target = FIREBASE),
        TrackingConfig(target = MIXPANEL, overriddenName = "second_event_overidden_name")
    ]
)
object SecondEvent : Event


@AnalyticsEvent(
    eventName = "third_event",
    configs = [TrackingConfig(target = SINGULAR)]
)
data class ThirdEvent(
    val thirdPayloadString: String,
    val thirdPayloadInt: Int
) : Event