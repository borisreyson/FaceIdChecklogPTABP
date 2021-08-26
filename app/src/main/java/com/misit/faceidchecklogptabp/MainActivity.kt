package com.misit.faceidchecklogptabp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Response.AbpResponse
import com.misit.faceidchecklogptabp.Response.AndroidTokenResponse
import com.misit.faceidchecklogptabp.Response.AppVersionResponse
import com.misit.faceidchecklogptabp.Response.MainResponse.FirstLoadResponse
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), LocationListener {

    private var app_version : String?=""
    lateinit var runnable:Runnable
    lateinit var handler: Handler
    private var android_token : String?=null
    var tipe:String?=null
    lateinit var mAdView : AdView
//    lateinit var  mFirebaseAnalytics: FirebaseAnalytics
    var isWifiConn: Boolean = false
    lateinit var url_base:ApiClient
    private var mLocationManager : LocationManager?=null
    private var mLocation : Location?= null

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  or View.SYSTEM_UI_FLAG_FULLSCREEN
            }

        }
        handler = Handler()
        PrefsUtil.initInstance(this)
//        getGPS()
//        updateProgress()
        androidToken()
        versionApp()

        tipe = intent.getStringExtra(TIPE)
        Glide.with(this).load(R.drawable.abp).into(imageView)

        tvVersionCode.text=" V.${app_version}"
        updateProgress()
    }

    override fun onResume() {
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
//        MobileAds.initialize(this) {}
//
//        mAdView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
//
//        mAdView.adListener = object: AdListener() {
//            override fun onAdLoaded() {
////                    updateProgress()
//            }
//
//            override fun onAdFailedToLoad(errorCode : Int) {
////                    updateProgress()
//            }
//
//            override fun onAdOpened() {
//            }
//
//            override fun onAdClicked() {
//            }
//
//            override fun onAdLeftApplication() {
//            }
//
//            override fun onAdClosed() {
//            }
//        }
        super.onResume()
    }

//    fun getGPS(){
//        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
//            if(ActivityCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED ){
//                PackageManager.PERMISSION_GRANTED
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
//                return
//            }else{
//                updateProgress()
//            }
//        }
//        assert(mLocationManager!=null)
//        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//            10,5f,this)
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                {
                    return
                }else
                {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                }
//                mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    10,10f,this)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

//    verifyStoragePermissions
    private fun verifyStoragePermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission1 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "READ_EXTERNAL_STORAGE Permission to record denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),11)
//            finish()
        }else{
            updateProgress()
        }
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "WRITE_EXTERNAL_STORAGE Permission to record denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),12)
//            finish()
        }else{
            updateProgress()
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "READ_PHONE_STATE Permission to record denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),13)
//            finish()
        }else{
            updateProgress()
        }
    }
//verifyStoragePermissions

//versionApp
    fun versionApp(){
         try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
            LAST_VERSION=pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
//    versionApp

//    checkVersion
    fun checkVersion(){

        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getAppVersion("face")
        call?.enqueue(object : Callback<AppVersionResponse?> {
            override fun onFailure(call: Call<AppVersionResponse?>, t: Throwable) {
                finish()
            }

            override fun onResponse(
                call: Call<AppVersionResponse?>,
                response: Response<AppVersionResponse?>
            ) {
                var resVersion = response.body()
                if(resVersion!=null){
                    LAST_VERSION = resVersion.version!!
                    var currentVersion=app_version?.toFloat()
                    var lastVersion=LAST_VERSION.toFloat()
                    if(currentVersion!! < lastVersion){
                        updateAplication(resVersion.url!!)
                    }else{
                        updateProgress()
                    }
                }
            }

        })
    }
//    checkVersion

    fun updateProgress(){
        runnable= Runnable{
            var besar = progressHorizontal.progress
            progressHorizontal.progress = besar + 10
            if(besar<40){
                verifyStoragePermissions()
            }else if(besar<100){
            updateProgress()
            } else if(besar==100){
                handler.removeCallbacks(runnable)
                loadFirst()
            }
        }
        handler.postDelayed(runnable,100)
    }

    fun loadFirst(){
        Log.v("FaceId",android_token)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false))
        {
            var nik = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
            val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
            val call = apiEndPoint.getAndroidToken(nik,"faceId",android_token)
            call?.enqueue(object : Callback<FirstLoadResponse?> {
                override fun onFailure(call: Call<FirstLoadResponse?>, t: Throwable) {
                    Log.v("FaceId","Error: "+t.toString())
                    koneksiInActive()
                }
                override fun onResponse(
                    call: Call<FirstLoadResponse?>,
                    response: Response<FirstLoadResponse?>
                ) {
                    var loadResponse = response.body()

                    if(loadResponse!=null) {
                        Log.v("FaceId","Data"+loadResponse.toString())
                        if (loadResponse!!.absensi == null) {
                            loadPage("Login")
                        } else {
                            if (loadResponse!!.absensi!!.phoneToken == android_token) {
                                loadPage("Index")
                            } else {
                                loadPage("Index")
                            }

                        }
                    }else{
                        loadPage("Login")
                    }

                }

            })
        }

        else
        {
            loadPage("Login")
        }
    }
//    androidToken
    fun androidToken(){
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@MainActivity,"Error : $task.exception", Toast.LENGTH_SHORT).show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result
            })
    }
//    androidToken

//    updateAplication
    fun updateAplication(uriString:String){
        AlertDialog.Builder(this)
            .setTitle("Pambaharuan , Aplikasi Versi "+ LAST_VERSION+" Telah Tersedia.")
            .setPositiveButton("Ya, Perbaharui",{
                    _,
                    _ ->
                var intent = Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uriString));
                startActivity(intent);
            })
            .setNegativeButton("Tidak, Keluar",{
                _, _ ->
                finish()
            })
            .setOnDismissListener({_ -> finish()  })
            .show()
    }
//    updateAplication

//    koneksiInActive
    fun koneksiInActive(){
        AlertDialog.Builder(this)
            .setTitle("Maaf , Anda Harus Menggunakan Jaringan Wifi PT. ABP!")
            .setPositiveButton("OK, Keluar",{
                    _,
                    _ ->
                finish()
            })
            .setOnDismissListener({_ -> finish()  })
            .show()
    }
//    koneksiInActive


//    loadPage
    fun loadPage(pageName:String){
        var intent:Intent?=null
        if(pageName=="Login"){
            PrefsUtil.getInstance().setBooleanState(
                PrefsUtil.IS_LOGGED_IN, false
            )
            intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else if(pageName=="Index"){
            intent = Intent(this@MainActivity, HomeActivity::class.java)
            intent.putExtra(IndexActivity.TIPE, tipe)
            startActivity(intent)
            finish()
        }else{

        }


    }
//    loadPage

    companion object{
        var TIPE = "TIPE"
        var LAST_VERSION = "LAST_VERSION"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var LAT = 0.0
        var LNG = 0.0
        var NotifNik="NotifNik"
        private const val DEBUG_TAG = "NetworkStatusExample"
    }


    override fun onLocationChanged(location: Location?) {
        mLocation=location
//        mLocationManager?.removeUpdates(this)
        if (location != null) {
            LAT=location.latitude
        }
        if (location != null) {
            LNG=location.longitude
        }
        if (location != null) {
            PrefsUtil.getInstance()
                .setStringState(PrefsUtil.CURRENT_LAT,
                    location.latitude.toString())
        }
        if (location != null) {
            PrefsUtil.getInstance()
                .setStringState(PrefsUtil.CURRENT_LNG,
                    location.longitude.toString())
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}

