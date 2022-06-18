package com.vtiahotenkov.propertygenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vtiahotenkov.processor.AnalyticsEvent
import com.vtiahotenkov.processor.TrackingConfig
import getMapper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        consumeDto(UserDto("Alice", 25))
        consumeDto(Stub("Stub DTO"))
        consumeDto(Other("Other DTO"))

    }

    private fun consumeDto(dto: ConsumableDto) {
        println("Consuming: ${getMapper(dto).toMap(dto)}")
    }
}

@AnalyticsEvent(
    eventName = "first_event",
    configs = [
        TrackingConfig(target = "firebase"),
        TrackingConfig(target = "mixpanel", overriddenName = "first_event_overridden_name")
    ]
)
data class FirstEvent(
    val payload: String
)

@AnalyticsEvent(
    eventName = "second_event",
    configs = [
        TrackingConfig(target = "firebase"),
        TrackingConfig(target = "mixpanel", overriddenName = "second_event_overidden_name")
    ]
)
object SecondEvent
