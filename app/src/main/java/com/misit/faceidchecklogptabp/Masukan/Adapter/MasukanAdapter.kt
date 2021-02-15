package com.misit.faceidchecklogptabp.Masukan.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.misit.faceidchecklogptabp.Masukan.Response.DataItem
import com.misit.faceidchecklogptabp.R
import java.text.SimpleDateFormat

class MasukanAdapter(
    private val context: Context?,
    private val masukanModelList:MutableList<DataItem>):
    RecyclerView.Adapter<MasukanAdapter.MyViewHolder>(){

    private val layoutInflater: LayoutInflater
    private var simpleDateFormat: SimpleDateFormat? = null
    private var onItemClickListener: OnItemClickListener? = null
    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvNik = itemView.findViewById<View>(R.id.tvNik) as TextView
        var tvNama = itemView.findViewById<View>(R.id.tvNama) as TextView
        var lnMasukan = itemView.findViewById<View>(R.id.lnMasukan) as LinearLayout
        var tvMasukannya = itemView.findViewById<View>(R.id.tvMasukannya) as TextView
        var tvTanggal = itemView.findViewById<View>(R.id.tvTanggal) as TextView
        var imgUnread = itemView.findViewById<View>(R.id.imgUnread) as ImageView
        var imgReading = itemView.findViewById<View>(R.id.imgReading) as ImageView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = layoutInflater.inflate(R.layout.list_masukan,
            parent,
            false)
        return MyViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return masukanModelList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var masukanList =masukanModelList[position]
        holder.tvNik.text= masukanList.nik
        holder.tvNama.text= masukanList.nama
        holder.tvTanggal.text = masukanList.tglEntry
        holder.tvMasukannya.text = masukanList.masukan
        if(masukanList.flag==0){
            holder.imgUnread.visibility = View.VISIBLE
            holder.imgReading.visibility = View.GONE
        }else{
            holder.imgUnread.visibility = View.GONE
            holder.imgReading.visibility = View.VISIBLE
        }
        if(onItemClickListener!=null) {
                holder.lnMasukan.setOnClickListener{
                    onItemClickListener?.onItemClick(
                        masukanList.idMasukan,
                        masukanList.nik,
                        masukanList.nama,
                        masukanList.masukan,
                        masukanList.tglEntry)
                }
            }
        }
    interface OnItemClickListener{
        fun onItemClick(idMasukan:Int?,nik:String?,nama:String?,masukan:String?,tgl:String?)
    }

    fun setListener (listener: OnItemClickListener){
        onItemClickListener = listener
    }
    init {
        layoutInflater = LayoutInflater.from(context)
        simpleDateFormat= SimpleDateFormat("yyyy-MM-dd")
    }
}