package com.misit.faceidchecklogptabp.Response.MainResponse

import com.google.gson.annotations.SerializedName

data class FirstLoadResponse(

	@field:SerializedName("area")
	val area: String? = null,

	@field:SerializedName("hari")
	val hari: String? = null,

	@field:SerializedName("roster")
	val roster: Roster? = null,

	@field:SerializedName("detik")
	val detik: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("presensi")
	val presensi: List<PresensiItem?>? = null,

	@field:SerializedName("absensi")
	val absensi: Absensi? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("menit")
	val menit: String? = null
)

data class Absensi(

	@field:SerializedName("app")
	val app: String? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("phone_token")
	val phoneToken: String? = null,

	@field:SerializedName("flag")
	val flag: Int? = null,

	@field:SerializedName("app_version")
	val appVersion: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("tgl")
	val tgl: String? = null,

	@field:SerializedName("imei")
	val imei: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class PresensiItem(

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("pulang")
	val pulang: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("masuk")
	val masuk: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("id_roster")
	val idRoster: Int? = null,

	@field:SerializedName("kode_jam")
	val kodeJam: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Roster(

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("pulang")
	val pulang: String? = null,

	@field:SerializedName("masuk")
	val masuk: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("id_roster")
	val idRoster: Int? = null,

	@field:SerializedName("kode_jam")
	val kodeJam: String? = null
)
