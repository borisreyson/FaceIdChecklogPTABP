package com.misit.faceidchecklogptabp.Utils

import android.content.Context
import android.util.Log
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.services.JobServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.SQLException

class MapAreaUtils() {
    var TAG= "JobScheduler"
    fun getMapArea(c:Context,COMPANY:String){
        GlobalScope.launch(Dispatchers.IO) {
            var apiEndPoint = ApiClient.getClient(c)?.create(ApiEndPoint::class.java)
            var res = apiEndPoint?.getMapArea(COMPANY)
            if(res!=null){
                if(res.isSuccessful){
                    var result = res.body()
                    if(result!=null){
                        var respon = result.mapArea
                        if(respon!=null){
                            respon.forEach{
                                var item = CompanyLocationModel()
                                item.idLok = it.idLok
                                item.company = it.company
                                item.lat = it.lat
                                item.lng = it.lng
                                item.flag = it.flag
                                item.time_update = it.timeUpdate
                                insertArea(c,COMPANY,"${it.lat}","${it.lng}",item,it.idLok!!.toInt(),"${it.timeUpdate}")
                                Log.d("JobScheduler","Map Database ${it.idLok}")

                            }
                        }
                    }
                }
            }
        }
    }
    private fun insertArea(c:Context,company:String, lat:String, lng:String, item: CompanyLocationModel, idLok:Int,timeUpdate:String){
        var mapArea = MapAreaDataSource(c)
        try {
                if(mapArea.cekMap(company,lat,lng)<=0){
                    mapArea.insertItem(item)
                    Log.d("JobScheduler","a")
                }else{
                    val cekMap = mapArea.getByIdMaps(idLok)
                    cekMap.forEach{
                        if(it.time_update != timeUpdate){
                            Log.d("JobScheduler","b ${it.idLok}")

                            mapArea.updateItem(item,it.idLok!!)
                        }else{
                            Log.d("JobScheduler","c ${it.idLok}")
                            mapArea.deleteItem(idLok)
                        }
                    }
                }
        }catch (e: SQLException){
            Log.d(TAG,"${e.message}")
        }
    }
    companion object{
    }
}