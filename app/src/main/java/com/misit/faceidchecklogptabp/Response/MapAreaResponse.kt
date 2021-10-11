package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class MapAreaResponse(

	@field:SerializedName("mapArea")
	val mapArea: List<MapAreaItem>? = null
)

data class MapAreaItem(

	@field:SerializedName("time_update")
	val timeUpdate: String? = null,

	@field:SerializedName("flag")
	val flag: Int? = null,

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("company")
	val company: String? = null,

	@field:SerializedName("idLok")
	val idLok: Int? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)
