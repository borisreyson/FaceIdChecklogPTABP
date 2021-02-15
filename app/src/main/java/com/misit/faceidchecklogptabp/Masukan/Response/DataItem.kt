package com.misit.faceidchecklogptabp.Masukan.Response

import com.google.gson.annotations.SerializedName

data class DataItem(

	@field:SerializedName("nik")
	var nik: String? = null,

	@field:SerializedName("flag")
	var flag: Int? = null,

	@field:SerializedName("nama")
	var nama: String? = null,

	@field:SerializedName("tgl_entry")
	var tglEntry: String? = null,

	@field:SerializedName("masukan")
	var masukan: String? = null,

	@field:SerializedName("tgl_reply")
	var tglReply: Any? = null,

	@field:SerializedName("id_reply")
	var idReply: Any? = null,

	@field:SerializedName("nik_reply")
	var nikReply: Any? = null,

	@field:SerializedName("id_masukan")
	var idMasukan: Int? = null
)