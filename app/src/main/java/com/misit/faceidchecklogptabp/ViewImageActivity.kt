package com.misit.faceidchecklogptabp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.misit.faceidchecklogptabp.Absen.v1.AbsenMapActivity
import com.misit.faceidchecklogptabp.Utils.DateUtils
import kotlinx.android.synthetic.main.activity_view_image.*

class ViewImageActivity : AppCompatActivity(),View.OnClickListener {

    lateinit var mAdView : AdView
    lateinit var  mFirebaseAnalytics: FirebaseAnalytics
    private var lat=0.0
    private var lng=0.0
    private var nama=""
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        var gambar = intent.getStringExtra(GAMBAR)
        var nik = intent.getStringExtra(NIK)
        nama = intent.getStringExtra(NAMA).toString()
        var tgl = intent.getStringExtra(TGL)
        var jam = intent.getStringExtra(JAM)
        var status = intent.getStringExtra(STATUS)
        lat = intent.getStringExtra(LAT)!!.toDouble()
        lng = intent.getStringExtra(LNG)!!.toDouble()
        var lupa_absen = intent.getStringExtra(LUPA_ABSEN)
        title = nama
        detNama.text= "($nik) $nama"
        detTGL.text = tgl?.let { DateUtils.fmt(it) }
        detJam.text = jam
        detStatus.text = status
        tvLAT.text= lat.toString();
        tvLNG.text= lng.toString();
        Glide
            .with(this).load(gambar)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(ImgView)
        if(lupa_absen==null || lupa_absen=="LUPA_ABSEN"){
            detLupaAbsen.visibility = View.GONE
        }else{
            detLupaAbsen.text = lupa_absen
            detLupaAbsen.visibility = View.VISIBLE
        }
        if(lat==0.0 && lng==0.0){
            ftMaps.visibility = View.GONE
        }else{
            ftMaps.visibility = View.VISIBLE
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        backPresed.setOnClickListener(this)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        }
        ftMaps.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    companion object{
        var GAMBAR = "GAMBAR"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var TGL = "TGL"
        var JAM = "JAM"
        var STATUS = "STATUS"
        var LAT = "LAT"
        var LNG = "LNG"
        var LUPA_ABSEN = "LUPA_ABSEN"
    }

    override fun onResume() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adViewViewInfo)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onResume()
    }
    override fun onClick(v: View?) {
        if(v!!.id==R.id.ftMaps){
            val intent = Intent(this@ViewImageActivity, AbsenMapActivity::class.java)
            intent.putExtra(AbsenMapActivity.KEY_LAT,lat)
            intent.putExtra(AbsenMapActivity.KEY_LNG,lng)
            intent.putExtra(AbsenMapActivity.NAMA,nama)
            startActivity(intent)
        }
    }
}