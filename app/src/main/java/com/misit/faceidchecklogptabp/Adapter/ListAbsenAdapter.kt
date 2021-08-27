package com.misit.faceidchecklogptabp.Adapter

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.textfield.TextInputEditText
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Response.Absen.DataItems
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class ListAbsenAdapter (
    private val context: Context?,
    private val listAbsen:MutableList<DataItems>):
    RecyclerView.Adapter<ListAbsenAdapter.MyViewHolder>(){
    private val layoutInflater: LayoutInflater
    private var simpleDateFormat: SimpleDateFormat? = null
    private var onItemClickListener: OnItemClickListener? = null
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM, yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAbsenAdapter.MyViewHolder {
        val view = layoutInflater.inflate(R.layout.grid_list_absen,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listAbsen.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var listAbsen =listAbsen[position]
        if(listAbsen.gambar!=null){
            Glide.with(context!!)
                .load(listAbsen?.gambar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imgFace)
            if(onItemClickListener!=null){
                holder.cvGrid.setOnClickListener{
                    onItemClickListener?.onItemClick(
                        listAbsen.nik,
                        listAbsen.nama,
                        listAbsen.gambar,
                        listAbsen.tanggal,
                        listAbsen.jam,
                        listAbsen.status,
                        listAbsen.lat,
                        listAbsen.lng)
                }
            }
        }
        holder.nama.text = listAbsen.nama
        val tanggal = LocalDate.parse(listAbsen.tanggal)

        holder.tanggal.text = tanggal.toString(fmt)?.toLowerCase(Locale.getDefault())
        holder.jam.text = listAbsen.jam
        holder.status.text = listAbsen.status
        holder.tvKeterangan.text="${listAbsen.lupa_absen}"
        if(listAbsen.lat!="0" && listAbsen.lng!="0"){
        }
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgFace = itemView.findViewById<View>(R.id.imgFace) as ImageView
        var nama = itemView.findViewById<View>(R.id.tvNama) as TextView
        var tanggal = itemView.findViewById<View>(R.id.tvTGL) as TextView
        var jam = itemView.findViewById<View>(R.id.tvJam) as TextView
        var status = itemView.findViewById<View>(R.id.tvStatus) as TextView
        var cvGrid = itemView.findViewById<View>(R.id.cvGrid) as CardView
        var tvKeterangan = itemView.findViewById<View>(R.id.tvKeterangan) as TextView
        var tvLAT = itemView.findViewById<View>(R.id.tvLAT) as TextView
        var tvLNG = itemView.findViewById<View>(R.id.tvLNG) as TextView

    }
    interface OnItemClickListener{
        fun onItemClick(
            nik:String?,
            nama:String?,
            gambar:String?,
            tgl:String?,
            jam:String?,
            status:String?,
            lat:String?,
            lng:String?)
    }
    fun setListener (listener:OnItemClickListener){
        onItemClickListener = listener
    }

    init {
        layoutInflater = LayoutInflater.from(context)
    }
}