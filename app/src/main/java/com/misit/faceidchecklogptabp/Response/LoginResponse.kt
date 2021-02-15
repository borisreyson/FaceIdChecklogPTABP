package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
	@field:SerializedName("success")
	var success: Boolean? = null,

	@field:SerializedName("dataLogin")
	var dataLogin: DataLogin? = null
)
