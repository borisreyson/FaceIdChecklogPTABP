package com.misit.faceidchecklogptabp.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.Utils.MapAreaUtils
import com.misit.faceidchecklogptabp.Utils.MapUtilsService
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.SQLException

class JobServices: JobService() {
    private var TAG="JobScheduler"
    lateinit var jobParameters: JobParameters
    private var jobcanceled = false
    lateinit var bgMapService : Intent
    override fun onCreate() {
        bgMapService = Intent(this@JobServices,MapUtilsService::class.java)
        super.onCreate()
    }
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Started")
        if (params != null) {
            jobParameters = params
        }
            startService(bgMapService)
            Log.d(TAG,"From Job Service Start")
        jobFinished(jobParameters,true)

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Cancelation Before Completion")
        stopService(bgMapService)
        jobcanceled=true
        return true
    }
}