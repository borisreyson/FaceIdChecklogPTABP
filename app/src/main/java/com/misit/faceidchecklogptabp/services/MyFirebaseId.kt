package com.misit.faceidchecklogptabp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.misit.faceidchecklogptabp.IndexActivity
import com.misit.faceidchecklogptabp.MainActivity
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import kotlin.random.Random

class MyFirebaseId : FirebaseMessagingService() {
    lateinit var notificationManager: NotificationManager
    lateinit var builder : NotificationCompat.Builder
    var channelId = "com.misit.faceidchecklogptabp.services"


    override fun onCreate() {
        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,true)){
            USERNAME = PrefsUtil.getInstance().getStringState(PrefsUtil.USER_NAME,"")
            NAMA_LENGKAP = PrefsUtil.getInstance().getStringState(PrefsUtil.NAMA_LENGKAP,"")
            DEPARTMENT = PrefsUtil.getInstance().getStringState(PrefsUtil.DEPT,"")
            SECTON = PrefsUtil.getInstance().getStringState(PrefsUtil.SECTION,"")
            LEVEL = PrefsUtil.getInstance().getStringState(PrefsUtil.LEVEL,"")
        }
        super.onCreate()
    }
    override fun onMessageReceived(p0: RemoteMessage) {
        if (p0.data.isNotEmpty()) {
            Log.d(TAG, "Message data : " + p0.data)
        }
        val data: Map<String, String> = p0.data
        val teks = data["text"]
        val title = data["title"]
        val tipe = data["tipe"]
        val urlUpdate = data["urlUpdate"]
        val nik = data["nik"]!!.toInt()
        notif(title,teks,tipe,nik,urlUpdate)
    }
    private fun notif(title: String?, body: String?,tipe:String?,nik:Int,urlUpdate:String?){

        if(tipe=="belum_absen"){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra(MainActivity.TIPE,tipe)
            showNotification(title,body,intent,"Belum Absen")
        }else if(tipe=="terlambat"){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra(MainActivity.TIPE,tipe)
            channelId= nik.toString()
            showNotification(title,body,intent, nik.toString())
        }else if(tipe=="update"){
            var intent = Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlUpdate!!));
            channelId= nik.toString()
            showNotification(title,body,intent, nik.toString())
        }

    }
    private fun showNotification(title: String?, body: String?,intent: Intent,group:String) {
        var pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        var dSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            var oChannel = NotificationChannel(channelId,title,NotificationManager.IMPORTANCE_HIGH)
            oChannel.enableVibration(true)
            oChannel.enableLights(true)

            notificationManager.createNotificationChannel(oChannel)
            builder = NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.drawable.abp_white)
                .setColor(R.drawable.abp_blue)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(dSoundUri)
                .setAutoCancel(true)
                .setGroup(group)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)

        }else{
                builder = NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.drawable.abp_white)
                .setColor(R.drawable.abp_blue)
                .setContentTitle(title)
                .setContentText(body)
                    .setGroup(group)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(dSoundUri)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
        }
        var idRand = (0 until 100).random()
        notificationManager.notify(idRand,builder.build())

    }
    companion object{
        var USERNAME = "username"
        var DEPARTMENT="department"
        var SECTON="section"
        var LEVEL="level"
        var NotifNik="Nik"
        var NAMA_LENGKAP = "nama_lengkap"
        private  var TAG="MyFirebaseMessagingService"
    }
}