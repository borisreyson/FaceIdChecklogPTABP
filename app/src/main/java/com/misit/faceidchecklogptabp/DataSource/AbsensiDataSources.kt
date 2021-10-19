package com.misit.faceidchecklogptabp.DataSource

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.getIntOrNull
import com.misit.faceidchecklogptabp.Models.LastAbsenModels
import com.misit.faceidchecklogptabp.Models.TigaHariModel
import com.misit.faceidchecklogptabp.SQLite.DbHelper

class AbsensiDataSources(val c: Context) {
    var dbHelper : DbHelper
    var sqlDatabase : SQLiteDatabase?=null
    var listItem :ArrayList<TigaHariModel>?=null
    init {
        listItem = ArrayList()
        dbHelper = DbHelper(c)
    }
    private fun openAccess(){
        sqlDatabase = dbHelper.writableDatabase
    }
    private fun closeAccess(){
        sqlDatabase?.close()
        dbHelper?.close()
    }

    fun cekAbsensi(nik: String): Int {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT count(*) FROM "+
                "${tbItem} WHERE nik = '"+nik+"' ",null)
        c?.let {
            if(it.moveToFirst()){
                return it?.getIntOrNull(0) ?: 0
            }
        }
        c?.close()
        closeAccess()
        return 0
    }

    fun getItem(nik: String,status:String): TigaHariModel {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT * FROM "+
                "$tbItem WHERE nik = ? and status =? order by id desc", arrayOf(nik,status))
        c?.moveToFirst()
        var itemModels = TigaHariModel()
        c?.let {
            itemModels = fetchRow(it)
        }
        c?.close()
        closeAccess()
        return itemModels
    }

    fun lastAbsen(): LastAbsenModels? {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT * FROM "+
                "$tbLastAbsen", null)
        c?.use {
            if(c!!.moveToFirst() ) {
                val sendItem = rowLastAbsen(c!!)
                Log.d("sendItem","${sendItem.tanggal}")
                if(sendItem!=null){
                    return sendItem
                    c?.close()
                    closeAccess()
                }else{
                    c.moveToFirst()
                }
            }else{
                c.moveToFirst()
            }
        }
        return null
    }

    fun cekLastAbsen(): Int {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT count(*) FROM "+
                "${tbLastAbsen}",null)
        c?.let {
            if(it.moveToFirst()){
                return it?.getIntOrNull(0) ?: 0
            }
        }
        c?.close()
        closeAccess()
        return 0
    }
    fun getAll(nik: String): ArrayList<TigaHariModel> {
        val listItem : ArrayList<TigaHariModel> = ArrayList()
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT * FROM "+
                "$tbItem WHERE nik = ? ORDER BY id", arrayOf(nik))
        if(c!!.moveToFirst()){
            do {
                listItem?.add(fetchRow(c))
            }while (c.moveToNext())

        }
        c?.close()
        closeAccess()
        return listItem!!
    }


    fun cekById(id: String): Int {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT count(*) FROM "+
                "${tbItem} WHERE id = '"+id+"' ",null)
        c?.let {
            if(it.moveToFirst()){
                return it?.getIntOrNull(0) ?: 0
            }
        }
        c?.close()
        closeAccess()
        return 0
    }
    fun insertItem(item: TigaHariModel):Long{
        openAccess()
        var cv = createCV(item)
        var hasil = sqlDatabase?.insertOrThrow("$tbItem",null,cv)
        closeAccess()
        return hasil!!
    }

    fun instertLastAbsen(item: LastAbsenModels):Long{
        openAccess()
        var cv = cvLastAbsen(item)
        var hasil = sqlDatabase?.insertOrThrow("$tbLastAbsen",null,cv)
        closeAccess()
        return hasil!!
    }
    fun deleteLastAbsen():Boolean{
        openAccess()
        val hasil = sqlDatabase?.delete("$tbLastAbsen",null,null)
        if(hasil!! <0 ){
            return false
        }
        closeAccess()
        return true
    }
    private fun rowLastAbsen(cursor: Cursor): LastAbsenModels{
        val lastAbsen = cursor.getString(cursor.getColumnIndex("lastAbsen"))
        val lastNew = cursor.getString(cursor.getColumnIndex("lastNew"))
        val masuk = cursor.getString(cursor.getColumnIndex("masuk"))
        val pulang = cursor.getString(cursor.getColumnIndex("pulang"))
        val tanggal = cursor.getString(cursor.getColumnIndex("tanggal"))
        val items = LastAbsenModels()
        items.lastAbsen = lastAbsen
        items.lastNew = lastNew
        items.masuk = masuk
        items.pulang = pulang
        items.tanggal = tanggal
        return items
    }
    private fun fetchRow(cursor: Cursor): TigaHariModel {
        val id = cursor.getInt(cursor.getColumnIndex("id"))
        val id_roster = cursor.getString(cursor.getColumnIndex("id_roster"))
        val nik = cursor.getString(cursor.getColumnIndex("nik"))
        val tanggal = cursor.getString(cursor.getColumnIndex("tanggal"))
        val jam = cursor.getString(cursor.getColumnIndex("jam"))
        val gambar = cursor.getString(cursor.getColumnIndex("gambar"))
        val status = cursor.getString(cursor.getColumnIndex("status"))
        val face_id = cursor.getString(cursor.getColumnIndex("face_id"))
        val flag = cursor.getString(cursor.getColumnIndex("flag"))
        val OFF = cursor.getString(cursor.getColumnIndex("OFF"))
        val IZIN_BERBAYAR = cursor.getString(cursor.getColumnIndex("IZIN_BERBAYAR"))
        val ALPA = cursor.getString(cursor.getColumnIndex("ALPA"))
        val CR = cursor.getString(cursor.getColumnIndex("CR"))
        val CT = cursor.getString(cursor.getColumnIndex("CT"))
        val SAKIT = cursor.getString(cursor.getColumnIndex("SAKIT"))
        val lupa_absen = cursor.getString(cursor.getColumnIndex("lupa_absen"))
        val lat = cursor.getString(cursor.getColumnIndex("lat"))
        val lng = cursor.getString(cursor.getColumnIndex("lng"))
        val timeIn = cursor.getString(cursor.getColumnIndex("timeIn"))
        val tanggal_jam = cursor.getString(cursor.getColumnIndex("tanggal_jam"))
        val tigaHari = TigaHariModel()
        tigaHari.id = id
        tigaHari.id_roster = id_roster
        tigaHari.nik = nik
        tigaHari.tanggal = tanggal
        tigaHari.jam = jam
        tigaHari.gambar = gambar
        tigaHari.status = status
        tigaHari.face_id = face_id
        tigaHari.flag = flag
        tigaHari.OFF = OFF
        tigaHari.IZIN_BERBAYAR = IZIN_BERBAYAR
        tigaHari.ALPA = ALPA
        tigaHari.CR = CR
        tigaHari.CT = CT
        tigaHari.SAKIT = SAKIT
        tigaHari.lupa_absen = lupa_absen
        tigaHari.lat = lat
        tigaHari.lng = lng
        tigaHari.timeIn = timeIn
        tigaHari.tanggal_jam = tanggal_jam
        return tigaHari
    }
    fun updateItem(item: TigaHariModel, id:Int):Boolean{
        openAccess()
        val items = ContentValues()
        items.put("id_roster",item.id_roster)
        items.put("nik",item.nik)
        items.put("tanggal",item.tanggal)
        items.put("jam",item.jam)
        items.put("gambar",item.gambar)
        items.put("status",item.status)
        items.put("face_id",item.face_id)
        items.put("flag",item.flag)
        items.put("OFF",item.OFF)
        items.put("IZIN_BERBAYAR",item.IZIN_BERBAYAR)
        items.put("ALPA",item.ALPA)
        items.put("CR",item.CR)
        items.put("CT",item.CT)
        items.put("SAKIT",item.SAKIT)
        items.put("lupa_absen",item.lupa_absen)
        items.put("lat",item.lat)
        items.put("lng",item.lng)
        items.put("timeIn",item.timeIn)
        items.put("tanggal_jam",item.tanggal_jam)

        val hasil = sqlDatabase?.update("$tbItem",items,"id = ?", arrayOf("${id}"))
        if(hasil!! < 0){
            return false
        }
        closeAccess()
        return true
    }
    private fun cvLastAbsen(item : LastAbsenModels): ContentValues {
        var items = ContentValues()
        items.put("idLastAbsen",item.idLastAbsen)
        items.put("lastAbsen",item.lastAbsen)
        items.put("lastNew",item.lastNew)
        items.put("masuk",item.masuk)
        items.put("pulang",item.pulang)
        items.put("tanggal",item.tanggal)
        return items

    }
    private fun createCV(item : TigaHariModel): ContentValues {
        var items = ContentValues()
        items.put("id",item.id)
        items.put("id_roster",item.id_roster)
        items.put("nik",item.nik)
        items.put("tanggal",item.tanggal)
        items.put("jam",item.jam)
        items.put("gambar",item.gambar)
        items.put("status",item.status)
        items.put("face_id",item.face_id)
        items.put("flag",item.flag)
        items.put("OFF",item.OFF)
        items.put("IZIN_BERBAYAR",item.IZIN_BERBAYAR)
        items.put("ALPA",item.ALPA)
        items.put("CR",item.CR)
        items.put("CT",item.CT)
        items.put("SAKIT",item.SAKIT)
        items.put("lupa_absen",item.lupa_absen)
        items.put("lat",item.lat)
        items.put("lng",item.lng)
        items.put("timeIn",item.timeIn)
        items.put("tanggal_jam",item.tanggal_jam)
        return items
    }
    companion object{
        val tbItem = "ABSENSI"
        val tbLastAbsen = "lastAbsen"
    }
}