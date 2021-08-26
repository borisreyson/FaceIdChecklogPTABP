package com.misit.faceidchecklogptabp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.textfield.TextInputEditText
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Absen.*
import com.misit.faceidchecklogptabp.Absen.v1.*
import com.misit.faceidchecklogptabp.Chart.PersentaseActivity
import com.misit.faceidchecklogptabp.Helper.KirimMasukanActivity
import com.misit.faceidchecklogptabp.Masukan.MasukanActivity
import com.misit.faceidchecklogptabp.Response.*
import com.misit.faceidchecklogptabp.Response.Absen.DirInfoResponse
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_index.*
import kotlinx.android.synthetic.main.change_password.view.*
import kotlinx.android.synthetic.main.info_app.view.*
import kotlinx.android.synthetic.main.informasi.view.*
import kotlinx.android.synthetic.main.lupa_masuk.view.*
import kotlinx.android.synthetic.main.lupa_pulang.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class IndexActivity : AppCompatActivity(),View.OnClickListener, LocationListener {

    lateinit var handler :Handler
    private var r :Runnable?=null
    lateinit var tm :TelephonyManager
    private var IMEI :String?=null
    private var time: Date?=null
    private var csrf_token : String?=""
    private var jamSekarang : Int=0
    private var menitSekarang : Int=0
    private var detikSekarang : Int=0
    var tipe :String?=null
    private var app_version : String?=""
    private var mLocation : Location?= null
    lateinit var viewPassword: View
    lateinit var alertDialog: AlertDialog

    lateinit var mAdView : AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        var intPerm :Int= ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
        if(intPerm==PackageManager.PERMISSION_GRANTED){
            tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEI = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)

            } else {
                IMEI = tm.getDeviceId()
            }
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),123)
        }
        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,true)){
            NAMA = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP,"")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
            SHOW_ABSEN = PrefsUtil.getInstance().getStringState(PrefsUtil.SHOW_ABSEN,"")
        }
        tipe = intent.getStringExtra(TIPE)
        if(tipe=="terlambat"){
            listAbsen()
        }
        tvNama.text= NAMA
        tvNik.text=NIK
        handler= Handler()
        MobileAds.initialize(this) {}
        showInformasi()
        btn_masuk.setOnClickListener(this)
        btnListAbsen.setOnClickListener(this)
        btn_pulang.setOnClickListener(this)
        btnLihatAbsen.setOnClickListener(this)
        logOut.setOnClickListener(this)
        btnSandi.setOnClickListener(this)
        btnInfoApp.setOnClickListener(this)
        btnChart.setOnClickListener(this)
        btnLupaMasuk.setOnClickListener(this)
        btnLupaPulang.setOnClickListener(this)
        btnMasukan.setOnClickListener(this)
        btnDaftarWajah.setOnClickListener(this)
        btnMasukanList.setOnClickListener(this)

    }

    fun cekFaceID(){
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getDirInfo(NIK!!)
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
                        btnFaceFalse.visibility=View.GONE
                        btnFaceTrue.visibility=View.VISIBLE
                        PopupUtil.dismissDialog()
                        loadAbsen()
                        btnDaftarWajah.visibility=View.GONE
                    }else{
                        btnFaceFalse.visibility=View.VISIBLE
                        btnFaceTrue.visibility=View.GONE
                        PopupUtil.dismissDialog()
                        val intent = Intent(this@IndexActivity, DaftarWajahActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
        PopupUtil.dismissDialog()
    }
    fun listAbsen(){
        val intent= Intent(this, ListAbsenActivity::class.java)
        intent.putExtra(ListAbsenActivity.NIK, NIK)

        startActivity(intent)
    }
    override fun onResume() {
        cekLokasi()

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adViewIndex)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                Log.d("errorCode",errorCode.toString())
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

//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        PopupUtil.showProgress(this,"Loading","Update Data Dari Server!!")
        cekFaceID()

        if(SHOW_ABSEN=="1"){
            presentasiPengguna()
            btnListAbsen.visibility=View.VISIBLE
            btnChart.visibility=View.VISIBLE
            btnMasukanList.visibility=View.VISIBLE
        }else{
            btnChart.visibility=View.GONE
            btnListAbsen.visibility=View.GONE
            btnMasukanList.visibility=View.GONE
        }
        super.onResume()
    }

    override fun onPause() {
        handler.removeCallbacks(r)
        super.onPause()
    }
    fun loadAbsen(){
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.lastAbsen(NIK!!)
        call?.enqueue(object : Callback<AbsenLastResponse?> {
            override fun onFailure(call: Call<AbsenLastResponse?>, t: Throwable) {
                loadAbsen()
            }
            override fun onResponse(
                call: Call<AbsenLastResponse?>,
                response: Response<AbsenLastResponse?>
            ) {

                val lastRes = response.body()
                if(lastRes!=null){
                    if(lastRes?.lastNew!=null){
//                        Toasty.info(this@IndexActivity,lastRes?.lastAbsen).show()
                        if(lastRes.lastNew=="Masuk"){
                            chkMasuk.isChecked=true
                            chkPulang.isChecked=false
                            btn_pulang.isEnabled=true
                            btn_masuk.isEnabled=true
                            btn_masuk.visibility=View.GONE
                            btn_pulang.visibility=View.VISIBLE
                        }else
                        if(lastRes.lastNew=="Pulang"){
                            chkMasuk.isChecked=true
                            chkPulang.isChecked=true
                            btn_pulang.isEnabled=true
                            btn_masuk.isEnabled=true
                            btn_masuk.visibility=View.VISIBLE
                            btn_pulang.visibility=View.GONE
                        }else{
                            chkMasuk.isChecked=false
                            chkPulang.isChecked=false
                            btn_masuk.isEnabled=true
                            btn_pulang.isEnabled=true
                            btn_masuk.visibility=View.VISIBLE
                            btn_pulang.visibility=View.GONE
                        }
                        if(btn_masuk.isEnabled==false){
//                            Toasty.info(this@IndexActivity,"Anda Sudah Melakukan Absen Masuk Dan Pulang!",Toasty.LENGTH_SHORT).show()
                        }
                    }else{
                        chkMasuk.isChecked=false
                        chkPulang.isChecked=false
                        btn_masuk.isEnabled=true
                        btn_pulang.isEnabled=true
                        btn_masuk.visibility=View.VISIBLE
                        btn_pulang.visibility=View.GONE
                    }
                }
            }

        })
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
    private fun showInformasi() {
        MobileAds.initialize(this) {}
        viewPassword = LayoutInflater.from(this@IndexActivity).inflate(R.layout.informasi,null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAdView = viewPassword.adViewAPPINFORMASI
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        viewPassword.btnInformasiOK.setOnClickListener{
            alertDialog.dismiss()
        }
        alertDialog = AlertDialog.Builder(this@IndexActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
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
                    tvJam.text = "Pukul : 0" + "${jamSekarang} : 0" + "${menitSekarang} : 0" + "${detikSekarang}"
                }else{
                    tvJam.text = "Pukul : 0" + "${jamSekarang} : 0" + "${menitSekarang} : ${detikSekarang}"
                }
            }else{
                if(detikSekarang<10){
                    tvJam.text = "Pukul : 0" + "${jamSekarang} : ${menitSekarang} : 0" + "${detikSekarang}"
                }else{
                    tvJam.text = "Pukul : 0" + "${jamSekarang} : ${menitSekarang} : ${detikSekarang}"
                }
            }
        }else{
            if(menitSekarang<10){
                if(detikSekarang<10){
                    tvJam.text = "Pukul : ${jamSekarang} : 0" + "${menitSekarang} : 0" + "${detikSekarang}"
                }else{
                    tvJam.text = "Pukul : ${jamSekarang} : 0" + "${menitSekarang} : ${detikSekarang}"
                }
            }else{
                if(detikSekarang<10){
                    tvJam.text = "Pukul : ${jamSekarang} : ${menitSekarang} : 0" + "${detikSekarang}"
                }else{
                    tvJam.text =  "Pukul : ${jamSekarang} : ${menitSekarang} : ${detikSekarang}"
                }
            }
        }
        handler.postDelayed(r,1000)
    }
    fun cekLokasi(){
        handler.removeCallbacks(r)
        r = Runnable{
            cekLokasi()
        }
        val apiEndPoint = ApiClient.getClient(this@IndexActivity)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.cekLokasi()
        call?.enqueue(object : Callback<AbpResponse?> {
            override fun onFailure(call: Call<AbpResponse?>, t: Throwable) {
//                koneksiInActive()

                cekLokasi()
                Log.d("ErrorLokasi",t.toString())
            }

            override fun onResponse(call: Call<AbpResponse?>, response: Response<AbpResponse?>) {
                var koneksiCek = response.body()
                if(koneksiCek!=null){
                    if(koneksiCek.jam!=null)
                        jamSekarang = koneksiCek.jam!!.toInt()
                        menitSekarang = koneksiCek.menit!!.toInt()
                        detikSekarang = koneksiCek.detik!!.toInt()
                        doJob()
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
    fun showDialogOption(nik:String,nama: String){
        viewPassword = LayoutInflater.from(this@IndexActivity).inflate(R.layout.change_password,null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tvCHname.text="Nama: ${nama}"
        viewPassword.tvCHnik.text="NIK : ${nik}"
        viewPassword.btnUpdateSandi.setOnClickListener {
            val oldPass = viewPassword.oldInChPassword.text
            val newPass = viewPassword.InChPassword.text
            updatePassword(nik,oldPass.toString(),newPass.toString())
        }
        alertDialog = AlertDialog.Builder(this@IndexActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }
    private fun getToken() {
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getToken("csrf_token")
        call?.enqueue(object : Callback<CsrfTokenResponse> {
            override fun onFailure(call: Call<CsrfTokenResponse>, t: Throwable) {
                Toast.makeText(this@IndexActivity,"Error : $t", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(
                call: Call<CsrfTokenResponse>,
                response: Response<CsrfTokenResponse>
            ) {
                csrf_token = response.body()?.csrfToken
            }
        })
    }
    fun updatePassword(nik:String,oldPass:String,newPass:String){
        PopupUtil.showLoading(this@IndexActivity,"Update Password","Please Wait")
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.updatePassword(nik,oldPass,newPass,csrf_token)
        call?.enqueue(object : Callback<LoginResponse?> {
            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                Toasty.error(this@IndexActivity,"Error While Update...",Toasty.LENGTH_SHORT).show()
                PopupUtil.dismissDialog()
            }
            override fun onResponse(
                call: Call<LoginResponse?>,
                response: Response<LoginResponse?>
            ) {
                var usrResponse =response.body()
                if (usrResponse!=null) {
                    if (usrResponse.success!!) {
                        Toasty.success(this@IndexActivity,"Update Password Success...",Toasty.LENGTH_SHORT).show()
                    }else{
                        Toasty.error(this@IndexActivity,"Update Password Failed...",Toasty.LENGTH_SHORT).show()
                    }
                }else{
                    Toasty.error(this@IndexActivity,"Update Password Failed...",Toasty.LENGTH_SHORT).show()
                }
                PopupUtil.dismissDialog()
                alertDialog.dismiss()
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
    }

    override fun onClick(v: View?) {
        if(v!!.id==R.id.btn_masuk){
                val intent= Intent(this, MasukActivity::class.java)
                intent.putExtra(MasukActivity.NIK, NIK)
                startActivity(intent)
        }
        else if(v!!.id==R.id.btnSandi){
            getToken()
            showDialogOption(NIK, NAMA)
        }
        else if(v!!.id==R.id.logOut){
            logout()
        }
        else if(v!!.id==R.id.btnLihatAbsen){
            val intent= Intent(this, LihatAbsenActivity::class.java)
            intent.putExtra(LihatAbsenActivity.NIK, NIK)
            startActivity(intent)
        }
        else if(v!!.id==R.id.btn_pulang){
            val intent= Intent(this, PulangActivity::class.java)
            intent.putExtra(PulangActivity.NIK, NIK)
            startActivity(intent)
        }
        else if(v!!.id==R.id.btnListAbsen){
            listAbsen()
        }
        else if(v!!.id==R.id.btnInfoApp){
            showDialogInfoApp()
        }
        else if(v!!.id==R.id.btnChart){
            val intent= Intent(this, PersentaseActivity::class.java)
            intent.putExtra(PersentaseActivity.KARYAWAN, KARYAWAN)
            intent.putExtra(PersentaseActivity.PENGGUNA, PENGGUNA)
            intent.putExtra(PersentaseActivity.PERSENTASE, PERSENTASE)
            startActivity(intent)
        }
        else if(v!!.id==R.id.btnLupaMasuk){
            showDialogLupaMasuk()

        }
        else if(v!!.id==R.id.btnLupaPulang){
            val intent= Intent(this, LupaPulangActivity::class.java)
            intent.putExtra(LupaPulangActivity.NIK, NIK)
            startActivity(intent)
            showDialogLupaPulang()
        }else if(v!!.id==R.id.btnMasukan){
            kirimMasukan()
        }else if(v!!.id==R.id.btnDaftarWajah){
            val intent = Intent(this@IndexActivity, DaftarWajahActivity::class.java)
            startActivity(intent)
        }else if(v!!.id==R.id.btnMasukanList){
            val intent = Intent(this@IndexActivity,MasukanActivity::class.java)
            startActivity(intent)
        }
    }

    fun showDialogInfoApp(){
        MobileAds.initialize(this) {}
        versionApp()
        viewPassword = LayoutInflater.from(this@IndexActivity).inflate(R.layout.info_app,null)
        if(viewPassword.parent!=null){
            (viewPassword.parent as ViewGroup).removeView(viewPassword)
        }
        viewPassword.tvVersionCode.text = "Version "+app_version
        if(IMEI!=null){
            viewPassword.phoneIMEI.text = "IMEI :"+IMEI
        }else{
            viewPassword.phoneIMEI.text = "IMEI: MISSING"
        }
        mAdView = viewPassword.adViewAPPINFO
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        alertDialog = AlertDialog.Builder(this@IndexActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    fun showDialogLupaMasuk(){
        MobileAds.initialize(this) {}

        viewPassword = LayoutInflater.from(this@IndexActivity).inflate(R.layout.lupa_masuk,null)
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
        mAdView = viewPassword.adViewLMasuk
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        alertDialog = AlertDialog.Builder(this@IndexActivity)
            .setView(viewPassword).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    fun showDialogLupaPulang(){
        MobileAds.initialize(this) {}


        viewPassword = LayoutInflater.from(this@IndexActivity).inflate(R.layout.lupa_pulang,null)
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
        mAdView = viewPassword.adViewLPulang
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        alertDialog = AlertDialog.Builder(this@IndexActivity)
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
    fun versionApp(){
        Use@ try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Kirim Masukan?")
            .setPositiveButton("Ya",{
                    dialog,
                    which ->
                kirimMasukan()
                finish()
            })
            .setNegativeButton("Tidak",
                {
                        dialog,
                        which ->
                    finish()
                })
            .show()
        //super.onBackPressed()
    }

    private fun kirimMasukan() {
        val intent = Intent(this,KirimMasukanActivity::class.java)
        startActivity(intent)
    }

    fun presentasiPengguna(){
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getPresentasiPengguna()
        call?.enqueue(object : Callback<PresentasiPenggunaResponse?> {
            override fun onFailure(call: Call<PresentasiPenggunaResponse?>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<PresentasiPenggunaResponse?>,
                response: Response<PresentasiPenggunaResponse?>
            ) {
                var  res = response.body()
                if(res!=null){
                    PENGGUNA = res.jumlah_pengguna!!.toString()
                    KARYAWAN = res.jumlah_karyawan!!.toString()
                    PERSENTASE = "%.2f".format(res.persentasi!!.toFloat())
                    btnChart.setText("${PERSENTASE} % Pengguna Aktif")
                    var s = "${res.persentasi} - ${res.jumlah_pengguna} - ${res.jumlah_karyawan}";
//                    Toasty.info(this@IndexActivity,s).show()
                }
            }
        })

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
