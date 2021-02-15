package com.misit.faceidchecklogptabp.Response.Absen

import com.google.gson.annotations.SerializedName

data class DataItems(

    @field:SerializedName("no")
    var no: Int? = null,

    @field:SerializedName("devisi")
    var devisi: String? = null,

    @field:SerializedName("ALPA")
    var aLPA: Int? = null,

    @field:SerializedName("flag")
    var flag: Int? = null,

    @field:SerializedName("jabatan")
    var jabatan: String? = null,

    @field:SerializedName("gambar")
    var gambar: String? = null,

    @field:SerializedName("show_absen")
    var showAbsen: Int? = null,

    @field:SerializedName("OFF")
    var oFF: Int? = null,

    @field:SerializedName("CR")
    var cR: Int? = null,

    @field:SerializedName("departemen")
    var departemen: String? = null,

    @field:SerializedName("nik")
    var nik: String? = null,

    @field:SerializedName("CT")
    var cT: Int? = null,

    @field:SerializedName("tgl_up")
    var tglUp: Any? = null,

    @field:SerializedName("IZIN_BERBAYAR")
    var iZINBERBAYAR: Int? = null,

    @field:SerializedName("password")
    var password: String? = null,

    @field:SerializedName("nama")
    var nama: String? = null,

    @field:SerializedName("user_entry")
    var userEntry: String? = null,

    @field:SerializedName("jam")
    var jam: String? = null,

    @field:SerializedName("tgl_entry")
    var tglEntry: String? = null,

    @field:SerializedName("SAKIT")
    var sAKIT: Int? = null,

    @field:SerializedName("face_id")
    var faceId: String? = null,

    @field:SerializedName("id")
    var id: Int? = null,

    @field:SerializedName("tanggal")
    var tanggal: String? = null,

    @field:SerializedName("status")
    var status: String? = null,

    @field:SerializedName("lupa_absen")
    var lupa_absen: String? = null,

    @field:SerializedName("lat")
    var lat: String? = null,

    @field:SerializedName("lng")
    var lng: String? = null
)