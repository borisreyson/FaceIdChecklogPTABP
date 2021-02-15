package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class AbpResponse(

	@field:SerializedName("area")
	var area: String? = null,
	@field:SerializedName("jam")
	var jam: String? = null,
	@field:SerializedName("menit")
	var menit: String? = null,
	@field:SerializedName("detik")
	var detik: String? = null
)