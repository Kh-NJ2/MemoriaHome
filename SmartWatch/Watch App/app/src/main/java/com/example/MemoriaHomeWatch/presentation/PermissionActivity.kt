package com.example.MemoriaHomeWatch.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.tooling.preview.devices.WearDevices

// handles permissions checking and requesting
class PermissionActivity : FragmentActivity() {

    companion object {
        private const val PERMISSION_REQ_TAG = 1
        private const val TAG = "PERMISSIONN"

        fun checkPermission(context: Context?, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (context == null || ActivityCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "checkPermission : PERMISSION_DENIED : $permission")
                    return false
                } else {
                    Log.i(TAG, "checkPermission : PERMISSION_GRANTED : $permission")
                }
            }
            return true
        }

        // declaring appropriate permissions
        val foregroundPermissionsArray : Array<String> by lazy {
            val permissionsList = mutableListOf(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_HEALTH,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
            // some permissions get auto denied on newer android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) { /* API 36 and above | Android 16 and above */
                permissionsList.add(HealthPermissions.READ_HEART_RATE)
            } else {
                permissionsList.add(Manifest.permission.BODY_SENSORS)
            }
            permissionsList.toTypedArray()
        }

        val backgroundPermissionsArray : Array<String> by lazy {
            val permissionsList = mutableListOf<String>()
            // some permissions get auto denied on newer android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) { /* API 36 and above | Android 16 and above */
                permissionsList.add(HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND)
            } else {
                permissionsList.add(Manifest.permission.BODY_SENSORS_BACKGROUND)
            }
            permissionsList.toTypedArray()
        }
    }
    private var isFinishingActivity = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionScreen(
                onOpenSettings = { goToSettings() }
            )
        }
    }

    override fun onResume(){
        super.onResume()
        if (!isFinishingActivity) {
            checkAllPermissions()
        }
    }

    fun checkAllPermissions() {

        if (checkPermission(this, foregroundPermissionsArray)) { // check if all foreground permissions have been granted
            Log.i(TAG, "Foreground Permissions already granted")

            if (checkPermission(this, backgroundPermissionsArray)) { // check if all background permissions have been granted
                Log.i(TAG, "Background Permissions already granted")
                goToNextActivity()
            } else {
                Log.i(TAG, "requesting Background Permissions") // request background permissions
                ActivityCompat.requestPermissions(this, backgroundPermissionsArray, PERMISSION_REQ_TAG)
            }
        }else {
            Log.d(TAG, "Requesting foreground permission")  // request foreground permissions
            ActivityCompat.requestPermissions(this, foregroundPermissionsArray, PERMISSION_REQ_TAG)
        }
    }

    fun goToNextActivity(){
        if (isFinishingActivity) return
        isFinishingActivity = true
        startActivity(Intent(this, TrackingActivity::class.java))
        finish()
    }

    fun goToSettings(){
        isFinishingActivity = true
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts(("package"), packageName, null)).also {
            startActivity(it)
        }
        // gives 10 seconds before checking the permissions again (critical for activity switching) however (will go back to settings on back button press if user spent 10+ seconds in setting and did not grant the required permissions)
        Handler().postDelayed({
            isFinishingActivity = false
        }, 10000)
    }

    // gets called automatically after a user interacts with a permission request dialog
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQ_TAG || grantResults.isEmpty()) return
        val denied = permissions.filterIndexed { index, _ ->
            grantResults[index] == PackageManager.PERMISSION_DENIED
        }

        if (denied.isEmpty()) return

        val firstDenied = denied[0]
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, firstDenied)) {
            Toast.makeText(this, "Permissions needed for health tracking", Toast.LENGTH_LONG).show()

        } else {
            // Permanent denial - Send to Settings
            Toast.makeText(this, "Enable permissions in Settings to continue", Toast.LENGTH_LONG).show()
            goToSettings()
        }
    }
}

@Composable
fun PermissionScreen(onOpenSettings: () -> Unit) {
    MaterialTheme {
        Scaffold(
            timeText = { TimeText() },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.caption1,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Grant Permissions", fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PermissionScreenPreview() {
    PermissionScreen(onOpenSettings = {})
}

