package com.example.MemoriaHomeWatch.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTracker.TrackerError
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey


class TrackingActivity : FragmentActivity(), SensorEventListener {

    companion object{
        private const val TAG = "TrackActivityy"
    }

    private var buttontext by mutableStateOf("Restart Tracking")
    private var isTracking by mutableStateOf(false)

    lateinit var healthTrackingService: HealthTrackingService
    private lateinit var mSensorManager : SensorManager
    private var offBodySensor : Sensor? = null
    // trackers
    lateinit var bpmContinuousTracker : HealthTracker

    // LISTENERS
    private val connectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            // Connection success.
            Log.d(TAG, "Connection success")
            // Tracking Data
            startAllTrackings()
            // Capability Check
        }

        override fun onConnectionEnded() {
            // Connection is ended.
            endAllTrackings()
            Log.d(TAG, "Connection ENDED")
        }

        override fun onConnectionFailed(e: HealthTrackerException) {
            if (e.hasResolution()) {
                Log.d(TAG, "Connection FAILED")
                e.resolve(this@TrackingActivity)
            }
        }
    }
    private val trackerListener = object : TrackerEventListener {

        override fun onError(error: TrackerError?) {
            Log.d(TAG, "TRACKER ERRORRRR!!!!!!!!!!!")
        }

        override fun onDataReceived(p0: List<DataPoint?>) {
            if (p0.isNotEmpty()) {
                for(data in p0){
                    val value = data?.getValue(ValueKey.HeartRateSet.HEART_RATE)
                    Log.d(TAG, "Heart Rate: $value")
                }
            } else {
                Log.d(TAG, "No heart rate data received")
            }
        }

        override fun onFlushCompleted() {
            // Flushing data is completed.
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val permissions = arrayOf(Manifest.permission.)
//        val isAllowed = PermissionActivity.checkPermission(this, permissions)
//
//        if (isAllowed) {
//
//        } else {
//            Log.e("SubActivity", "Blocked: Permissions not granted.")
//            finish()
//            return
//        }

        connectHealthService()

        setContent {
            MaterialTheme {
                TrackAppUi(onExit = {if(isTracking){endAllTrackings()}else{connectHealthService()}}, buttontext)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        endAllTrackings()
    }

    private fun connectHealthService() {
        healthTrackingService = HealthTrackingService(connectionListener, this)
        healthTrackingService.connectService()
    }
    private fun startAllTrackings() {
        bpmContinuousTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS)
        startOffBodySensor()
        startTracking(bpmContinuousTracker)
        buttontext = "Stop Tracking"
        isTracking = true
    }

    fun startTracking(continuousTracker: HealthTracker? = null) {
        Log.d(TAG, "Starting tracker")
        continuousTracker?.setEventListener(trackerListener)
    }

    fun stopTracking(continuousTracker: HealthTracker? = null) {
        continuousTracker?.unsetEventListener()
        Log.d(TAG, "Stopped tracking")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val offBodyDataFloat = p0?.values[0];
        val offBodyData = offBodyDataFloat?.toInt();
        if (offBodyData == 1){
            Log.d(TAG, "Watch is being worn")
            startTracking(bpmContinuousTracker)
        } else {
            Log.d(TAG, "Watch is NOT being worn")
            Toast.makeText(this, "Watch removed",Toast.LENGTH_LONG).show();
            stopTracking(bpmContinuousTracker)
        }
    }

    private fun startOffBodySensor(){
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        offBodySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        mSensorManager.registerListener(this, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private fun endAllTrackings(){
        if(::mSensorManager.isInitialized){ mSensorManager.unregisterListener(this); }
        if(::bpmContinuousTracker.isInitialized){ stopTracking(bpmContinuousTracker) }
        if(::healthTrackingService.isInitialized) { healthTrackingService.disconnectService() }
        buttontext = "start tracking"
        isTracking = false
    }

    fun flushHeartRate() {
        // Flushing data gives collected data instantly.
        val heartRateContinuousTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE)
        heartRateContinuousTracker.flush()
    }


}
@Composable
fun TrackAppUi(onExit: () -> Unit, buttontext: String) {
    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onExit, modifier = Modifier.size(100.dp)) {
                Text(buttontext)
            }
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TrackingActivityPreview() {
    MaterialTheme {
        TrackAppUi(onExit = {}, buttontext = "Stop Tracking")
    }
}