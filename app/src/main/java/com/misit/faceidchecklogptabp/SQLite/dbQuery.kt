package com.misit.faceidchecklogptabp.SQLite

import android.database.sqlite.SQLiteDatabase

object dbQuery {

    fun tbAbsensi(db: SQLiteDatabase?){
        db?.execSQL("CREATE TABLE ABSENSI "+
                "(id INTEGER, "+
                "id_roster TEXT," +
                "nik TEXT," +
                "tanggal TEXT," +
                "jam TEXT," +
                "gambar TEXT," +
                "status TEXT," +
                "face_id TEXT," +
                "flag TEXT," +
                "OFF TEXT," +
                "IZIN_BERBAYAR TEXT," +
                "ALPA TEXT," +
                "CR TEXT," +
                "CT TEXT," +
                "SAKIT TEXT," +
                "lupa_absen TEXT," +
                "lat TEXT," +
                "lng TEXT," +
                "timeIn TEXT," +
                "tanggal_jam TEXT)")
    }
    fun tbMapArea(db: SQLiteDatabase?){
        db?.execSQL("CREATE TABLE map_area "+
                "(idLok INTEGER, "+
                "company TEXT," +
                "lat REAL," +
                "lng REAL," +
                "time_update Text," +
                "flag INTEGER)")
    }
    fun tbLastAbsen(db:SQLiteDatabase?){
        db?.execSQL("CREATE TABLE lastAbsen "+
                "(idLastAbsen INTEGER, "+
                "lastAbsen TEXT," +
                "lastNew Text," +
                "tanggal Text," +
                "masuk Text," +
                "pulang Text)")
    }
}