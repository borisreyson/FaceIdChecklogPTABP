package com.misit.faceidchecklogptabp.Utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.DataSource.AbsensiDataSources
import com.misit.faceidchecklogptabp.DataSource.MapAreaDataSource
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.Models.LastAbsenModels
import com.misit.faceidchecklogptabp.Models.TigaHariModel
import com.misit.faceidchecklogptabp.services.JobServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.sql.SQLException

class MapAreaUtils() {
    var TAG= "JobScheduler"
    suspend fun getMapArea(c:Context,COMPANY:String,nik:String){
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
                                insertArea(c,item,it.idLok!!.toInt(),"${it.timeUpdate}",nik)
                                Log.d("JobScheduler","Map Database ${it.idLok}")

                            }
                        }
                    }
                }
            }
        }
    }
    suspend private fun insertArea(c:Context, item: CompanyLocationModel, idLok:Int,timeUpdate:String,nik:String){
        var mapArea = MapAreaDataSource(c)
        try {
                if(mapArea.cekMap(idLok)<=0){
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
//                            mapArea.deleteItem(idLok)
                        }
                    }
                }
            coroutineScope {
                getAbsensi(c,nik)
            }
        }catch (e: SQLException){
            Log.d(TAG,"${e.message}")
        }
    }
    suspend private fun getAbsensi(c:Context,nik:String){
        val absensi = AbsensiDataSources(c)
        GlobalScope.launch {
            var absensiOnline = ApiClient.getClient(c)?.create(ApiEndPoint::class.java)
            var response = absensiOnline?.getAbsensi(nik)
            if(response!=null){
                if (response.isSuccessful){
                    var res = response.body()
                    if(res!=null){
                        var absen = res.absensi
                        if(absen!=null){
                            absen.forEach{
                                val result = absensi.cekById("${it.id}")
                                if(result<=0){
                                    var item = TigaHariModel()
                                    item.id = it.id
                                    item.id_roster = it.idRoster
                                    item.nik = it.nik
                                    item.tanggal = it.tanggal
                                    item.jam = it.jam
                                    item.gambar = it.gambar
                                    item.status = it.status
                                    item.face_id = it.faceId
                                    item.flag = "${it.flag}}"
                                    item.OFF = "${it.oFF}"
                                    item.IZIN_BERBAYAR = "${it.iZINBERBAYAR}"
                                    item.ALPA = "${it.aLPA}"
                                    item.CR = "${it.cR}"
                                    item.CT = "${it.cT}"
                                    item.SAKIT = "${it.sAKIT}"
                                    item.lupa_absen = it.lupaAbsen
                                    item.lat = "${it.lat}"
                                    item.lng = "${it.lat}"
                                    item.timeIn = it.timeIn
                                    absensi.insertItem(item)
                                }else{
                                    var item = TigaHariModel()
                                    item.id = it.id
                                    item.id_roster = it.idRoster
                                    item.nik = it.nik
                                    item.tanggal = it.tanggal
                                    item.jam = it.jam
                                    item.gambar = it.gambar
                                    item.status = it.status
                                    item.face_id = it.faceId
                                    item.flag = "${it.flag}"
                                    item.OFF = "${it.oFF}"
                                    item.IZIN_BERBAYAR = "${it.iZINBERBAYAR}"
                                    item.ALPA = "${it.aLPA}"
                                    item.CR = "${it.cR}"
                                    item.CT = "${it.cT}"
                                    item.SAKIT = "${it.sAKIT}"
                                    item.lupa_absen = it.lupaAbsen
                                    item.lat = "${it.lat}"
                                    item.lng = "${it.lat}"
                                    item.timeIn = it.timeIn
                                    absensi.updateItem(item,it.id!!)
                                }
                            }
                        }
                    }
                }
            }
            processWork(c,"MapAreaUtils")

//            loadAbsen(c,nik)
        }
    }
    suspend private fun loadAbsen(c:Context,nik:String){
        val absensi = AbsensiDataSources(c)
        val apiEndPoint = ApiClient.getClient(c)?.create(ApiEndPoint::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            val call = apiEndPoint?.lastAbsenCorutine(nik)
            if (call != null) {
                if (call.isSuccessful) {
                    absensi.deleteLastAbsen()
                    val response = call.body()
                    if (response != null) {
                        var item = LastAbsenModels()
                        item.lastAbsen = response.lastAbsen
                        item.lastNew = response.lastNew
                        item.masuk = response.masuk
                        item.pulang = response.pulang
                        item.tanggal = response.tanggal
                        absensi.instertLastAbsen(item)
                    }
                }
            }
        }
    }

    private fun processWork(c: Context, counter:String){
        Log.d("JobScheduler","processWork")
        val intent = Intent(c, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var rCode = (100..1000).random()
        ConfigUtil.showNotification(c,"Maps Utils Service","Notification ${counter}",intent,rCode,"MapUtils")
    }
}