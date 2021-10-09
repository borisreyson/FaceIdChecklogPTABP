package com.misit.faceidchecklogptabp.Chart

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.misit.faceidchecklogptabp.R
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_persentase.*

class PersentaseActivity : AppCompatActivity() {

    var list:ArrayList<PieEntry>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_persentase)
        title="Pengguna Aplikasi"
        var pengguna = intent.getStringExtra(PENGGUNA)
        var karyawan = intent.getStringExtra(KARYAWAN)
        var persentase = intent.getStringExtra(PERSENTASE)
        var p = persentase?.let { "%.2f".format(it.toFloat()).toFloat() }
        var description = Description()
        description.text=""
        persentaseTV.setText("${p.toString()} %")
        var pieChart:PieChart=findViewById(R.id.piePersentase)

        list =ArrayList()
        list?.add(PieEntry(pengguna!!.toFloat(),"Pengguna"))
        list?.add(PieEntry(karyawan!!.toFloat(),"Karyawan"))
        var dataPieSet = PieDataSet(list,"")
        var  pieData  = PieData(dataPieSet)
        pieChart.data=pieData
        pieChart.setEntryLabelTextSize(20f)
        pieChart.description=description
//        pieChart.holeRadius = 25f
//        pieChart.transparentCircleRadius=25f
        pieChart.animateXY(1400,1400)
        dataPieSet.valueTextSize=20f
        dataPieSet.setColors(ColorTemplate.MATERIAL_COLORS.toList())
    }
    companion object{
        var PENGGUNA = "PENGGUNA"
        var KARYAWAN = "KARYAWAN"
        var PERSENTASE = "PERSENTASE"
    }
}
