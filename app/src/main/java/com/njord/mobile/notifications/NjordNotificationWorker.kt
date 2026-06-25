package com.njord.mobile.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.njord.mobile.BuildConfig
import com.njord.mobile.api.ActivityResult
import com.njord.mobile.api.HeartbeatResult
import com.njord.mobile.api.IncidentAcknowledgements
import com.njord.mobile.api.LiveResult
import com.njord.mobile.api.NjordApiClient
import com.njord.mobile.model.Destination

class NjordNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        try {
            when (inputData.getString(NjordNotificationScheduler.InputKind)) {
                NjordNotificationScheduler.KindIncident -> checkIncidents()
                NjordNotificationScheduler.KindActivity -> checkActivity()
                NjordNotificationScheduler.KindHeartbeat -> checkHeartbeat()
                else -> Result.failure()
            }
        } catch (_: Exception) {
            Result.retry()
        }

    private fun checkIncidents(): Result =
        when (val result = NjordApiClient.fetchLive(BuildConfig.NJORD_API_BASE_URL, BuildConfig.NJORD_API_KEY)) {
            is LiveResult.Success -> {
                val acknowledgedIds = IncidentAcknowledgements.readActiveIds(applicationContext.filesDir)
                val incidents = result.response.incidents.filterNot {
                    "${it.timestamp}-${it.category}" in acknowledgedIds
                }
                NjordNotificationDecisions.incidentNotification(incidents)?.let { content ->
                    NjordLocalNotifier.notify(applicationContext, 3001, content, Destination.Live)
                }
                Result.success()
            }
            is LiveResult.Error -> Result.retry()
            LiveResult.Loading -> Result.retry()
        }

    private fun checkActivity(): Result =
        when (val result = NjordApiClient.fetchActivity(BuildConfig.NJORD_API_BASE_URL, BuildConfig.NJORD_API_KEY)) {
            is ActivityResult.Success -> {
                val previous = notificationPrefs().getString(ActivitySignatureKey, null)
                NjordNotificationDecisions.activityNotification(result.response, previous)?.let { (content, signature) ->
                    NjordLocalNotifier.notify(applicationContext, 3002, content, Destination.Activity)
                    notificationPrefs().edit().putString(ActivitySignatureKey, signature).apply()
                }
                NjordNotificationScheduler.scheduleNextDailyActivity(applicationContext)
                Result.success()
            }
            is ActivityResult.Error -> Result.retry()
            ActivityResult.Loading -> Result.retry()
        }

    private fun checkHeartbeat(): Result =
        when (val result = NjordApiClient.fetchHeartbeat(BuildConfig.NJORD_API_BASE_URL, BuildConfig.NJORD_API_KEY)) {
            is HeartbeatResult.Success -> {
                NjordNotificationDecisions.heartbeatNotification(result.services)?.let { content ->
                    NjordLocalNotifier.notify(applicationContext, 3003, content, Destination.Heartbeat)
                }
                Result.success()
            }
            is HeartbeatResult.Error -> Result.retry()
            HeartbeatResult.Loading -> Result.retry()
        }

    private fun notificationPrefs() =
        applicationContext.getSharedPreferences("njord-notification-state", Context.MODE_PRIVATE)

    private companion object {
        const val ActivitySignatureKey = "activity-signature"
    }
}
