package com.misit.faceidchecklogptabp.Utils

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.JobIntentService
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.services.JobServices

class MapUtilsService:Service() {
    lateinit var mapUtils :MapAreaUtils
    override fun onCreate() {
        Log.d("JobScheduler","On Create")
        mapUtils = MapAreaUtils()
        PrefsUtil.initInstance(this@MapUtilsService)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            COMPANY = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
        }
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("JobScheduler","onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("JobScheduler","onStartCommand")
        mapUtils.getMapArea(this@MapUtilsService,COMPANY,NIK)
        processWork(this@MapUtilsService,"onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }
    companion object{
        var COMPANY="COMPANY"
        var NIK="NIK"
    }
    private fun processWork(c: Context, counter:String){
        Log.d("JobScheduler","processWork")
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var rCode = (100..1000).random()
        ConfigUtil.showNotification(applicationContext,"Maps Utils Service","Notification ${counter}",intent,rCode,"MapUtils")
    }
}