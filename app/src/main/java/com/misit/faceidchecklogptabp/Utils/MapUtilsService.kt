package com.misit.faceidchecklogptabp.Utils

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.JobIntentService
import com.misit.faceidchecklogptabp.services.JobServices

class MapUtilsService:IntentService("com.misit.faceidchecklogptabp") {
    lateinit var mapUtils :MapAreaUtils
    override fun onCreate() {
        Log.d("JobScheduler","On Create")

        mapUtils = MapAreaUtils()
        PrefsUtil.initInstance(this@MapUtilsService)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            COMPANY = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
        }
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d("JobScheduler","onHandleIntent")
        mapUtils.getMapArea(this@MapUtilsService,COMPANY)
    }
    companion object{
        var COMPANY="COMPANY"
    }

}