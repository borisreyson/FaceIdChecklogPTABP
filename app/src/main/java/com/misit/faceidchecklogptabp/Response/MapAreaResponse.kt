package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class MapAreaResponse(

	@field:SerializedName("MapAreaResponse")
    var mapAreaResponse: List<MapAreaResponseItem>? = null
)

data class MapAreaResponseItem(

	@field:SerializedName("time_update")
	var timeUpdate: String? = null,

	@field:SerializedName("flag")
    var flag: Int? = null,

	@field:SerializedName("lng")
    var lng: Double? = null,

	@field:SerializedName("company")
    var company: String? = null,

	@field:SerializedName("idLok")
    var idLok: Int? = null,

	@field:SerializedName("lat")
    var lat: Double? = null
)
