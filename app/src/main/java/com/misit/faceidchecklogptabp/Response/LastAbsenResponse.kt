package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class LastAbsenResponse(

	@field:SerializedName("lastAbsen")
	val lastAbsen: String? = null,

	@field:SerializedName("lastNew")
	val lastNew: String? = null
)