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
import com.misit.faceidchecklogptabp.Utils.ConfigUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private var app_version: String? = ""
    lateinit var runnable: Runnable
    lateinit var handler: Handler
    private var android_token: String? = null
    var tipe: String? = null
    private var LAST_VERSION: Float? = null

    //    lateinit var mAdView : AdView
//    lateinit var  mFirebaseAnalytics: FirebaseAnalytics
    var isWifiConn: Boolean = false
    lateinit var url_base: ApiClient
    private var mLocationManager: LocationManager? = null
    private var mLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dirInit(this@MainActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.apply {
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
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

        tvVersionCode.text = " V.${app_version}"
        if (PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN, false)) {
            updateProgress()
        }else{
            loadPage("Login")
        }
    }

    private fun dirInit(c:Context){
        ConfigUtil.deleteInABPIMAGES(c,"ABP_IMAGES")
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                }
//                mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    10,10f,this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //    verifyStoragePermissions
    private fun verifyStoragePermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val permission1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permission2 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "READ_EXTERNAL_STORAGE Permission to record denied")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                11
            )
//            finish()
        } else {
            updateProgress()
        }
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "WRITE_EXTERNAL_STORAGE Permission to record denied")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                12
            )
//            finish()
        } else {
            updateProgress()
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.i("FaceId", "READ_PHONE_STATE Permission to record denied")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                13
            )
//            finish()
        } else {
            updateProgress()
        }
    }
//verifyStoragePermissions

    //versionApp
    fun versionApp() {
        try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
            LAST_VERSION = pInfo.versionName.toFloat()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
//    versionApp

    //    checkVersion
    fun checkVersion() {

        val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
        GlobalScope.launch {
            val call = apiEndPoint?.getAppVersionCorutine("face")
            if(call!=null){
                if(call.isSuccessful){
                    val response = call.body()
                    if(response!=null){
                        if (response.version != null) {
                            LAST_VERSION = response.version?.toFloat()
                            var currentVersion = app_version?.toFloat()
                            if (currentVersion!! < LAST_VERSION!!) {
                                updateAplication(response.url!!)
                            } else {
                                updateProgress()
                            }
                        } else {
                            updateProgress()
                        }
                    }else{
                        checkVersion()
                    }
                }else{
                    checkVersion()
                }
            }else{
                checkVersion()
            }
        }
    }
//    checkVersion

    fun updateProgress() {
        runnable = Runnable {
            var besar = progressHorizontal.progress
            progressHorizontal.progress = besar + 10
            if (besar < 40) {
//                verifyStoragePermissions()
                updateProgress()
            } else if (besar < 100) {
                updateProgress()
            } else if (besar == 100) {
                handler.removeCallbacks(runnable)
                loadFirst()
            }
        }
        handler.postDelayed(runnable, 100)
    }

    fun loadFirst() {
        Log.v("FaceId", android_token)
        if (PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN, false)) {
            var nik = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK, "")
            val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
            GlobalScope.launch {
                try {
                    val call = apiEndPoint?.tokenCorutine(nik, "faceId", android_token)
                    if (call != null) {
                        if (call.isSuccessful) {
                            val response = call.body()
                            if (response != null) {
                                Log.v("FaceId", "Data" + response.toString())
                                if (response.absensi == null) {
                                    loadPage("Login")
                                } else {
                                    if (response.absensi.phoneToken == android_token) {
                                        loadPage("Index")
                                    } else {
                                        loadPage("Index")
                                    }

                                }
                            } else {
                                loadFirst()
                            }
                        } else {
                            loadFirst()
                        }
                    } else {
                        loadFirst()
                    }


                } catch (e: Exception) {
                    Log.e("MainError","${e.message}")
                    loadFirst()
                } catch (e: InterruptedException) {
                    Log.e("MainError","${e.message}")
                    loadFirst()
                }
            }
        } else {
            loadPage("Login")
        }
    }

    //    androidToken
    fun androidToken() {
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Error : $task.exception", Toast.LENGTH_SHORT)
                        .show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result
            })
    }
//    androidToken

    //    updateAplication
    fun updateAplication(uriString: String) {
        AlertDialog.Builder(this)
            .setTitle("Pambaharuan , Aplikasi Versi " + LAST_VERSION + " Telah Tersedia.")
            .setPositiveButton("Ya, Perbaharui", { _,
                                                   _ ->
                var intent = Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uriString));
                startActivity(intent);
            })
            .setNegativeButton("Tidak, Keluar", { _, _ ->
                finish()
            })
            .setOnDismissListener({ _ -> finish() })
            .show()
    }
//    updateAplication

    //    koneksiInActive
    fun koneksiInActive() {
        AlertDialog.Builder(this)
            .setTitle("Maaf , Anda Harus Menggunakan Jaringan Wifi PT. ABP!")
            .setPositiveButton("OK, Keluar", { _,
                                               _ ->
                finish()
            })
            .setOnDismissListener({ _ -> finish() })
            .show()
    }
//    koneksiInActive


    //    loadPage
    fun loadPage(pageName: String) {
        var intent: Intent? = null
        if (pageName == "Login") {
            PrefsUtil.getInstance().setBooleanState(
                PrefsUtil.IS_LOGGED_IN, false
            )
            intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else if (pageName == "Index") {
            intent = Intent(this@MainActivity, HomeActivity::class.java)
            intent.putExtra(IndexActivity.TIPE, tipe)
            startActivity(intent)
            finish()
        } else {

        }


    }
//    loadPage

    companion object {
        var TIPE = "TIPE"
        //        var LAST_VERSION = null
        var NIK = "NIK"
        var NAMA = "NAMA"
        var LAT = 0.0
        var LNG = 0.0
        var NotifNik = "NotifNik"
        private const val DEBUG_TAG = "NetworkStatusExample"
    }


}