package com.vtiahotenkov.propertygenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vtiahotenkov.processor.annotations.AnalyticsEvent
import com.vtiahotenkov.processor.annotations.TrackingConfig
import getMapper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val analytics = initAnalytics()

        analytics.track(FirstEvent("I'm a payload of FirstEvent"))
        analytics.track(SecondEvent)
        analytics.track(ThirdEvent("Third String", 3))
    }

    private fun initAnalytics(): Analytics {
        return Analytics(listOf(
            Tracker(FIREBASE, AnalyticsEventsNames.firebase),
            Tracker(MIXPANEL, AnalyticsEventsNames.mixpanel),
            Tracker(SINGULAR, AnalyticsEventsNames.singular),
        ))
    }
}
