package com.misit.faceidchecklogptabp.Masukan.Response

import com.google.gson.annotations.SerializedName

data class MasukanModel(
    @field:SerializedName("id_masukan")
    var id: Int? = null,
    @field:SerializedName("nik")
    var nik: String? = null,
    @field:SerializedName("nama")
    var nama: String? = null,
    @field:SerializedName("masukan")
    var jabatan: String? = null
)