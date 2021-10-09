package com.misit.faceidchecklogptabp.services

import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.Utils.ConfigUtil
import com.misit.faceidchecklogptabp.Utils.Constants

class FaceIdWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private var c = context
    private var scheduler : JobScheduler?=null
    override fun doWork(): Result {
        scheduler = c.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        workBackground()
        return Result.success()
    }

    private fun workBackground(){
        if(!ConfigUtil.isJobServiceOn(c, Constants.JOB_SERVICE_ID)){
            ConfigUtil.jobScheduler(c,scheduler)
            processWork(c,"New Run Service")
        }else{
            processWork(c,"Service Is Run")
        }
    }

    private fun processWork(c:Context,counter:String){
        val intent = Intent(applicationContext,HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var rCode = (100..1000).random()
        ConfigUtil.showNotification(applicationContext,"Work Manager","Notification ${counter}",intent,rCode,"WorkManager")
    }
}