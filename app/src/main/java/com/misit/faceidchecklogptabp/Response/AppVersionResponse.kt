package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class AppVersionResponse(

	@field:SerializedName("version")
	var version: String? = null,
	@field:SerializedName("url")
	var url: String? = null,
	@field:SerializedName("app")
	var app: String? = null
	)