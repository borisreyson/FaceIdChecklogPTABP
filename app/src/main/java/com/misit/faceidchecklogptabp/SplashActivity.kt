package com.misit.faceidchecklogptabp

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.Utils.*
import com.misit.faceidchecklogptabp.services.LocationService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SplashActivity  : AppCompatActivity() {
    private val requestPermissionCode= 13
    private val listPermission = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var managePermissions: ManagePermissions
    private var app_version: String? = ""
    lateinit var runnable: Runnable
    lateinit var handler: Handler
    var tipe: String? = null
    private var LAST_VERSION: Float? = null
    var lm:LocationManager?=null
    var gps_enabled = false
    var network_enabled = false
    var tokenPassingReceiver : BroadcastReceiver?=null
    lateinit var bgMapService : Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bgMapService = Intent(this@SplashActivity,MapUtilsService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.apply {
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
        managePermissions = ManagePermissions(this@SplashActivity,listPermission,requestPermissionCode)

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        handler = Handler()
        PrefsUtil.initInstance(this)
        tipe = intent.getStringExtra(TIPE)
        Glide.with(this).load(R.drawable.abp).into(imageView)
        versionApp()
        reciever()

    }

    override fun onResume() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            managePermissions.checkPermissions()
        }
        cekLogin()
        gps()

        LocalBroadcastManager.getInstance(this@SplashActivity).registerReceiver(tokenPassingReceiver!!, IntentFilter(Constants.APP_ID))

        super.onResume()
    }
    private fun cekLogin(){
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
            NAMA = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP,"")
            PERUSAHAAN = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
            cekMapArea()
        }else{
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        }
        Log.d("PERUSAHAAN2",PERUSAHAAN)
    }
    private fun cekMapArea(){
        var mapArea = MapAreaDataSource(this@SplashActivity)
        if(mapArea.newMap(PERUSAHAAN,"")<=0){
            Log.d("JobScheduler1","$PERUSAHAAN")
            startService(bgMapService)
        }
    }
    private fun gps(){
        try {
            gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            Log.d("errorGPS","${ex.message}")
        }

        try {
            network_enabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            Log.d("errorGPS","${ex.message}")
        }

        if(!gps_enabled && !network_enabled) {
            AlertDialog.Builder(this@SplashActivity)
                .setMessage("GPS Not Enable")
                .setPositiveButton("Enable GPS", { dialogInterface, i ->
                      startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
                .setNegativeButton("Cancel", { dialog, which ->
                    Toasty.error(this@SplashActivity,"GPS Must Enable").show()
                    finish()
                })
                    .show()
        }else{
            androidToken()
            ConfigUtil.startStopService(LocationService::class.java,this@SplashActivity,"${NIK}",tokenPassingReceiver!!)
        }
    }
    //versionApp
    fun versionApp() {
        try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
            LAST_VERSION = pInfo.versionName.toFloat()
            tvVersionCode.text = " V.${app_version}"

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
    //    versionApp
    //    androidToken
    fun androidToken() {
        var tokenAndroid :String?=null
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@SplashActivity,
                        "Error : $task.exception",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                updateProgress(task.result)
            })

    }
    //    androidToken
    fun loadFirst(android_token: String) {
        Log.d("FaceId", "$android_token")
        try {
            if (PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN, false)) {
                var nik = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK, "")
                val apiEndPoint = ApiClient.getClient(this@SplashActivity)?.create(ApiEndPoint::class.java)
                GlobalScope.launch {
                    try {
                        val call = apiEndPoint?.tokenCorutine(nik, "faceId", android_token)
                        if (call != null) {
                            if (call.isSuccessful) {
                                val response = call.body()
                                if (response != null) {
                                    Log.d("FaceId", "Data  ${response.toString()}")
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
                                    loadFirst(android_token)
                                }
                            } else {
                                loadFirst(android_token)
                            }
                        } else {
                            loadFirst(android_token)
                        }


                    } catch (e: Exception) {
                        Log.e("MainError", "${e.message}")
                        loadFirst(android_token)
                    } catch (e: InterruptedException) {
                        Log.e("MainError", "${e.message}")
                        loadFirst(android_token)
                    }
                }
            } else {
                loadPage("Login")
            }
        }catch (e: Exception){
            Toasty.error(this@SplashActivity, "${e.message}").show()
        }

    }
    fun updateProgress(android_token: String) {
        runnable = Runnable {
            var besar = progressHorizontal.progress
            progressHorizontal.progress = besar + 10
            if (besar < 40) {
                updateProgress(android_token)
            } else if (besar < 100) {
                updateProgress(android_token)
            } else if (besar == 100) {
                handler.removeCallbacks(runnable)
                loadFirst(android_token)
            }
        }
        handler.postDelayed(runnable, 100)
    }
    //    loadPage
    fun loadPage(pageName: String) {
        var intent: Intent? = null
        if (pageName == "Login") {
            PrefsUtil.getInstance().setBooleanState(
                PrefsUtil.IS_LOGGED_IN, false
            )
            intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else if (pageName == "Index") {
            intent = Intent(this@SplashActivity, HomeActivity::class.java)
            intent.putExtra(IndexActivity.TIPE, tipe)
            startActivity(intent)
            finish()
        }
    }
//    loadPage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionCode -> {
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(requestCode, permissions, grantResults)
                if (isPermissionsGranted) {
                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    finish()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        managePermissions.checkPermissions()
                    }
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    companion object {
        var TIPE = "TIPE"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var LAT = 0.0
        var LNG = 0.0
        var NotifNik = "NotifNik"
        var PERUSAHAAN = "PERUSAHAAN"
        private const val DEBUG_TAG = "NetworkStatusExample"
    }
    private fun reciever() {
        tokenPassingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    if (bundle.containsKey("fgLocation")) {
                        val tokenData = bundle.getString("fgLocation")
                        if(tokenData=="fgDone"){
                            LocalBroadcastManager.getInstance(this@SplashActivity).unregisterReceiver(tokenPassingReceiver!!)
                            Log.d("ServiceName", tokenData)
                        }
                    }
                }
            }
        }
    }
}