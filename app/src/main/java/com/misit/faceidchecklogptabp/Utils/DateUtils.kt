package com.misit.faceidchecklogptabp.Utils

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    val formatDate: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM, yyyy")
    val simpleFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun fmt(dateString:String):String{
        val tanggal = LocalDate.parse(dateString)
        try {
            return tanggal.toString(formatDate)
        }catch (e:Exception){
            return ""
        }
    }
    fun timeFmt(time:Date):String{
        try {
            return simpleFormat.format(time)
        }catch (e:Exception){
            return ""
        }
    }
}