package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class AbsensiResponse(
	@field:SerializedName("absensi")
	var absensi: List<AbsensiItem>? = null
)

data class AbsensiItem(

	@field:SerializedName("ALPA")
	var aLPA: Int? = null,

	@field:SerializedName("flag")
	var flag: Int? = null,

	@field:SerializedName("lng")
	var lng: Int? = null,

	@field:SerializedName("gambar")
	var gambar: String? = null,

	@field:SerializedName("OFF")
	var oFF: Int? = null,

	@field:SerializedName("CR")
	var cR: Int? = null,

	@field:SerializedName("nik")
	var nik: String? = null,

	@field:SerializedName("CT")
	var cT: Int? = null,

	@field:SerializedName("IZIN_BERBAYAR")
	var iZINBERBAYAR: Int? = null,

	@field:SerializedName("lupa_absen")
	var lupaAbsen: String? = null,

	@field:SerializedName("jam")
	var jam: String? = null,

	@field:SerializedName("SAKIT")
	var sAKIT: Int? = null,

	@field:SerializedName("face_id")
	var faceId: String? = null,

	@field:SerializedName("id")
	var id: Int? = null,

	@field:SerializedName("tanggal")
	var tanggal: String? = null,

	@field:SerializedName("id_roster")
	var idRoster: String? = null,

	@field:SerializedName("lat")
	var lat: Int? = null,

	@field:SerializedName("timeIn")
	var timeIn: String? = null,

	@field:SerializedName("status")
	var status: String? = null
)
