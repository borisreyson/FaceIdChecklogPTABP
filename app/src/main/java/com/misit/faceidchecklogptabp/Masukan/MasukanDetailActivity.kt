package com.misit.faceidchecklogptabp.Masukan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.misit.faceidchecklogptabp.R
import kotlinx.android.synthetic.main.activity_masukan_detail.*
import kotlin.math.tan

class MasukanDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masukan_detail)

        var actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        var nik = intent.getStringExtra(NIK)
        var nama = intent.getStringExtra(NAMA)
        var masukan = intent.getStringExtra(MASUKAN)
        var tanggal = intent.getStringExtra(TANGGAL)

        title= "( " + nik + " ) " + nama
        tvMasukanDetail.text = "${masukan}"
        tvTanggal.text = "${tanggal}"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    companion object{
        var NIK = "NIK"
        var NAMA ="NAMA"
        var MASUKAN = "MASUKAN"
        var TANGGAL = "TANGGAL"
    }
}
