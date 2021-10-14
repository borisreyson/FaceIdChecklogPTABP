package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class LastAbsenResponse(

	@field:SerializedName("pulang")
	val pulang: String? = null,

	@field:SerializedName("presensiPulang")
	val presensiPulang: PresensiPulang? = null,

	@field:SerializedName("lastAbsen")
	val lastAbsen: String? = null,

	@field:SerializedName("masuk")
	val masuk: String? = null,

	@field:SerializedName("presensiMasuk")
	val presensiMasuk: PresensiMasuk? = null,

	@field:SerializedName("lastNew")
	val lastNew: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null
)

data class PresensiPulang(

	@field:SerializedName("ALPA")
	val aLPA: Int? = null,

	@field:SerializedName("flag")
	val flag: Int? = null,

	@field:SerializedName("lng")
	val lng: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null,

	@field:SerializedName("OFF")
	val oFF: Int? = null,

	@field:SerializedName("CR")
	val cR: Int? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("CT")
	val cT: Int? = null,

	@field:SerializedName("IZIN_BERBAYAR")
	val iZINBERBAYAR: Int? = null,

	@field:SerializedName("lupa_absen")
	val lupaAbsen: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("SAKIT")
	val sAKIT: Int? = null,

	@field:SerializedName("face_id")
	val faceId: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("id_roster")
	val idRoster: Any? = null,

	@field:SerializedName("lat")
	val lat: Int? = null,

	@field:SerializedName("timeIn")
	val timeIn: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("tanggal_jam")
	val tanggalJam: String? = null
)

data class PresensiMasuk(

	@field:SerializedName("ALPA")
	val aLPA: Int? = null,

	@field:SerializedName("flag")
	val flag: Int? = null,

	@field:SerializedName("lng")
	val lng: Int? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null,

	@field:SerializedName("OFF")
	val oFF: Int? = null,

	@field:SerializedName("CR")
	val cR: Int? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("CT")
	val cT: Int? = null,

	@field:SerializedName("IZIN_BERBAYAR")
	val iZINBERBAYAR: Int? = null,

	@field:SerializedName("lupa_absen")
	val lupaAbsen: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("SAKIT")
	val sAKIT: Int? = null,

	@field:SerializedName("face_id")
	val faceId: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("id_roster")
	val idRoster: Any? = null,

	@field:SerializedName("lat")
	val lat: Int? = null,

	@field:SerializedName("timeIn")
	val timeIn: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("tanggal_jam")
	val tanggalJam: String? = null
)
