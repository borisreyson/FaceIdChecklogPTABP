package com.misit.faceidchecklogptabp

import android.Manifest
import android.app.DatePickerDialog
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import com.google.maps.android.PolyUtil
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Absen.v1.*
import com.misit.faceidchecklogptabp.Adapter.Last3DaysAdapter
import com.misit.faceidchecklogptabp.DataSource.AbsensiDataSources
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.Response.AbsenTigaHariItem
import com.misit.faceidchecklogptabp.Utils.*
import com.misit.faceidchecklogptabp.services.FaceIdWorker
import com.misit.faceidchecklogptabp.services.LocationService
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.lupa_masuk.view.*
import kotlinx.android.synthetic.main.lupa_pulang.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(),View.OnClickListener,
    OnMapReadyCallback {
    lateinit var handler : Handler
    lateinit var tm : TelephonyManager
    private var IMEI :String?=null
    private var jamSekarang : Int=0
    private var menitSekarang : Int=0
    private var detikSekarang : Int=0
    var tipe :String?=null
    lateinit var mAdView : AdView
    lateinit var viewPassword: View
    lateinit var alertDialog: AlertDialog
    private var android_token : String?=null
    var tokenPassingReceiver : BroadcastReceiver?=null
    private var adapter: Last3DaysAdapter? = null
    private var absenList: MutableList<AbsenTigaHariItem>? = null
    lateinit var rvLast3Day :RecyclerView
    private var mMap : GoogleMap?= null
    var modelCompany : MutableList<CompanyLocationModel>?= null
    var abpLocation:LatLng?=null
    var mapFragment:SupportMapFragment?=null
    var z =0
    private val updateClock = object :Runnable{
        override fun run() {
            doJob()
            handler.postDelayed(this, 1000)
        }
    }
    lateinit var bgMapService : Intent
    private var scheduler : JobScheduler?=null
    var userLocation :LatLng? = null
    var liveLocation = mMap?.addMarker(MarkerOptions().position(userLocation!!).title(NAMA))
    var mapAbp : MutableList<LatLng>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        tvJam.text =""
        androidToken()
        var intPerm :Int= ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        )
        modelCompany = ArrayList()
        mapAbp = mutableListOf()
        if(intPerm== PackageManager.PERMISSION_GRANTED){
            tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEI = Settings.Secure.getString(
                    this.getContentResolver(),
                    Settings.Secure.ANDROID_ID
                )
            } else {
                IMEI = tm.getDeviceId()
            }
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                123
            )
        }
        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN, false)){
            NAMA = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP, "")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK, "")
            SHOW_ABSEN = PrefsUtil.getInstance().getStringState(PrefsUtil.SHOW_ABSEN, "")
            PERUSAHAAN = PrefsUtil.getInstance().getStringState("PERUSAHAAN", "")
            if(PERUSAHAAN==""){
                Log.d("Perusahaan1", "$PERUSAHAAN")
                PrefsUtil.getInstance().setBooleanState(PrefsUtil.IS_LOGGED_IN, false)
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            }
        }else{
            startActivity(Intent(this@HomeActivity, SplashActivity::class.java))
            finish()
        }
        bgMapService = Intent(this@HomeActivity, MapUtilsService::class.java)

        tipe = intent.getStringExtra(TIPE)
        if(tipe=="terlambat"){
//            listAbsen()
        }
        tvNama.text= NAMA
        tvNik.text= NIK
        handler= Handler(Looper.getMainLooper())
//        MobileAds.initialize(this) {}
//        r = Runnable{
//            doJob()
//        }
        absenList = ArrayList()
        adapter = Last3DaysAdapter(this@HomeActivity, absenList!!, NIK, NAMA)
        rvLast3Day = findViewById(R.id.rvLast3Day)
        val linearLayoutManager = LinearLayoutManager(this@HomeActivity)
        rvLast3Day.layoutManager = linearLayoutManager
        rvLast3Day.adapter= adapter
        btnNewMasuk.setOnClickListener(this)
        btnNewPulang.setOnClickListener(this)
        btnFloatHistory.setOnClickListener(this)
        btnListAllAbsen.setOnClickListener(this)
        lpAbsenMasuk.setOnClickListener(this)
        lpAbsenPulang.setOnClickListener(this)
        lupaAbsen.setOnClickListener(this)
        tvLogOut.setOnClickListener(this)
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(this))
//        }
//        val python = Python.getInstance()
//        val pythonFile = python.getModule("helloworldscript")
//        val helloWorldString = pythonFile.callAttr("helloworld")
//        Toasty.info(this@HomeActivity,"${helloWorldString}",Toasty.LENGTH_LONG).show()
        userLocation =LatLng(LAT, LNG)
        abpLocation = LatLng(-0.5634222, 117.0139606)
        state = 0
        reciever()

    }

    override fun onClick(v: View?) {
        if(v?.id==R.id.btnNewMasuk){
            val intent= Intent(this, MasukActivity::class.java)
            intent.putExtra(MasukActivity.NIK, NIK)
            startActivity(intent)
        }else
        if(v?.id==R.id.btnNewPulang){
            val intent= Intent(this, PulangActivity::class.java)
            intent.putExtra(PulangActivity.NIK, NIK)
            startActivity(intent)
        }else
        if(v?.id==R.id.btnFloatHistory){
            val intent= Intent(this, LihatAbsenActivity::class.java)
            intent.putExtra(LihatAbsenActivity.NIK, NIK)
            startActivity(intent)
        }else
        if(v?.id == R.id.lpAbsenMasuk){
            showDialogLupaMasuk()
        }
        if(v?.id == R.id.lpAbsenPulang){
            showDialogLupaPulang()
        }else
        if (v?.id==R.id.btnListAllAbsen){
            listAbsen()
        }else{
            btnNewLupaAbsen.collapse()
        }
        if(v?.id==R.id.lupaAbsen){
            lupaAbsen(this@HomeActivity)
        }
        if(v?.id==R.id.tvLogOut){
            logout()
        }
    }
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setPositiveButton("OK , Sign Out",{
                    dialog,
                    which ->
                if(PrefsUtil.getInstance().getBooleanState(
                        PrefsUtil.IS_LOGGED_IN,true)){
                    PrefsUtil.getInstance().setBooleanState(
                        PrefsUtil.IS_LOGGED_IN,false)
                    PrefsUtil.getInstance().setStringState(
                        PrefsUtil.NAMA_LENGKAP,null)
                    PrefsUtil.getInstance().setStringState(
                        PrefsUtil.NIK,null)
                    val intent = Intent(this,LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
            .setNegativeButton("Cancel",
                {
                        dialog,
                        which ->
                    dialog.dismiss()
                })
            .show()
    }
    fun listAbsen(){
        val intent= Intent(this, ListAbsenActivity::class.java)
        intent.putExtra(ListAbsenActivity.NIK, NIK)

        startActivity(intent)
    }
    //    androidToken
    fun androidToken(){
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Error : $task.exception", Toast.LENGTH_SHORT)
                        .show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result
            })
    }
    //    androidToken
    override fun onResume() {
        mapAbp?.clear()
        loadFragment()
        LocalBroadcastManager.getInstance(this@HomeActivity).registerReceiver(
            tokenPassingReceiver!!, IntentFilter(
                Constants.APP_ID
            )
        )

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adViewIndex)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.d("errorCode", errorCode.toString())
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        absenList?.clear()
        loadAbsenTigaHari()
        cekLokasi()
        handler.post(updateClock)
        if(SHOW_ABSEN =="1"){
            btnListAllAbsen.visibility=View.VISIBLE
        }else{
            btnListAllAbsen.visibility=View.GONE
        }
        workBackground()
        super.onResume()
    }

    private fun workBackground(){
        if(ConfigUtil.isJobServiceOn(this@HomeActivity, Constants.JOB_SERVICE_ID)){
            var cekMap = MapAreaDataSource(this)
            var absensi = AbsensiDataSources(this)
            var dataMap = cekMap.getMaps(PERUSAHAAN)
            if(dataMap.size<=0){
                Log.d("JobScheduler", "Service Is Running dataMap")

                ConfigUtil.jobScheduler(this@HomeActivity, scheduler)
            }else{
                Log.d("JobScheduler", "Service Is Running MAP Not Null")

                var ceklog =absensi.cekAbsensi(NIK)
                if(ceklog<=0){
                    Log.d("JobScheduler", "Service Is Running ceklog")
                    cekMap.deleteAll()
                    ConfigUtil.jobScheduler(this@HomeActivity, scheduler)
                }else{

                    if(absensi.cekLastAbsen()<=0){
                        Log.d("JobScheduler", "Service Is Running lastAbsen")
                        cekMap.deleteAll()
                        ConfigUtil.jobScheduler(this@HomeActivity, scheduler)
                    }
                }
            }
        }else {
            Log.d("JobScheduler", "New Run Job Scheduler")
            ConfigUtil.jobScheduler(this@HomeActivity, scheduler)
            var cekMap = MapAreaDataSource(this)
            var absensi = AbsensiDataSources(this)
            var dataMap = cekMap.getMaps(PERUSAHAAN)
            if (dataMap.size <= 0) {
                Log.d("JobScheduler", "Service Is Running dataMap")
                startService(bgMapService)
            } else {
                Log.d("JobScheduler", "Service Is Running MAP Not Null")

                var ceklog = absensi.cekAbsensi(NIK)
                if (ceklog <= 0) {
                    Log.d("JobScheduler", "Service Is Running ceklog")
                    cekMap.deleteAll()
                    startService(bgMapService)
                } else {
                    if (absensi.cekLastAbsen()<=0) {
                        Log.d("JobScheduler", "Service Is Running lastAbsen")
                        cekMap.deleteAll()
                        startService(bgMapService)
                    }
                }
            }
        }
    }
    override fun onPause() {
        handler.removeCallbacks(updateClock)
        super.onPause()
    }

    override fun onStop() {
        handler.removeCallbacks(updateClock)
        super.onStop()
    }
    fun doJob() {
            if(detikSekarang!!.toInt()>=59){
                if(menitSekarang!!.toInt()>=59){
                    if(jamSekarang!!.toInt()>=23){
                        jamSekarang==0
                        menitSekarang=0
                        detikSekarang=0
                    }else{
                        jamSekarang=jamSekarang+1
                        menitSekarang=0
                        detikSekarang=0
                    }
                }else{
                    menitSekarang=menitSekarang+1
                    detikSekarang=0
                }
            }else{
                detikSekarang=detikSekarang+1
            }
            if(jamSekarang<10) {
                if(menitSekarang<10){
                    if(detikSekarang<10){
                        tvJam.text = "0" + "${jamSekarang} : 0" + "${menitSekarang} : 0" + "${detikSekarang}"
                    }else{
                        tvJam.text = "0" + "${jamSekarang} : 0" + "${menitSekarang} : ${detikSekarang}"
                    }
                }else{
                    if(detikSekarang<10){
                        tvJam.text = "0" + "${jamSekarang} : ${menitSekarang} : 0" + "${detikSekarang}"
                    }else{
                        tvJam.text = "0" + "${jamSekarang} : ${menitSekarang} : ${detikSekarang}"
                    }
                }
            }else{
                if(menitSekarang<10){
                    if(detikSekarang<10){
                        tvJam.text = "${jamSekarang} : 0" + "${menitSekarang} : 0" + "${detikSekarang}"
                    }else{
                        tvJam.text = "${jamSekarang} : 0" + "${menitSekarang} : ${detikSekarang}"
                    }
                }else{
                    if(detikSekarang<10){
                        tvJam.text = "${jamSekarang} : ${menitSekarang} : 0" + "${detikSekarang}"
                    }else{
                        tvJam.text =  "${jamSekarang} : ${menitSekarang} : ${detikSekarang}"
                    }
                }
            }
            tvTanggal.text = TANGGAL
//            handler.postDelayed(r,1000)
        }
    fun cekLokasi(){
        tvJam.text =""
        val apiEndPoint = ApiClient.getClient(this@HomeActivity)?.create(ApiEndPoint::class.java)
        GlobalScope.launch {
            try {
                val call = apiEndPoint?.tokenCorutine(NIK, "faceId", android_token)
                if (call != null) {
                    if(call.isSuccessful)
                    {
                        val respon = call.body()
                        if(respon!=null){
                            if(respon.jam!=null)
                                jamSekarang = respon.jam!!.toInt()
                            menitSekarang = respon.menit!!.toInt()
                            detikSekarang = respon.detik!!.toInt()
                            TANGGAL = "${respon.hari}, ${respon.tanggal}"
                            cekFaceID()
                        }else{
                            cekLokasi()
                        }
                    }else{
                        cekLokasi()
                    }
                }else{
                    cekLokasi()
                }
            }catch (e: Exception){
                Log.d("ErrorLokasi", "${e.message}")
            }

        }


    }
    fun koneksiInActive(){
        AlertDialog.Builder(this)
            .setTitle("Maaf , Anda Harus Menggunakan Jaringan Wifi PT. ABP!")
            .setPositiveButton("OK, Keluar", { _,
                                               _ ->
                finish()
            })
            .setOnDismissListener({ _ -> finish() })
            .show()
    }
    fun cekFaceID(){
        val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
        GlobalScope.launch {
            val call = apiEndPoint?.dirInfoCorutine(NIK!!)
            if(call!=null){
                if(call.isSuccessful){
                    val respon = call.body()
                    if(respon!=null){
                        if(respon.folder!=null){
                            if(respon.folder){
//                        btnFaceFalse.visibility=View.GONE
//                        btnFaceTrue.visibility=View.VISIBLE
                                PopupUtil.dismissDialog()
//                                loadAbsen()
//                        btnDaftarWajah.visibility=View.GONE
                            }else{
//                        btnFaceFalse.visibility=View.VISIBLE
//                        btnFaceTrue.visibility=View.GONE
                                PopupUtil.dismissDialog()
                                val intent = Intent(
                                    this@HomeActivity,
                                    DaftarWajahActivity::class.java
                                )
                                startActivity(intent)
                            }
                        }else{
                            cekFaceID()
                        }

                    }else{
                        cekFaceID()
                    }
                }else{
                    cekFaceID()
                }
            }else{
                cekFaceID()
            }
        }
    }
    fun loadAbsen(){
        val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
        GlobalScope.launch(Dispatchers.Main){
            val call = apiEndPoint?.lastAbsenCorutine(NIK!!)
            if(call!=null){
                if(call.isSuccessful){
                    val response= call.body()
                    if(response!=null){
                        if(response.lastNew!=null){
                            borderMiddle.visibility = View.VISIBLE
                            if(response.lastNew=="Masuk"){
                                btnNewPulang.isEnabled=true
                                btnNewMasuk.isEnabled=true
                                btnNewMasuk.visibility=View.GONE
                                btnNewPulang.visibility=View.VISIBLE
                                tvNewMasuk.visibility=View.VISIBLE
                                if(response.presensiMasuk!=null){
                                    tvNewMasuk.text = "${response.presensiMasuk?.jam}"
                                }
                                tvNewPulang.visibility=View.GONE
                            }else if(response.lastNew=="Pulang"){
//                                chkMasuk.isChecked=true
//                                chkPulang.isChecked=true
                                btnNewPulang.isEnabled=true
                                btnNewMasuk.isEnabled=true
                                btnNewMasuk.visibility=View.VISIBLE
                                btnNewPulang.visibility=View.GONE
                                tvNewMasuk.visibility=View.GONE
                                tvNewPulang.visibility=View.VISIBLE
                                if(response.presensiMasuk!=null){
                                    val presensiMasuk = response.presensiMasuk
                                    if(presensiMasuk!=null){
                                        tvNewMasuk.text = "${presensiMasuk?.jam}"
                                    }
                                }
                                if(response.presensiPulang!=null){
                                    tvNewPulang.text = "${response.presensiPulang?.jam}"
                                }

                            }else{
//                                chkMasuk.isChecked=false
//                                chkPulang.isChecked=false
                                btnNewMasuk.isEnabled=true
                                btnNewPulang.isEnabled=true
                                btnNewMasuk.visibility=View.VISIBLE
                                btnNewPulang.visibility=View.VISIBLE
                                tvNewMasuk.visibility=View.GONE
                                tvNewPulang.visibility=View.GONE
                            }
                            if(btnNewMasuk.isEnabled==false){

                            }
                        }else{
//                        chkMasuk.isChecked=false
//                        chkPulang.isChecked=false
                            btnNewMasuk.isEnabled=true
                            btnNewPulang.isEnabled=true
                            btnNewMasuk.visibility=View.VISIBLE
                            btnNewPulang.visibility=View.GONE
                        }
                    }else{
                        loadAbsen()
                    }
                }else{
                    loadAbsen()
                }
            }
        }
    }

    fun localAbsen(){
        try {
            var absenLokal = AbsensiDataSources(this@HomeActivity)
            val response = absenLokal?.lastAbsen()
            Log.d("CurrentLocation","Response ${response}")
            if(response!=null){
                if(response.lastNew!=null){
                    borderMiddle.visibility = View.VISIBLE
                    var presensiMasuk = absenLokal.getItem(NIK,"MASUK")
                    var presensiPulang = absenLokal.getItem(NIK,"PULANG")
                    Log.d("CurrentLocation","Response 1 ${presensiMasuk}")
                    Log.d("CurrentLocation","Response 2 ${presensiPulang}")
                    Log.d("CurrentLocation","Response 3 ${response.lastNew}")
                    if(response.lastNew=="Masuk"){
                        btnNewPulang.isEnabled=true
                        btnNewMasuk.isEnabled=true
                        btnNewMasuk.visibility=View.GONE
                        btnNewPulang.visibility=View.VISIBLE
                        tvNewMasuk.visibility=View.VISIBLE
                        if(presensiMasuk!=null){
                            tvNewMasuk.text = "${presensiMasuk?.jam}"
                        }
                        tvNewPulang.visibility=View.GONE
                    }else if(response.lastNew=="Pulang"){
//                                chkMasuk.isChecked=true
//                                chkPulang.isChecked=true
                        btnNewPulang.isEnabled=true
                        btnNewMasuk.isEnabled=true
                        btnNewMasuk.visibility=View.VISIBLE
                        btnNewPulang.visibility=View.GONE
                        tvNewMasuk.visibility=View.GONE
                        tvNewPulang.visibility=View.VISIBLE
                        if(presensiMasuk!=null){
                            tvNewMasuk.text = "${presensiMasuk?.jam}"
                        }
                        if(presensiPulang!=null){
                            tvNewPulang.text = "${presensiPulang?.jam}"
                        }

                    }else{
                        btnNewMasuk.isEnabled=true
                        btnNewPulang.isEnabled=true
                        btnNewMasuk.visibility=View.VISIBLE
                        btnNewPulang.visibility=View.VISIBLE
                        tvNewMasuk.visibility=View.GONE
                        tvNewPulang.visibility=View.GONE
                    }
                    if(btnNewMasuk.isEnabled==false){

                    }
                }else{
                    btnNewMasuk.isEnabled=true
                    btnNewPulang.isEnabled=true
                    btnNewMasuk.visibility=View.VISIBLE
                    btnNewPulang.visibility=View.GONE
                }
            }else {
                loadAbsen()
            }
        }catch (e:Exception){
            Log.d("CurrentLocation","5 ${e.message}")
        }

    }

    private fun loadAbsenTigaHari() {
        val apiEndPoint = ApiClient.getClient(this@HomeActivity)?.create(ApiEndPoint::class.java)
        GlobalScope.launch(Dispatchers.Main)
        {
            val call = apiEndPoint?.absenTigaHari(NIK!!)
            if(call!=null){
                if(call.isSuccessful){
                    val res = call.body()
                    if(res!==null){
                        if(res.absenTigaHari!=null){
                            absenList?.addAll(res.absenTigaHari!!)
                            adapter?.notifyDataSetChanged()
                        }else{
                            loadAbsenTigaHari()
                        }
                        Log.d("DATATIGAHARI", res.toString())
                    }else{
                        loadAbsenTigaHari()
                    }
                }else{
                    loadAbsenTigaHari()
                }
            }else{
                loadAbsenTigaHari()
            }

        }
    }
    fun showDialogLupaMasuk(){
        MobileAds.initialize(this) {}

        viewPassword = LayoutInflater.from(this@HomeActivity).inflate(R.layout.lupa_masuk, null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tglLupaAbsenMasuk.setOnClickListener{
            showDialogTgl(viewPassword.tglLupaAbsenMasuk)
        }
        viewPassword.btnLupaAbsenMasuk.setOnClickListener{
            if(viewPassword.tglLupaAbsenMasuk.text!!.isNotEmpty()){
                val intent= Intent(this, LupaMasukActivity::class.java)
                intent.putExtra(
                    LupaMasukActivity.TGL_LUPA_ABSEN,
                    viewPassword.tglLupaAbsenMasuk.text.toString()
                )
                startActivity(intent)
                alertDialog.dismiss()
            }else{
                viewPassword.tilTanggalLupaMasuk.error="Tidak Boleh Kosong"
            }
        }

        viewPassword.btnBatalLupaAbsenMasuk.setOnClickListener{
            alertDialog.dismiss()
        }
        alertDialog = AlertDialog.Builder(this@HomeActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }
    fun showDialogLupaPulang(){
        viewPassword = LayoutInflater.from(this@HomeActivity).inflate(R.layout.lupa_pulang, null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tglLupaAbsenPulang.setOnClickListener{
            showDialogTgl(viewPassword.tglLupaAbsenPulang)
        }
        viewPassword.btnLupaAbsenPulang.setOnClickListener{
            if(viewPassword.tglLupaAbsenPulang.text!!.isNotEmpty()){
                val intent= Intent(this, LupaPulangActivity::class.java)
                intent.putExtra(
                    LupaPulangActivity.TGL_LUPA_ABSEN,
                    viewPassword.tglLupaAbsenPulang.text.toString()
                )
                startActivity(intent)
                alertDialog.dismiss()
            }else{
                viewPassword.tilTanggalLupaPulang.error="Tidak Boleh Kosong"
            }
        }

        viewPassword.btnBatalLupaAbsenPulang.setOnClickListener{
            alertDialog.dismiss()
        }
        alertDialog = AlertDialog.Builder(this@HomeActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }
    fun showDialogTgl(inTgl: TextInputEditText){
        val now = Calendar.getInstance()
        val datePicker  = DatePickerDialog.OnDateSetListener{ view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            now.set(Calendar.YEAR, year)
            now.set(Calendar.MONTH, month)
            now.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            inTgl.setText(SimpleDateFormat("dd MMMM yyyy").format(now.time))
        }

        DatePickerDialog(
            this,
            datePicker,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    override fun onDestroy() {
        var c = this@HomeActivity
        var jvClass = LocationService::class.java
        if(ConfigUtil.isMyServiceRunning(jvClass, c)){
            LocalBroadcastManager.getInstance(c).unregisterReceiver(tokenPassingReceiver!!)
            var intent = Intent(c, jvClass).apply {
                this.action = Constants.SERVICE_STOP
            }
            c.stopService(intent)
            Log.d("ServiceName", "${jvClass} Stop")
        }
        super.onDestroy()
    }
    companion object{
        var TIPE = "TIPE"
        var PENGGUNA = "PENGGUNA"
        var KARYAWAN = "KARYAWAN"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var SHOW_ABSEN="SHOW_ABSEN"
        var PERUSAHAAN="PERUSAHAAN"
        var PERSENTASE = "PERSENTASE"
        var LAT = -0.5
        var LNG = 117.0
        var TANGGAL = "Selasa, 30 Maret 2021"
        var state =0
    }
    private fun reciever() {
        tokenPassingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    if (bundle.containsKey("fgLocation")) {
                        val tokenData = bundle.getString("fgLocation")
                        if(tokenData=="fgDone"){
//                            LocalBroadcastManager.getInstance(this@HomeActivity).unregisterReceiver(tokenPassingReceiver!!)
                            Log.d("ServiceName", tokenData)
                        }
                        if(tokenData=="fgUpdate"){
//                            LocalBroadcastManager.getInstance(this@HomeActivity).unregisterReceiver(tokenPassingReceiver!!)
                            Log.d("ServiceName", tokenData)
                        }
                    }
                }
            }
        }
    }
    private fun loadFragment(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        var mapArea = MapAreaDataSource(this@HomeActivity)
        var abpMarker:Marker?=null
        val polylineOptions = PolygonOptions()
        mapAbp?.clear()
        val me = resources.getDrawable(R.drawable.ic_baseline_my_location_24)
        val dr = resources.getDrawable(R.drawable.abp_marker)
        val bitmap = dr.toBitmap(100, 100)
        mMap = googleMap
        if(state<=0){
            abpMarker?.remove()
            abpMarker = mMap?.addMarker(
                MarkerOptions().position(abpLocation!!).title("PT Alamjaya Bara Pratama").icon(
                    BitmapDescriptorFactory.fromBitmap(
                        bitmap
                    )
                )
            )
            var cameraUpdate = CameraUpdateFactory.newLatLngZoom(abpLocation, 20f)
            mMap?.animateCamera(cameraUpdate)
        }
        adPolygon(mapArea,polylineOptions)

        var locationReciever = object : BroadcastReceiver() {
            var z =0
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    if (bundle.containsKey("fgLat")) {
                        val tokenData = bundle.getString("fgLat")
                        Log.d("CurrentLocation", "Lat : $tokenData")
                        LAT = tokenData!!.toDouble()
                    }
                    if (bundle.containsKey("fgLng")) {
                        val tokenData = bundle.getString("fgLng")
                        Log.d("CurrentLocation", "Lng : $tokenData")
                        LNG = tokenData!!.toDouble()
                    }
                    if (bundle.containsKey("fgMock")) {
                        val tokenData = bundle.getString("fgMock")
                        if(tokenData=="true"){
                            var c = this@HomeActivity
                            ConfigUtil.showLoading(
                                c, c, "Fake Gps atau Lokasi Palsu",
                                "Maaf, " +
                                        "Perangkat Anda Terdeteksi Aplikasi Fake Gps atau sejenisnya, " +
                                        "Anda Dilarang menggunakan Aplikasi tersebut, " +
                                        "Jika Masih Menggunakan Aplikasi Tersebut, " +
                                        "Maka Anda Tidak Bisa Menggunakan Aplikasi Ini Selamanya!!!",
                                "text"
                            )
                        }else if(tokenData=="false"){
                            state=1
                            userLocation =LatLng(LAT, LNG)
//                            loadFragment()
                        }
                        Log.d("CurrentLocation", "Gps Mock : $tokenData")
                        if(state>0){
                            liveLocation?.remove()
                            liveLocation = mMap?.addMarker(
                                MarkerOptions().position(userLocation!!).title(NAMA).icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        me.toBitmap(
                                            80,
                                            80
                                        )
                                    )
                                )
                            )
                            abpMarker?.remove()
                            abpMarker = mMap?.addMarker(
                                MarkerOptions().position(abpLocation!!).title("PT Alamjaya Bara Pratama").icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        bitmap
                                    )
                                )
                            )
                            liveLocation?.showInfoWindow()
                            var cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 20f)
                            mMap?.animateCamera(cameraUpdate)

                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(this@HomeActivity).registerReceiver(
            locationReciever!!, IntentFilter(
                Constants.APP_ID
            )
        )



    }
    private fun adPolygon(mapArea:MapAreaDataSource,polylineOptions:PolygonOptions){
        var polyline:Polygon? = null
        try {
            mapAbp?.clear()
            if(mapAbp!!.size<=0){
                modelCompany = mapArea.getMaps(PERUSAHAAN)
                modelCompany?.forEach {
                    var latLng = LatLng(it.lat!!, it.lng!!)
                    polylineOptions.add(latLng)
                    mapAbp?.add(latLng)
                    z++
                }
                if(z>0){
                    polyline?.remove()
                    polyline = mMap?.addPolygon(polylineOptions)
                    polyline!!.strokeColor = Color.argb(100, 40, 123, 250)
                    polyline.fillColor= Color.argb(40, 40, 123, 250)
                    var isInside = PolyUtil.containsLocation(userLocation, mapAbp!!, true)
                    if(isInside){
                        Log.d("CurrentLocation", "IsInside ${isInside}")
                        localAbsen()
                    }else{
                        localAbsen()
                        Log.d("CurrentLocation", "IsInside ${isInside}")
//                disableAbsen()
                    }
                    z=0
                }else{
                    localAbsen()
                    Log.d("CurrentLocation", "IsInside ${z}")
                    z=0
//            disableAbsen()
                }
            }else{
                mapAbp?.clear()
            }

        }catch (e: SQLException){
            Log.d("CurrentLocation", "${e.message}")
        }
    }
    fun lupaAbsen(c:Context){
        var item = arrayOf("Lupa Absen Masuk","Lupa Absen Pulang")
        val alertDialog = AlertDialog.Builder(c)
        alertDialog.setTitle("Silahkan Pilih")
        alertDialog!!.setItems(item, { dialog, which ->
            when (which) {
                0 ->showDialogLupaMasuk()
                1 ->showDialogLupaPulang()
            }
        })
        alertDialog.create()
        alertDialog.show()
    }
    private fun disableAbsen(){
        borderMiddle.visibility = View.GONE
        btnNewMasuk.isEnabled=false
        btnNewPulang.isEnabled=false
        btnNewMasuk.visibility=View.GONE
        btnNewPulang.visibility=View.GONE
        tvNewMasuk.visibility=View.GONE
        tvNewPulang.visibility=View.GONE
    }
}