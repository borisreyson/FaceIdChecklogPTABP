package com.misit.faceidchecklogptabp.services

import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.Utils.ConfigUtil
import com.misit.faceidchecklogptabp.Utils.Constants

class FaceIdWorker(context: Context) {
    private var c = context
    private var scheduler : JobScheduler?=null
//    override fun doWork(): Result {
//        scheduler = c.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
//        workBackground()
//        return Result.success()
//    }

    private fun workBackground(){
//        if(!ConfigUtil.isJobServiceOn(c, Constants.JOB_SERVICE_ID)){
            Log.d("JobScheduler","New Run Service")

//            ConfigUtil.jobScheduler(c,scheduler)
//            processWork(c,"workBackground New Run Service")
//        }else{
            Log.d("JobScheduler","workBackground Service Is Run")

//            processWork(c,"Service Is Run")
//        }
    }

    private fun processWork(c:Context,counter:String){
//        val intent = Intent(applicationContext,HomeActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
        var rCode = (100..1000).random()
//        ConfigUtil.showNotification(applicationContext,"Work Manager","Notification ${counter}",intent,rCode,"WorkManager")
    }
}