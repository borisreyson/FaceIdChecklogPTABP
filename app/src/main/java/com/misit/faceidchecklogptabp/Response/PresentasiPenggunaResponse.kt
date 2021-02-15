package com.misit.faceidchecklogptabp.Response

import com.google.gson.annotations.SerializedName

data class PresentasiPenggunaResponse(

	@field:SerializedName("persentasi")
	val persentasi: Double? = null,

	@field:SerializedName("jumlah_karyawan")
	val jumlah_karyawan: Int? = null,
	@field:SerializedName("jumlah_pengguna")
	val jumlah_pengguna: Int? = null
)