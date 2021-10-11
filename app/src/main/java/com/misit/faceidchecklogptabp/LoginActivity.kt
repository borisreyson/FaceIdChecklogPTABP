package com.misit.faceidchecklogptabp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.misit.faceidchecklogptabp.Response.CsrfTokenResponse
import com.misit.faceidchecklogptabp.Response.LoginResponse
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException

class LoginActivity : AppCompatActivity(),View.OnClickListener {

    private var csrf_token : String?=""
    private var android_token : String?=""
    private var app_version : String?=""
    private var IMEI : String?=""
    lateinit var tm : TelephonyManager

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    @SuppressLint("MissingPermission")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var intPerm :Int= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
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
//        Toasty.info(this,IMEI.toString()).show()
        cekLokasi()
        PrefsUtil.initInstance(this)
        loginBtn.setOnClickListener(this)
        InPassword.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //Perform Code

                if(isValidatedAll()) {
                    loginSubmit(
                        InUsername.text.toString().trim(),
                        InPassword.text.toString().trim()
                    )
                }
                return@OnKeyListener true
            }
            false
        })
    }
    override fun onClick(v: View?) {
        if(v?.id==R.id.loginBtn){
            if(isValidatedAll()){
                loginSubmit(InUsername.text.toString().trim(),InPassword.text.toString().trim())
            }

        }
    }

    override fun onResume() {
        cekLokasi()
        androidToken()
        versionApp()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onResume()
    }
    fun cekLokasi(){
        try {
            val apiEndPoint = ApiClient.getClient(this)?.create(ApiEndPoint::class.java)
            GlobalScope.launch {
                val call = apiEndPoint?.cekLokasiCorrutine()
                if(call!=null){
                    if(call.isSuccessful){
                        getToken()
                    }else{
                        cekLokasi()
                    }
                }else{
                    koneksiInActive()
                }
            }
        }catch (e:ConnectException){
            Toasty.error(this@LoginActivity,"${e.message}").show()
        }

    }

    fun androidToken(){
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@LoginActivity,"Error : $task.exception", Toast.LENGTH_SHORT).show()

                    return@OnCompleteListener
                }
                // Get new Instance ID token
                android_token = task.result
            })
    }
    private fun getToken() {
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        GlobalScope.launch {
            val call = apiEndPoint.getTokenCorutine("csrf_token")
            if(call!=null){
                if(call.isSuccessful){
                    if(call.body()!=null){
                        csrf_token = call.body()?.csrfToken
                        Log.d("CsrfToken","$csrf_token")
                    }
                }else{
                    getToken()
                }
            }else{
                getToken()
            }
        }
    }
    fun versionApp(){
        Use@ try {
            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
            app_version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
    fun loginSubmit(userIn:String,passIn:String){
        Log.d("csrf_token","${csrf_token}")
        PopupUtil.showLoading(this@LoginActivity,"Logging In","Please Wait")
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            val call = apiEndPoint.loginCheckloginCorutine(
                userIn,
                passIn,
                csrf_token,
                android_token,
                app_version,
                "faceId",
                IMEI.toString()
            )
            if(call!=null){
                if(call.isSuccessful){
                    val usrResponse = call.body()
                    if (usrResponse!=null){
                        if (usrResponse.success!!){
                            PopupUtil.dismissDialog()
                            PrefsUtil.getInstance()
                                .setBooleanState(PrefsUtil.IS_LOGGED_IN,
                                    true)
                            PrefsUtil.getInstance()
                                .setStringState(PrefsUtil.NIK,
                                    usrResponse.dataLogin?.nik)
                            PrefsUtil.getInstance()
                                .setStringState(PrefsUtil.NAMA_LENGKAP,
                                    usrResponse.dataLogin?.nama)
                            PrefsUtil.getInstance()
                                .setStringState(PrefsUtil.SHOW_ABSEN,
                                    usrResponse.dataLogin?.show_absen)
                            PrefsUtil.getInstance()
                                .setStringState("PERUSAHAAN",
                                    usrResponse.dataLogin?.perusahaan)
                            Log.d("LoginPerusahaan","${usrResponse.dataLogin?.perusahaan}")
                            Toasty.success(this@LoginActivity,"Login Success ",Toasty.LENGTH_LONG).show()
                            val intents = Intent(this@LoginActivity,HomeActivity::class.java)
                            finish()
                            startActivity(intents)
                            PopupUtil.dismissDialog()

                        }else{
                            Toasty.error(this@LoginActivity,"Username Or Password Wrong!",Toasty.LENGTH_SHORT).show()
                            clearForm()
                            InUsername.requestFocus()
                            PopupUtil.dismissDialog()
                        }
                    }else{
                        Toasty.error(this@LoginActivity,"Username Or Password Wrong!",Toasty.LENGTH_SHORT).show()
                        clearForm()
                        InUsername.requestFocus()
                        PopupUtil.dismissDialog()
                    }
                }else{
                    Toasty.error(this@LoginActivity,"Error While Loging...",Toasty.LENGTH_SHORT).show()
                    PopupUtil.dismissDialog()
                }
            }
        }
    }
    private fun clearForm(){
        InUsername.text=null;
        InPassword.text=null;
    }
    private fun isValidatedAll()  :Boolean{

        clearError()
        if(InUsername.text!!.isEmpty()){
            tilUsername.error="Please Input Someting"
            InUsername.requestFocus()
            return false
        }
        if(InPassword.text!!.isEmpty()){
            tilPassword.error="Please Input Someting"
            InPassword.requestFocus()
            return false
        }
        PopupUtil.dismissDialog()
        return true
    }
    fun koneksiInActive(){
        AlertDialog.Builder(this)
            .setTitle("Maaf , Anda Harus Menggunakan Jaringan Wifi PT. ABP!")
            .setPositiveButton("OK, Keluar",{
                    dialog,
                    which ->
                finish()
            })
            .setOnDismissListener({dialog -> finish()  })
            .show()
    }
    private fun clearError() {
        tilUsername.error=null
        tilPassword.error=null
    }
}
