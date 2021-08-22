package com.misit.faceidchecklogptabp.Absen.v1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.misit.faceidchecklogptabp.R

class AbsenMapActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap : GoogleMap?= null
    private var lat = 0.0
    private var lng = 0.0
    private var nama = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment?.getMapAsync(this)

        lat = intent.getDoubleExtra(KEY_LAT,0.0)
        lng = intent.getDoubleExtra(KEY_LNG,0.0)
        nama = intent.getStringExtra(NAMA)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        val userLocation = LatLng(lat,lng)
        mMap?.addMarker(MarkerOptions().position(userLocation).title(nama))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){ return }
        mMap?.isMyLocationEnabled = true

        val gson= Gson()
//        val listOfObject = object : TypeToken<List<StoreItem?>?>() {}.type
    }

    companion object{
        val KEY_LAT = "key_lat"
        val KEY_LNG = "key_lng"
        val NAMA = "NAMA"
    }
}
