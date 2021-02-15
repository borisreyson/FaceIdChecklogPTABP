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
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Response.AbpResponse
import com.misit.faceidchecklogptabp.Response.AndroidTokenResponse
import com.misit.faceidchecklogptabp.Response.AppVersionResponse
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
    lateinit var  mFirebaseAnalytics: FirebaseAnalytics
    var isWifiConn: Boolean = false
    lateinit var url_base:ApiClient
    private var mLocationManager : LocationManager?=null
    private var mLocation : Location?= null
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

        handler = Handler()
        PrefsUtil.initInstance(this)
//        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        getGPS()
//        var activeNetwork = connMgr.getActiveNetworkInfo()
//        if (activeNetwork != null) {
//            // connected to the internet
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                // connected to wifi
//                isWifiConn = isWifiConn
//            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                // connected to mobile data
//            }
//        } else {
//            // not connected to the internet
//        }

//        var url_base = ApiClient
//        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)) {
//            if(PrefsUtil.getInstance()
//                    .getStringState(PrefsUtil.NIK,null)=="18060207"){
//                url_base.loadURL("https://abpjobsite.com")
//                updateProgress()
//            }else{
//                url_base.loadURL("http://10.10.3.13")
//                if(isWifiConn){

//                }else{
//                    koneksiInActive()
//                }
//            }
//        }
//        Log.d(DEBUG_TAG, "Wifi connected: $isWifiConn")

//        updateProgress()

        cekLokasi()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            verifyStoragePermissions()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  or View.SYSTEM_UI_FLAG_FULLSCREEN
            }

        }
        androidToken()
        versionApp()


        tipe = intent.getStringExtra(TIPE)
        Glide.with(this).load(R.drawable.abp).into(imageView)

        tvVersionCode.text=" V.${app_version}"



    }

    override fun onResume() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
//                    updateProgress()
            }

            override fun onAdFailedToLoad(errorCode : Int) {
//                    updateProgress()
            }

            override fun onAdOpened() {
            }

            override fun onAdClicked() {
            }

            override fun onAdLeftApplication() {
            }

            override fun onAdClosed() {
            }
        }
        super.onResume()
    }
    fun getGPS(){
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            if(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){
                PackageManager.PERMISSION_GRANTED
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                return
            }else{
                updateProgress()
            }
        }
        assert(mLocationManager!=null)
        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
            10,10f,this)
    }
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
//                    Toasty.info(this@MainActivity,"B",Toasty.LENGTH_LONG).show()
                    return
                }else
                {
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
//                    Toast.makeText(this,"tidak mendapatkan lokasi",Toast.LENGTH_SHORT).show()
                }
                mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    10,10f,this)
                updateProgress()
//                PopupUtil.showLoading(this,"","Finding your location")
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)

//                Toasty.info(this@MainActivity,"C",Toasty.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun verifyStoragePermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission1 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permition Denied", "Permission to record denied")
        }
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permition Denied", "Permission to record denied")
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permition Denied", "Permission to record denied")
        }
    }


    fun versionApp(){
         try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
            LAST_VERSION=pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
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
    fun updateProgress(){
        runnable= Runnable{
            var besar = progressHorizontal.progress
            progressHorizontal.progress = besar + 10
            if (besar == 40) {
                cekLokasi()
            }else if(besar<75){
                checkVersion()
            }else if(besar<100){
                updateProgress()
            } else {
                if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false))
                {
                    var nik = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")

                    val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
                    val call = apiEndPoint.getAndroidToken(nik,"faceId")
                    call?.enqueue(object : Callback<AndroidTokenResponse?> {
                        override fun onFailure(call: Call<AndroidTokenResponse?>, t: Throwable) {
                            PrefsUtil.getInstance().setBooleanState(
                                PrefsUtil.IS_LOGGED_IN,false)
                            intent = Intent(this@MainActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()

                        }

                        override fun onResponse(
                            call: Call<AndroidTokenResponse?>,
                            response: Response<AndroidTokenResponse?>
                        ) {
                            var androidToken = response.body()

                            if(androidToken==null){
                                PrefsUtil.getInstance().setBooleanState(
                                    PrefsUtil.IS_LOGGED_IN,false)
                                intent = Intent(this@MainActivity, LoginActivity::class.java)

                                startActivity(intent)
                                finish()
                            }else{
//                                Toasty.info(this@MainActivity,androidToken.phoneToken.toString()).show()
                                if(androidToken.phoneToken==android_token){
                                var intent = Intent(this@MainActivity, IndexActivity::class.java)
                                    intent.putExtra(IndexActivity.TIPE,tipe)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    PrefsUtil.getInstance().setBooleanState(
                                        PrefsUtil.IS_LOGGED_IN,false)
                                    intent = Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                            }

                        }

                    })
                }
                else
                {
                    intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                handler.removeCallbacks(runnable)
            }
        }
        handler.postDelayed(runnable,100)
    }
    fun androidToken(){
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@MainActivity,"Error : $task.exception", Toast.LENGTH_SHORT).show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result?.token
            })
    }
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
    fun cekLokasi(){
        val apiEndPoint = ApiClient.getClient(this@MainActivity)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.cekLokasi()
        call?.enqueue(object : Callback<AbpResponse?> {
            override fun onFailure(call: Call<AbpResponse?>, t: Throwable) {
                koneksiInActive()
            }

            override fun onResponse(call: Call<AbpResponse?>, response: Response<AbpResponse?>) {
                var koneksiCek = response.body()
                if(koneksiCek!=null){
                    versionApp()
                    updateProgress()
                }else{
                    koneksiInActive()
                }
            }

        })

    }

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

