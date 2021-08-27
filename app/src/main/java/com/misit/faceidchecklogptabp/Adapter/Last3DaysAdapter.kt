package com.misit.faceidchecklogptabp.Adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Response.AbsenTigaHariItem
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.SimpleDateFormat

class Last3DaysAdapter(
    private val context: Context?,
    private val listAbsen:MutableList<AbsenTigaHariItem>,
    private val nik:String?,
    private val nama:String?):
    RecyclerView.Adapter<Last3DaysAdapter.MyViewHolder>(){
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("EEEE, d MMMM, yyyy")
    private val layoutInflater: LayoutInflater
    private var simpleDateFormat: SimpleDateFormat? = null
//    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = layoutInflater.inflate(R.layout.last_3_days_absen,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listAbsen.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val absenList = listAbsen[position]
        if(absenList.status=="Masuk"){
            holder.cvRecent.setBackgroundResource(R.color.successColor)
        }else if(absenList.status == "Pulang"){
            holder.cvRecent.setBackgroundResource(R.color.errorColor)
        }
        holder.tvTGL.text = LocalDate.parse(absenList.tanggal).toString(fmt)
        holder.tvNama.text = nama
        holder.tvNik.text = nik
        holder.tvJam.text = absenList.jam
        holder.tvStatus.text = absenList.status
        Glide.with(context!!)
            .load(absenList?.gambar)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.imgFace)
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTGL = itemView.findViewById<View>(R.id.tvTGL) as TextView
        var tvNama = itemView.findViewById<View>(R.id.tvNama) as TextView
        var tvNik = itemView.findViewById<View>(R.id.tvNik) as TextView
        var tvJam = itemView.findViewById<View>(R.id.tvJam) as TextView
        var tvStatus = itemView.findViewById<View>(R.id.tvStatus) as TextView
        var cvRecent = itemView.findViewById<View>(R.id.cvRecent) as CardView
        var imgFace = itemView.findViewById<View>(R.id.imgFace) as ImageView
    }

    init {
        layoutInflater = LayoutInflater.from(context)
    }

}