package com.njord.mobile

import android.app.Application
import com.njord.mobile.notifications.NjordNotificationScheduler

class NjordApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NjordNotificationScheduler.scheduleAll(this)
    }
}
