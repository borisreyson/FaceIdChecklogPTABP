package com.misit.faceidchecklogptabp.Masukan.Response

import com.google.gson.annotations.SerializedName

data class MasukanResponse(

	@field:SerializedName("path")
	var path: String? = null,

	@field:SerializedName("per_page")
	var perPage: Int? = null,

	@field:SerializedName("total")
	var total: Int? = null,

	@field:SerializedName("data")
	var data: List<DataItem>? = null,

	@field:SerializedName("last_page")
	var lastPage: Int? = null,

	@field:SerializedName("next_page_url")
	var nextPageUrl: String? = null,

	@field:SerializedName("from")
	var from: Int? = null,

	@field:SerializedName("to")
	var to: Int? = null,

	@field:SerializedName("prev_page_url")
	var prevPageUrl: Any? = null,

	@field:SerializedName("current_page")
	var currentPage: Int? = null
)