package com.misit.faceidchecklogptabp.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.SQLException

class JobServices: JobService() {
    private var TAG="JobScheduler"
    lateinit var jobParameters: JobParameters
    private var jobcanceled = false

    override fun onCreate() {
        PrefsUtil.initInstance(this@JobServices)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            COMPANY = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
        }
        super.onCreate()
    }
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Started")
        if(COMPANY!="COMPANY"){
            getMapArea()
        }
        if (params != null) {
            jobParameters = params
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG,"Job Cancelation Before Completion")
        jobcanceled=true
        return true

    }
    private fun getMapArea(){
        GlobalScope.launch(Dispatchers.IO) {
            var apiEndPoint = ApiClient.getClient(this@JobServices)?.create(ApiEndPoint::class.java)
            var res = apiEndPoint?.getMapArea(COMPANY)
            if(res!=null){
                if(res.isSuccessful){
                    var result = res.body()
                    if(result!=null){
                        var respon = result.mapAreaResponse
                        if(respon!=null){
                            respon.forEach{
                                var item = CompanyLocationModel()
                                item.idLok = it.idLok
                                item.company = it.company
                                item.lat = it.lat
                                item.lng = it.lng
                                item.flag = it.flag
                                item.time_update = it.timeUpdate
                                insertArea(COMPANY,it.timeUpdate!!,item,it.idLok!!.toInt())
                            }
                        }
                    }
                }
            }
        }
        jobFinished(jobParameters,true)
    }
    private fun insertArea(company:String,time_update:String,item:CompanyLocationModel,idLok:Int){
        var mapArea = MapAreaDataSource(this@JobServices)
        try {
            if(mapArea.newMap(company,time_update)<=0){
                mapArea.insertItem(item)
            }else{
                if(mapArea.cekMap(company,time_update)<0){
                    mapArea.updateItem(item,idLok)
                }
            }
        }catch (e:SQLException){
            Log.d(TAG,"${e.message}")
        }
    }
    companion object{
        var COMPANY = "COMPANY"
    }
}