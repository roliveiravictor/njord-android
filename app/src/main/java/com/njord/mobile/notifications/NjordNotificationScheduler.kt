package com.njord.mobile.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object NjordNotificationScheduler {
    private const val IncidentWorkName = "njord-hourly-incident-notifications"
    private const val HeartbeatWorkName = "njord-hourly-heartbeat-notifications"
    private const val ActivityWorkName = "njord-daily-activity-notifications"

    internal const val InputKind = "kind"
    internal const val KindIncident = "incident"
    internal const val KindActivity = "activity"
    internal const val KindHeartbeat = "heartbeat"

    private val connectedConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleAll(context: Context) {
        scheduleHourly(context, IncidentWorkName, KindIncident)
        scheduleHourly(context, HeartbeatWorkName, KindHeartbeat)
        scheduleDailyActivity(context, ExistingWorkPolicy.KEEP)
    }

    fun scheduleNextDailyActivity(context: Context) {
        scheduleDailyActivity(context, ExistingWorkPolicy.REPLACE)
    }

    internal fun millisUntilNextActivityRun(now: ZonedDateTime): Long {
        val targetToday = now.with(LocalTime.of(21, 30, 0, 0))
        val target = if (now.isBefore(targetToday)) targetToday else targetToday.plusDays(1)
        return Duration.between(now, target).toMillis().coerceAtLeast(0L)
    }

    private fun scheduleHourly(context: Context, workName: String, kind: String) {
        val request = PeriodicWorkRequestBuilder<NjordNotificationWorker>(1, TimeUnit.HOURS)
            .setConstraints(connectedConstraints)
            .setInputData(Data.Builder().putString(InputKind, kind).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun scheduleDailyActivity(context: Context, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<NjordNotificationWorker>()
            .setConstraints(connectedConstraints)
            .setInitialDelay(millisUntilNextActivityRun(ZonedDateTime.now()), TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(InputKind, KindActivity).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(ActivityWorkName, policy, request)
    }
}
