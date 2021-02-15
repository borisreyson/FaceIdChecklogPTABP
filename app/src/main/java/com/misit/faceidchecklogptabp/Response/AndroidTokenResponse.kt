package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class AndroidTokenResponse(

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

	@field:SerializedName("id")
	val id: Int? = null
)