package com.misit.faceidchecklogptabp.SQLite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(c: Context): SQLiteOpenHelper(c,DB_NAME,null,2) {

    companion object{
        val DB_NAME = "abp.db"
        val tb = arrayOf("ABSENSI")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        dbQuery.tbAbsensi(db)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        tb.forEach {
            db?.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + it + "'")
        }
        tb.forEach {
            db?.execSQL("DROP TABLE IF EXISTS ${it}")
        }
        onCreate(db)
    }
}