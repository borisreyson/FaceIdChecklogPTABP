package com.misit.faceidchecklogptabp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Absen.v1.*
import com.misit.faceidchecklogptabp.Adapter.Last3DaysAdapter
import com.misit.faceidchecklogptabp.Response.Absen.DirInfoResponse
import com.misit.faceidchecklogptabp.Response.AbsenLastResponse
import com.misit.faceidchecklogptabp.Response.AbsenTigaHariItem
import com.misit.faceidchecklogptabp.Response.MainResponse.FirstLoadResponse
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.tvJam
import kotlinx.android.synthetic.main.activity_home.tvNama
import kotlinx.android.synthetic.main.activity_home.tvNik
import kotlinx.android.synthetic.main.lupa_masuk.view.*
import kotlinx.android.synthetic.main.lupa_pulang.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity(),View.OnClickListener {
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
    private var adapter: Last3DaysAdapter? = null
    private var absenList: MutableList<AbsenTigaHariItem>? = null
    lateinit var rvLast3Day :RecyclerView
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
        tvJam.text =""
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
        absenList = ArrayList()
        adapter = Last3DaysAdapter(this@HomeActivity,absenList!!,NIK,NAMA)
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
    }
    fun listAbsen(){
        val intent= Intent(this, ListAbsenActivity::class.java)
        intent.putExtra(ListAbsenActivity.NIK, NIK)

        startActivity(intent)
    }
    //    androidToken
    fun androidToken(){
        try {

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
        }catch (e:Exception){
            Log.e("HomeError","Cek Token :${e.message}")
            androidToken()
        }catch (e:InterruptedException){
            Log.e("HomeError","Cek Token :${e.message}")
            androidToken()
        }
    }
    //    androidToken
    override fun onResume() {
        absenList?.clear()
        loadAbsenTigaHari()
        cekLokasi()
        handler.post(updateClock)
        if(SHOW_ABSEN =="1"){
            btnListAllAbsen.visibility=View.VISIBLE
        }else{
            btnListAllAbsen.visibility=View.GONE
        }
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
        try {
            tvJam.text =""
            val apiEndPoint = ApiClient.getClient(this@HomeActivity)?.create(ApiEndPoint::class.java)
            GlobalScope.launch {
                val call = apiEndPoint?.tokenCorutine(NIK,"faceId",android_token)
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
            }
        }catch (e:Exception){
            Log.e("HomeError","Cek Lokasi :${e.message}")
            cekLokasi()
        }catch (e:InterruptedException){
            Log.e("HomeError","Cek Lokasi :${e.message}")
            cekLokasi()
        }
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
        try {
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
                                    loadAbsen()
//                        btnDaftarWajah.visibility=View.GONE
                                }else{
//                        btnFaceFalse.visibility=View.VISIBLE
//                        btnFaceTrue.visibility=View.GONE
                                    PopupUtil.dismissDialog()
                                    val intent = Intent(this@HomeActivity, DaftarWajahActivity::class.java)
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
        }catch (e:Exception){
            cekFaceID()
            Log.e("HomeError","Cek cekFaceID :${e.message}")
        }catch (e:InterruptedException){
            cekFaceID()
            Log.e("HomeError","Cek cekFaceID :${e.message}")
        }

    }
    fun loadAbsen(){
        try {
            val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
            GlobalScope.launch(Dispatchers.Main){
                val call = apiEndPoint?.lastAbsenCorutine(NIK!!)
                if(call!=null){
                    if(call.isSuccessful){
                        val response= call.body()
                        if(response!=null){
                            if(response.lastNew!=null){
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
        }catch (e:Exception){
            Log.e("HomeError","Cek loadAbsen :${e.message}")
            loadAbsen()
        }catch (e:InterruptedException){
            Log.e("HomeError","Cek loadAbsen :${e.message}")
            loadAbsen()
        }

    }

    private fun loadAbsenTigaHari() {
        try {
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
                            Log.d("DATATIGAHARI",res.toString())
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
        }catch (e:Exception){
            Log.e("HomeError","Cek loadAbsenTigaHari :${e.message}")
            loadAbsenTigaHari()
        }catch (e:InterruptedException){
            Log.e("HomeError","Cek loadAbsenTigaHari :${e.message}")
            loadAbsenTigaHari()
        }
    }
    fun showDialogLupaMasuk(){
        MobileAds.initialize(this) {}

        viewPassword = LayoutInflater.from(this@HomeActivity).inflate(R.layout.lupa_masuk,null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tglLupaAbsenMasuk.setOnClickListener{
            showDialogTgl(viewPassword.tglLupaAbsenMasuk)
        }
        viewPassword.btnLupaAbsenMasuk.setOnClickListener{
            if(viewPassword.tglLupaAbsenMasuk.text!!.isNotEmpty()){
                val intent= Intent(this, LupaMasukActivity::class.java)
                intent.putExtra(LupaMasukActivity.TGL_LUPA_ABSEN, viewPassword.tglLupaAbsenMasuk.text.toString())
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
        viewPassword = LayoutInflater.from(this@HomeActivity).inflate(R.layout.lupa_pulang,null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tglLupaAbsenPulang.setOnClickListener{
            showDialogTgl(viewPassword.tglLupaAbsenPulang)
        }
        viewPassword.btnLupaAbsenPulang.setOnClickListener{
            if(viewPassword.tglLupaAbsenPulang.text!!.isNotEmpty()){
                val intent= Intent(this, LupaPulangActivity::class.java)
                intent.putExtra(LupaPulangActivity.TGL_LUPA_ABSEN, viewPassword.tglLupaAbsenPulang.text.toString())
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
        val datePicker  = DatePickerDialog.OnDateSetListener{
                view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            now.set(Calendar.YEAR,year)
            now.set(Calendar.MONTH,month)
            now.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            inTgl.setText(SimpleDateFormat("dd MMMM yyyy").format(now.time))
        }

        DatePickerDialog(this,
            datePicker,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
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