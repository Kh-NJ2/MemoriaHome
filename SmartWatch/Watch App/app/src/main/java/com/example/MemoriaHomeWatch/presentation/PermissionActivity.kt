package com.example.MemoriaHomeWatch.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionsList = mutableListOf(
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_HEALTH
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            permissionsList.add(HealthPermissions.READ_HEART_RATE)
        } else {
            permissionsList.add(Manifest.permission.BODY_SENSORS)
        }
        Log.d(TAG, "using: $permissionsList")
        val permissionsArray = permissionsList.toTypedArray()

        if (!checkPermission(this, permissionsArray)) {
            ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQ_TAG)
        }else {
            Log.i(TAG, "Permissions already granted")
            startActivity(Intent(this, TrackingActivity::class.java))
            finish()
        }
    }

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
                    finish()
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