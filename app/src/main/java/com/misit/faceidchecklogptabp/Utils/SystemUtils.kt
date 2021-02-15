package com.misit.faceidchecklogptabp.Utils

import android.app.ActionBar
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

object SystemUtils{
    fun fullscreen(window:Window,actionBar: ActionBar){
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            actionBar?.hide()
        }
    }
}