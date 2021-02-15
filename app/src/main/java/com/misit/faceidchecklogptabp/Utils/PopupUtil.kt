package com.misit.faceidchecklogptabp.Utils

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

//Membuat class PopupUtil untuk menampilkan menu loading #8/
object PopupUtil {
    private var dialog: ProgressDialog? = null
    const val SHORT = Toast.LENGTH_SHORT
    const val LONG = Toast.LENGTH_LONG
    fun showMsg(c: Context?, msg: String?, duration: Int) {
        try {
            Toast.makeText(c, msg, duration).show()
        } catch (e: Exception) {
        }
    }

    fun showLoading(context: Context?, title: String?, msg: String?) {
        try {
            dialog = ProgressDialog.show(context, title, msg)
        } catch (e: Exception) {
        }
    }

    fun showProgress(context: Context?, title: String?, msg: String?) {
        try {
            dialog = ProgressDialog(context)
            dialog!!.setTitle(title)
            dialog!!.setMessage(msg)
            dialog!!.isIndeterminate = true
            dialog!!.max = 100
            dialog!!.show()
        } catch (e: Exception) {
        }
    }

    fun updateProgress(progress: Int) {
        try {
            if (dialog != null) dialog!!.progress = progress
        } catch (e: Exception) {
        }
    }

    fun dismissDialog() {
        try {
            if (dialog != null) dialog!!.dismiss()
        } catch (e: Exception) {
        }
    }

    fun alert(context: Context?, msg: String?,
              positif: String?, negatif: String?, listener: DialogInterface.OnClickListener?) {
        try {
            if (context == null || listener == null) throw NullPointerException()
            val alert = AlertDialog.Builder(context)
            alert.setMessage(msg)
            alert.setPositiveButton(positif, listener)
            if (negatif != null) alert.setNegativeButton(negatif, listener) else alert.setCancelable(false)
            alert.show()
        } catch (e: Exception) {
        }
    }
}