package com.misit.faceidchecklogptabp.DataSource

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import com.misit.faceidchecklogptabp.Models.CompanyLocationModel
import com.misit.faceidchecklogptabp.SQLite.DbHelper

class MapAreaDataSource(val c: Context) {
    var dbHelper : DbHelper
    var sqlDatabase : SQLiteDatabase?=null
    var listItem :ArrayList<CompanyLocationModel>?=null
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
    fun cekMap(company: String,time_update:String): Int {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT count(*) FROM "+
                "${tbItem} WHERE company = '"+company+"' and time_update= '"+time_update+"' ",null)
        c?.let {
            if(it.moveToFirst()){
                return it?.getIntOrNull(0) ?: 0
            }
        }
        c?.close()
        closeAccess()
        return 0
    }

    fun newMap(company: String,time_update:String): Int {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT count(*) FROM "+
                "${tbItem} WHERE company = '"+company+"' ",null)
        c?.let {
            if(it.moveToFirst()){
                return it?.getIntOrNull(0) ?: 0
            }
        }
        c?.close()
        closeAccess()
        return 0
    }
    fun insertItem(item: CompanyLocationModel):Long{
        openAccess()
        var cv = createCV(item)
        var hasil = sqlDatabase?.insertOrThrow("${tbItem}",null,cv)
        closeAccess()
        return hasil!!
    }
    fun getMaps(company:String): ArrayList<CompanyLocationModel> {
        openAccess()
        val c = sqlDatabase?.rawQuery("SELECT * FROM "+
                "${tbItem} WHERE company = '$company'",null)
        if(c!!.moveToFirst()){
            do {
                listItem?.add(fetchRow(c))
            }while (c.moveToNext())
        }
        c?.close()
        closeAccess()
        return listItem!!
    }
    private fun fetchRow(cursor: Cursor): CompanyLocationModel {
        val idLok = cursor.getInt(cursor.getColumnIndex("idLok"))
        val company = cursor.getString(cursor.getColumnIndex("company"))
        val lat = cursor.getDouble(cursor.getColumnIndex("lat"))
        val lng = cursor.getDouble(cursor.getColumnIndex("lng"))
        val flag = cursor.getInt(cursor.getColumnIndex("flag"))
        val time_update = cursor.getString(cursor.getColumnIndex("time_update"))
        val mapLocation = CompanyLocationModel()
        mapLocation.idLok= idLok
        mapLocation.company = company
        mapLocation.lat = lat
        mapLocation.lng = lng
        mapLocation.flag = flag
        mapLocation.time_update = time_update
        return mapLocation
    }
    fun updateItem(item: CompanyLocationModel, idLok:Int):Boolean{
        openAccess()
        val items = ContentValues()
        items.put("idLok",item.idLok)
        items.put("company",item.company)
        items.put("lat",item.lat)
        items.put("lng",item.lng)
        items.put("flag",item.flag)
        items.put("time_update",item.time_update)

        val hasil = sqlDatabase?.update("$tbItem",items,"idLok = ?", arrayOf("${idLok}"))
        if(hasil!! < 0){
            return false
        }
        closeAccess()
        return true
    }
    private fun createCV(item : CompanyLocationModel): ContentValues {
        var items = ContentValues()
        items.put("idLok",item.idLok)
        items.put("company",item.company)
        items.put("lat",item.lat)
        items.put("lng",item.lng)
        items.put("flag",item.flag)
        items.put("time_update",item.time_update)
        return items
    }
    companion object{
        val tbItem = "map_area"
    }
}