package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class ImageResponse(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("res")
	val res: String? = null,

	@field:SerializedName("tidak_dikenal")
	val tidak_dikenal: Boolean = true
)