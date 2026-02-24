package com.example.MemoriaHomeWatch.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


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
    }
    lateinit var permissionsArray: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // declaring appropriate permissions
        val permissionsList = mutableListOf(
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_HEALTH
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            permissionsList.add(HealthPermissions.READ_HEART_RATE)
            permissionsList.add(HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND)
            permissionsList.add(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            permissionsList.add(Manifest.permission.BODY_SENSORS)
            permissionsList.add(Manifest.permission.BODY_SENSORS_BACKGROUND)
        }
        Log.d(TAG, "using: $permissionsList")

        permissionsArray = permissionsList.toTypedArray()

        // check if all permissions have been granted
        if (!checkPermission(this, permissionsArray)) {
            ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQ_TAG)
        }else {
            Log.i(TAG, "Permissions already granted")
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TrackingActivity::class.java))
            finish()
        }
    }

    // gets called automatically after a user interacts with a permission request dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_TAG) {
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult : permission denied")
                    Toast.makeText(this, "must accept all permissions to get full experience", Toast.LENGTH_LONG).show()
                    return
                }else {
                    Log.i(TAG, "onRequestPermissionsResult : permission granted")
                }
            }
            startActivity(Intent(this, TrackingActivity::class.java))
            finish()
        }
    }
}