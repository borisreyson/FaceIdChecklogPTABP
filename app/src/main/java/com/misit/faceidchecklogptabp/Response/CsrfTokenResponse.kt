package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class CsrfTokenResponse(

    @field:SerializedName("csrf_token")
    val csrfToken: String? = null
)