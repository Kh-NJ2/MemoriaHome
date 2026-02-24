package com.example.MemoriaHomeWatch.presentation

import android.content.Context
import android.util.Log
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTracker.TrackerError
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType

class HealthSDKManager (
    private val context: Context,
    private val onConnected: () -> Unit,
    private val onResolutionRequired: (HealthTrackerException) -> Unit,
    private val dataReceived: (HealthTrackerType, List<DataPoint?>) -> Unit
    ) {
    private val TAG = "TrackActivityy"
    private var isConnected = false
    private var activeTrackers = mutableMapOf<HealthTrackerType, HealthTracker>()
    private var activeListeners = mutableMapOf<HealthTrackerType, TrackerEventListener>()
    lateinit var healthTrackingService: HealthTrackingService
    val connectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            Log.d(TAG, "Connection success")
            isConnected = true
            onConnected()
        }

        override fun onConnectionEnded() {
            resetAllTrackers()
            isConnected = false
            Log.d(TAG, "Connection ENDED")
        }

        override fun onConnectionFailed(e: HealthTrackerException) {
            isConnected = false
            if (e.hasResolution()) {
                onResolutionRequired(e)
                Log.d(TAG, "Connection FAILED")
            }
        }
    }

    private fun createListenerForType(type: HealthTrackerType): TrackerEventListener{
        return object : TrackerEventListener {
            override fun onError(error: TrackerError?) {
                Log.d(TAG, "TRACKER ERRORRRR!!!!!!!!!!! : $error")
            }

            override fun onDataReceived(p0: List<DataPoint?>) {
                if (p0.isNotEmpty()) {
                    dataReceived(type, p0)
                } else {
                    Log.d(TAG, "No heart rate data received")
                }
            }
            override fun onFlushCompleted() {
            }
        }
    }
    fun connect() {
        Log.d(TAG, "Connecting to Health Tracking Service..")
        healthTrackingService = HealthTrackingService(connectionListener, context)
        healthTrackingService.connectService()
    }
    fun disconnect(){
        resetAllTrackers()
        if(::healthTrackingService.isInitialized) { healthTrackingService.disconnectService() }
        isConnected = false
    }

    fun startTracker(type: HealthTrackerType) {
        if(!isConnected) {
            Log.d(TAG, "Health Tracking service is not connected")
            return
        }
        try{
            val tracker = healthTrackingService.getHealthTracker(type)
            val listener = createListenerForType(type)

            tracker.setEventListener(listener)

            activeTrackers[type] = tracker
            activeListeners[type] = listener

            Log.d(TAG, "Started tracking ${type.name}")
        } catch (e: Exception){
            Log.d(TAG, "Error starting ${type.name} ${e.message}:")
        }
    }

    fun stopTracker(type: HealthTrackerType) {
        activeTrackers[type]?.let { tracker ->
            tracker.unsetEventListener()
            activeTrackers.remove(type)
            activeListeners.remove(type)
            Log.d(TAG, "Stopped tracking")
            return
        }
        Log.d(TAG, "Tracker ${type.name} is not active")
    }

    fun pauseAllTrackers(){
        activeTrackers.values.forEach { it.unsetEventListener() }
        Log.d(TAG, "All trackers paused")
    }

    fun resumeAllTrackers(){
        activeTrackers.forEach { (type, tracker) ->
            val listener = activeListeners[type] ?: createListenerForType(type)
            tracker.setEventListener(listener) }
        Log.d(TAG, "All trackers resumed")
    }

    fun resetAllTrackers(){
        pauseAllTrackers()
        activeTrackers.clear()
        activeListeners.clear()
    }
}