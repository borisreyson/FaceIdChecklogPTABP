package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class DataItem(

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("jam")
	val jam: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("gambar")
	val gambar: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("lupa_absen")
	val lupa_absen: String? = null,

	@field:SerializedName("lat")
	var lat: String? = null,

	@field:SerializedName("lng")
	var lng: String? = null
)