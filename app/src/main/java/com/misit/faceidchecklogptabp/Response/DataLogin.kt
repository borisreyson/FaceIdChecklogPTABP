package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class DataLogin(
	@field:SerializedName("nik")
	val nik: String? = null,
	@field:SerializedName("nama")
	val nama: String? = null,
	@field:SerializedName("show_absen")
	val show_absen: String? = null,
	@field:SerializedName("perusahaan")
	val perusahaan: String? = null


)
