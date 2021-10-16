package com.misit.faceidchecklogptabp.services
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.inforoeste.mocklocationdetector.MockLocationDetector
import com.misit.faceidchecklogptabp.Utils.Constants
class GetLocation(c: Context) {
    var TAG = "GetLocation"
    var konteks = c
    var locationManager : LocationManager
    var lokasi:Location?=null
    init {
        locationManager = c.getSystemService(LOCATION_SERVICE) as LocationManager
    }
    fun locationCurrent(c:Context){
        try {
            // Request location updates
            var gps_enable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            var network_enable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(gps_enable){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, gpsListener)
            }
            if (network_enable){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, networkListener)
            }
        } catch(ex: SecurityException) {
            Log.d(TAG, "Security Exception, no location available")
        }
    }
    //define the listener
    private val gpsListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "${location.longitude} : ${location.latitude}")
//          sendMessageToActivity(konteks,"fgLocation","fgUpdate")
            sendMessageToActivity(konteks,"fgLat","${location.latitude}")
            sendMessageToActivity(konteks,"fgLng","${location.longitude}")
            mockLocation(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    //define the listener
    private val networkListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "${location.longitude} : ${location.latitude}")
//          sendMessageToActivity(konteks,"fgLocation","fgUpdate")
            sendMessageToActivity(konteks,"fgLat","${location.latitude}")
            sendMessageToActivity(konteks,"fgLng","${location.longitude}")
            mockLocation(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    //Check Mock Location
    private fun mockLocation(lokasi:Location){
        var isMock = MockLocationDetector.isLocationFromMockProvider(konteks,lokasi)
        if (isMock) {
            sendMessageToActivity(konteks,"fgMock","${isMock}")
        } else {
            sendMessageToActivity(konteks,"fgMock","${isMock}")
        }
    }
    //Check Mock Location
    private fun sendMessageToActivity(c: Context,name: String,msg: String) {
        val intent = Intent()
        intent.action = Constants.APP_ID
        intent.putExtra(name, msg)
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent)
    }
}