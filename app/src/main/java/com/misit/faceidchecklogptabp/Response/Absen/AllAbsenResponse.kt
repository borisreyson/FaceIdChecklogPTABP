package com.misit.faceidchecklogptabp.Response.Absen

import com.google.gson.annotations.SerializedName

data class AllAbsenResponse(

    @field:SerializedName("path")
    val path: String? = null,

    @field:SerializedName("per_page")
    val perPage: Int? = null,

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("data")
    val data: List<DataItems>? = null,

    @field:SerializedName("last_page")
    val lastPage: Int? = null,

    @field:SerializedName("next_page_url")
    val nextPageUrl: Any? = null,

    @field:SerializedName("from")
    val from: Int? = null,

    @field:SerializedName("to")
    val to: Int? = null,

    @field:SerializedName("prev_page_url")
    val prevPageUrl: Any? = null,

    @field:SerializedName("current_page")
    val currentPage: Int? = null
)