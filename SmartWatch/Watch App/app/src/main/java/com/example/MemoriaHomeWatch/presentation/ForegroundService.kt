package com.example.MemoriaHomeWatch.presentation

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat


// still working on it
class ForegroundService : Service(){

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("TrackActivity","bound or wtvr")
        return null
    }
    fun startForeground() {
        try {
            Log.d("TrackActivity","starting foreground")
            val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                .build()
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ 100, // Cannot be 0
                /* notification = */ notification,
                /* foregroundServiceType = */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                } else {
                    0
                },
            )
        } catch (e: Exception) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                Log.e("TrackActivity", "Foreground service start not allowed", e)
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
            }
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return START_STICKY
    }

}