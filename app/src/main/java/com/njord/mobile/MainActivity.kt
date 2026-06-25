package com.njord.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.njord.mobile.model.Destination
import com.njord.mobile.notifications.NjordLocalNotifier
import com.njord.mobile.ui.NjordApp

class MainActivity : ComponentActivity() {
    private val pendingDestination = mutableStateOf<Destination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDestination.value = destinationFromIntent(intent)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            NjordApp(notificationDestination = pendingDestination.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDestination.value = destinationFromIntent(intent)
    }

    private fun destinationFromIntent(intent: Intent?): Destination? {
        val name = intent?.getStringExtra(NjordLocalNotifier.ExtraDestination) ?: return null
        return Destination.entries.firstOrNull { it.name == name }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }
}
