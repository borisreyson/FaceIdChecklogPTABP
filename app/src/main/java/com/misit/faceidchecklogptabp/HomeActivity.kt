package com.misit.faceidchecklogptabp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Absen.v1.DaftarWajahActivity
import com.misit.faceidchecklogptabp.Response.Absen.DirInfoResponse
import com.misit.faceidchecklogptabp.Response.AbsenLastResponse
import com.misit.faceidchecklogptabp.Response.LastAbsenResponse
import com.misit.faceidchecklogptabp.Response.MainResponse.FirstLoadResponse
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import kotlinx.android.synthetic.main.activity_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class HomeActivity : AppCompatActivity(),View.OnClickListener, LocationListener {
    lateinit var handler : Handler
    lateinit var tm : TelephonyManager
    private var IMEI :String?=null
    private var csrf_token : String?=""
    private var jamSekarang : Int=0
    private var menitSekarang : Int=0
    private var detikSekarang : Int=0
    var tipe :String?=null
    private var app_version : String?=""
    private var mLocationManager : LocationManager?=null
    private var mLocation : Location?= null
    lateinit var viewPassword: View
    lateinit var alertDialog: AlertDialog
    private var android_token : String?=null

    lateinit var mAdView : AdView
    private val updateClock = object :Runnable{
        override fun run() {
            doJob()
            handler.postDelayed(this,1000)
        }

    }
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        androidToken()
        var intPerm :Int= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if(intPerm== PackageManager.PERMISSION_GRANTED){
            tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEI = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)

            } else {
                IMEI = tm.getDeviceId()
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),123)
        }
        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            NAMA = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP,"")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
            SHOW_ABSEN = PrefsUtil.getInstance().getStringState(PrefsUtil.SHOW_ABSEN,"")
        }
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
    }

    override fun onClick(v: View?) {
    }

    override fun onLocationChanged(location: Location?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    //    androidToken
    fun androidToken(){
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@HomeActivity,"Error : $task.exception", Toast.LENGTH_SHORT).show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result
            })
    }
    //    androidToken
    override fun onResume() {
        cekLokasi()
        handler.post(updateClock)

        super.onResume()
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

        val apiEndPoint = ApiClient.getClient(this@HomeActivity)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getAndroidToken(NIK,"faceId",android_token)
        call?.enqueue(object : Callback<FirstLoadResponse?> {
            override fun onFailure(call: Call<FirstLoadResponse?>, t: Throwable) {
//                koneksiInActive()
                cekLokasi()
                Log.d("ErrorLokasi",t.toString())
            }

            override fun onResponse(call: Call<FirstLoadResponse?>, response: Response<FirstLoadResponse?>) {
                var koneksiCek = response.body()
                Log.v("CekData",koneksiCek.toString())
                if(koneksiCek!=null){
                    if(koneksiCek.jam!=null)
                        jamSekarang = koneksiCek.jam!!.toInt()
                        menitSekarang = koneksiCek.menit!!.toInt()
                        detikSekarang = koneksiCek.detik!!.toInt()
                        TANGGAL = "${koneksiCek.hari}, ${koneksiCek.tanggal}"
//                        doJob()

                    cekFaceID()
//                    Log.d("JAMNYA", "${koneksiCek.jam}")
                }else{
                    koneksiInActive()
                }
            }
        })

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
    fun cekFaceID(){
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getDirInfo(IndexActivity.NIK!!)
        call?.enqueue(object : Callback<DirInfoResponse?> {
            override fun onFailure(call: Call<DirInfoResponse?>, t: Throwable) {
                cekFaceID()
            }
            override fun onResponse(
                call: Call<DirInfoResponse?>,
                response: Response<DirInfoResponse?>
            ) {
                val faceRes = response.body()
                if(faceRes!=null){
                    if(faceRes.folder!!){
//                        btnFaceFalse.visibility=View.GONE
//                        btnFaceTrue.visibility=View.VISIBLE
                        PopupUtil.dismissDialog()
                        loadAbsen()
//                        btnDaftarWajah.visibility=View.GONE
                    }else{
//                        btnFaceFalse.visibility=View.VISIBLE
//                        btnFaceTrue.visibility=View.GONE
                        PopupUtil.dismissDialog()
                        val intent = Intent(this@HomeActivity, DaftarWajahActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
        PopupUtil.dismissDialog()
    }
    fun loadAbsen(){
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.lastAbsen(NIK!!)
        call?.enqueue(object : Callback<AbsenLastResponse?> {
            override fun onFailure(call: Call<AbsenLastResponse?>, t: Throwable) {

            }
            override fun onResponse(
                call: Call<AbsenLastResponse?>,
                response: Response<AbsenLastResponse?>
            ) {

                val lastRes = response.body()
                if(lastRes!=null){
                    if(lastRes?.lastNew!=null){
                        if(lastRes.lastNew=="Masuk"){
                            btnNewPulang.isEnabled=true
                            btnNewMasuk.isEnabled=true
                            btnNewMasuk.visibility=View.GONE
                            btnNewPulang.visibility=View.VISIBLE
                            tvNewMasuk.visibility=View.VISIBLE
                            if(lastRes.presensiMasuk!=null){
                                tvNewMasuk.text = lastRes.presensiMasuk!!.jam.toString()
                            }
                            tvNewPulang.visibility=View.GONE
                        }else
                            if(lastRes.lastNew=="Pulang"){
//                                chkMasuk.isChecked=true
//                                chkPulang.isChecked=true
                                btnNewPulang.isEnabled=true
                                btnNewMasuk.isEnabled=true
                                btnNewMasuk.visibility=View.VISIBLE
                                btnNewPulang.visibility=View.GONE
                                tvNewMasuk.visibility=View.GONE
                                tvNewPulang.visibility=View.VISIBLE
                                if(lastRes.presensiMasuk!=null){
                                    tvNewMasuk.text = lastRes.presensiMasuk!!.jam.toString()
                                }
                                if(lastRes.presensiPulang!=null){
                                    tvNewPulang.text = lastRes.presensiPulang!!.jam.toString()
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
//                            Toasty.info(this@IndexActivity,"Anda Sudah Melakukan Absen Masuk Dan Pulang!",Toasty.LENGTH_SHORT).show()
                        }
                    }else{
//                        chkMasuk.isChecked=false
//                        chkPulang.isChecked=false
                        btnNewMasuk.isEnabled=true
                        btnNewPulang.isEnabled=true
                        btnNewMasuk.visibility=View.VISIBLE
                        btnNewPulang.visibility=View.GONE
                    }
                }
            }

        })
    }
    companion object{
        var TIPE = "TIPE"
        var PENGGUNA = "PENGGUNA"
        var KARYAWAN = "KARYAWAN"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var SHOW_ABSEN="SHOW_ABSEN"
        var PERSENTASE = "PERSENTASE"
        var LAT = 0.0
        var LNG = 0.0
        var TANGGAL = "Selasa, 30 Maret 2021"
    }
}