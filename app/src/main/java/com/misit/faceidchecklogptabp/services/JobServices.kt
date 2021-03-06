package com.misit.faceidchecklogptabp.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.Utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class JobServices: JobService() {
    private var TAG="JobScheduler"
    lateinit var jobParameters: JobParameters
    private var jobcanceled = false
    lateinit var mapUtils :MapAreaUtils

    override fun onCreate() {
        mapUtils = MapAreaUtils()
        PrefsUtil.initInstance(this@JobServices)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            COMPANY = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
        }
        super.onCreate()
    }
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Started")
        if (params != null) {
            jobParameters = params
        }
        showNotification()
        Log.d(TAG,"From Job Service Start")
        jobFinished(jobParameters,true)
        return true
    }

    private fun showNotification(){
        GlobalScope.launch(Dispatchers.IO) {
            mapUtils.getMapArea(this@JobServices, COMPANY, MapUtilsService.NIK)
            processWork(this@JobServices,"onStartCommand")
        }
    }
    private fun processWork(c: Context, counter:String){
        Log.d("JobScheduler","processWork")
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var rCode = (100..1000).random()
        ConfigUtil.showNotification(applicationContext,"Maps Utils Service","Notification ${counter}",intent,rCode,"MapUtils")
    }
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Cancelation Before Completion")
//        stopService(bgMapService)
        jobcanceled=true
        return true
    }
    companion object{
        var COMPANY="COMPANY"
        var NIK="NIK"
    }
}