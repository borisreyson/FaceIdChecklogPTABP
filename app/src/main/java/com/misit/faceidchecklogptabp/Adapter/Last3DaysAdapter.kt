package com.misit.faceidchecklogptabp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Response.Absen.DataItems
import java.text.SimpleDateFormat

class Last3DaysAdapter(
    private val context: Context?,
    private val listAbsen:MutableList<DataItems>):
    RecyclerView.Adapter<ListAbsenAdapter.MyViewHolder>(){

    private val layoutInflater: LayoutInflater
    private var simpleDateFormat: SimpleDateFormat? = null
    private var onItemClickListener: ListAbsenAdapter.OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListAbsenAdapter.MyViewHolder {
        val view = layoutInflater.inflate(R.layout.last_3_days_absen,parent,false)
        return ListAbsenAdapter.MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ListAbsenAdapter.MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    init {
        layoutInflater = LayoutInflater.from(context)
    }
}