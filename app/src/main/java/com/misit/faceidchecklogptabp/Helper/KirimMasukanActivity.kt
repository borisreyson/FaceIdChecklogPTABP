package com.misit.faceidchecklogptabp.Helper

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Response.CsrfTokenResponse
import com.misit.faceidchecklogptabp.Response.LoginResponse
import com.misit.faceidchecklogptabp.Response.MasukanResponse
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_kirim_masukan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class KirimMasukanActivity : AppCompatActivity(),View.OnClickListener {

    private var csrf_token : String?=""
    lateinit var mAdView : AdView
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kirim_masukan)
        title="Kirim Masukan"
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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,true)){
            NAMA = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP,"")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
            SHOW_ABSEN = PrefsUtil.getInstance().getStringState(PrefsUtil.SHOW_ABSEN,"")
        }
        tvNama.text= NAMA
        tvNik.text= NIK
        btnKirimMasukan.setOnClickListener(this)
        btnBeriBintang.setOnClickListener(this)
    }

    override fun onResume() {
        getToken()
        super.onResume()
    }
    companion object{
        var TIPE = "TIPE"
        var PENGGUNA = "PENGGUNA"
        var KARYAWAN = "KARYAWAN"
        var NIK = "NIK"
        var NAMA = "NAMA"
        var SHOW_ABSEN="SHOW_ABSEN"
        var PERSENTASE = "PERSENTASE"
    }

    override fun onClick(v: View?) {
        if(v!!.id==R.id.btnKirimMasukan){
            if(inMasukan.text!!.isNotEmpty()){
                kirim_masukan(inMasukan.text.toString())
            }else{
                tilMasukan.error="Masukan Tidak Boleh Kosong"
                inMasukan.requestFocus()
            }
        }else
        if(v!!.id==R.id.btnBeriBintang){
            if(inMasukan.text!!.isNotEmpty()) {
                kirim_masukan(inMasukan.text.toString())
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()))
                startActivity(intent)
            }else{
                tilMasukan.error="Masukan Tidak Boleh Kosong"
                inMasukan.requestFocus()
            }
        }
    }

    private fun kirim_masukan(inMasuk:String) {
        PopupUtil.showLoading(this@KirimMasukanActivity,"Logging In","Please Wait")
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.kirimMasukan(NIK, NAMA,inMasuk,csrf_token)
        call?.enqueue(object : Callback<MasukanResponse?> {
            override fun onFailure(call: Call<MasukanResponse?>, t: Throwable) {
                kirim_masukan(inMasukan.text.toString())
            }

            override fun onResponse(
                call: Call<MasukanResponse?>,
                response: Response<MasukanResponse?>
            ) {
                var respon = response.body()
                if(respon!=null){
                    if(respon?.success!!){
                        PopupUtil.dismissDialog()
                        finish()
                    }else{
                        kirim_masukan(inMasukan.text.toString())
                    }
                }else{
                    kirim_masukan(inMasukan.text.toString())
                }
            }

        })
    }

    private fun getToken() {
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.getToken("csrf_token")
        call?.enqueue(object : Callback<CsrfTokenResponse> {
            override fun onFailure(call: Call<CsrfTokenResponse>, t: Throwable) {
                Toast.makeText(this@KirimMasukanActivity,"Error : $t", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(
                call: Call<CsrfTokenResponse>,
                response: Response<CsrfTokenResponse>
            ) {
                csrf_token = response.body()?.csrfToken
            }
        })
    }
}
