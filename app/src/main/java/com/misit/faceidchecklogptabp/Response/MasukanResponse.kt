package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class MasukanResponse(

    @field:SerializedName("success")
    var success: Boolean? = null
)