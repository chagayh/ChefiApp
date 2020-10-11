package com.example.chefi.track

import android.Manifest
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class LocationTracker(val _activity: Activity, val _info: LocationInfo) {
    val requestCodePermissionFineLocation = 1
    // Location structs:
    private val _fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(_activity)
    private val locationRequest = LocationRequest.create().apply {
        interval = 0 // millisec
        fastestInterval = 0 // millisec
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            _info._latitude = locationResult.lastLocation.latitude
            _info._longitude = locationResult.lastLocation.longitude
            _info._accuracy = locationResult.lastLocation.accuracy
            _activity.sendBroadcast(Intent("tracker"))
        }
    }

    private fun checkUnderlyingProviders(): Boolean{
        val locationManager: LocationManager =
            _activity.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun startTracking(){
        val hasCoarseLocationPermission:Boolean =
            ActivityCompat.checkSelfPermission(
                _activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasCoarseLocationPermission && checkUnderlyingProviders()) {
            _fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }else{
            ActivityCompat.requestPermissions(
                _activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCodePermissionFineLocation);
        }
    }

    fun stopTracking(){
        _fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}

data class LocationInfo(
    var _latitude:Double = 0.0,
    var _longitude: Double = 0.0,
    var _accuracy: Float = 0F)