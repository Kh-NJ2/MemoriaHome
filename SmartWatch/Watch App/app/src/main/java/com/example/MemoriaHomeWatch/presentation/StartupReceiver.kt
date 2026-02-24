package com.example.MemoriaHomeWatch.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.services.client.data.DataType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        if (p1.action != Intent.ACTION_BOOT_COMPLETED) return



        WorkManager.getInstance(p0).enqueue(
            OneTimeWorkRequestBuilder<RegisterForPassiveDataWorker>().build()
        )
    }
}

class RegisterForPassiveDataWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        runBlocking {
            HealthServicesManager(appContext).startPassiveMonitoring(DataType.HEART_RATE_BPM,
                {type, data -> TrackingActivity.dataHandle2(type, data)},
                false)
        }
        return Result.success()
    }
}