package com.misit.faceidchecklogptabp.Utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.JobIntentService
import com.misit.faceidchecklogptabp.HomeActivity
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.SplashActivity
import com.misit.faceidchecklogptabp.services.JobServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapUtilsService:Service() {
    lateinit var mapUtils :MapAreaUtils
    lateinit var manager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        createNotificationChannel()
        Log.d("JobScheduler","On Create")
        mapUtils = MapAreaUtils()
        PrefsUtil.initInstance(this@MapUtilsService)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,false)){
            COMPANY = PrefsUtil.getInstance().getStringState("PERUSAHAAN","")
            NIK = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
        }
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("JobScheduler","onStartCommand")
        intent.let {
            if(it!=null){
                when(it.action){
                    Constants.SERVICE_START -> showNotification()
                    Constants.SERVICE_STOP -> serviceStop()
                }
            }else{
                showNotification()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
    private fun showNotification(){
        val notificationIntent = Intent(this, SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0)
        val notification = Notification.Builder(this, Constants.CHANNEL_ID).setContentText("Mengambil data....!!!").setSmallIcon(
                R.drawable.abp_white
            ).setContentIntent(pendingIntent).build()
        GlobalScope.launch {
            mapUtils.getMapArea(this@MapUtilsService,COMPANY,NIK)
//            processWork(this@MapUtilsService,"onStartCommand")
            startForeground(Constants.NOTIFICATION_ID, notification)
        }
    }
    companion object{
        var COMPANY="COMPANY"
        var NIK="NIK"
    }

    private fun serviceStop(){
//        sendMessageToActivity("fgTokenService")
        manager.cancel(Constants.NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        Log.d("JobScheduler", "Service Stopped!!")
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Saving Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }

    }
    private fun processWork(c: Context, counter:String){
        Log.d("JobScheduler","processWork")
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var rCode = (100..1000).random()
        ConfigUtil.showNotification(applicationContext,"Maps Utils Service","Notification ${counter}",intent,rCode,"MapUtils")
    }
}