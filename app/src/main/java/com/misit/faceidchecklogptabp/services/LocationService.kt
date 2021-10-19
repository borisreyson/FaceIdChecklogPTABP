package com.misit.faceidchecklogptabp.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.SplashActivity
import com.misit.faceidchecklogptabp.Utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationService: Service() {
    var TAG ="LocationService"
    lateinit var manager: NotificationManager
    lateinit var getLocation : GetLocation
    lateinit var notificationIntent:Intent
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        notificationIntent = Intent(this, SplashActivity::class.java)

        getLocation = GetLocation(this@LocationService)
        createNotificationChannel()

        super.onCreate()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent.let {
            if(it!=null){
                when(it.action){
                    Constants.SERVICE_START -> showNotification()
                    Constants.SERVICE_STOP -> stopService()
                }
            }else{
                showNotification()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun showNotification() {
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification =
                Notification.Builder(this, Constants.CHANNEL_ID).setContentText("Mengambil Lokasi....!!!").setSmallIcon(
                    R.drawable.abp_blue
                ).setContentIntent(pendingIntent).build()
                doForeground()
                startForeground(Constants.NOTIFICATION_ID, notification)
                Log.d("LoadingServices", "Service Started!!")
        }
    }
    private fun stopService(){
        manager.cancel(Constants.NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "Service Stopped!!")
    }
    private fun createNotificationChannel(){
        Log.d("LoadingServices","CreateChannel")
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
    private fun doForeground(){
        getLocation!!.locationCurrent(this@LocationService)
    }

}