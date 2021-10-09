package com.misit.faceidchecklogptabp.Absen.v1

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Helper.RectOverlay
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Response.ImageResponse
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import com.wonderkiln.camerakit.*
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_lupa_masuk.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LupaMasukActivity : AppCompatActivity() {

    lateinit var waitingDialog: AlertDialog
    private var bitmap: Bitmap?=null
    private var niknya:String?=null
    private var tglLupaAbsen:String?=null
    lateinit var  rectOverlay: RectOverlay
    lateinit var mouthPos:MutableList<PointF>
    lateinit var leftEarPos: PointF
    lateinit var leftEyPos:MutableList<PointF>
    lateinit var rightEarPos: PointF
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_masuk)
        PrefsUtil.initInstance(this)
        LAT = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LAT,"")
        LNG = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LNG,"")

//        Toasty.info(this,"Lat : ${MasukActivity.LAT}, Lng : ${MasukActivity.LNG}",Toasty.LENGTH_LONG).show()

        mouthPos = ArrayList()
        leftEyPos = ArrayList()
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("Please Wait...")
            .setCancelable(false)
            .build()
        btn_detect.setOnClickListener{
            camera_view.captureImage()
            btn_detect.isEnabled=false
            btn_detect.isClickable=false
            //btnSave.visibility= View.GONE

        }
        btn_active_camera.setOnClickListener {
            btn_detect.isEnabled=true
            btn_detect.isClickable=true
            camera_view.start()
            graphic_overlay.clear()
            btn_active_camera.visibility=View.GONE
            btn_detect.visibility= View.VISIBLE
        }

        PrefsUtil.initInstance(this)
        if(PrefsUtil.getInstance().getBooleanState(PrefsUtil.IS_LOGGED_IN,true)) {
            niknya = PrefsUtil.getInstance().getStringState(PrefsUtil.NIK,"")
        }
        tglLupaAbsen = intent.getStringExtra(TGL_LUPA_ABSEN)

        btnToggle.setOnClickListener {
            //            if(camera_view.isFacingBack) {
//                btnToggle.text="Camera Belakang"
//            }else{
//                btnToggle.text="Camera Depan"
//            }
            camera_view.toggleFacing()
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        camera_view.addCameraKitListener(object: CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {

            }

            override fun onEvent(p0: CameraKitEvent?) {
            }

            override fun onImage(p0: CameraKitImage?) {

                waitingDialog.show()

                bitmap = p0!!.bitmap
                var bitmap1 = Bitmap.createScaledBitmap(bitmap!!,camera_view.width,camera_view.height,false)

                camera_view.stop()
                runFaceDetectorNew(bitmap1)
            }

            override fun onError(p0: CameraKitError?) {

            }

        })
    }
    override fun onResume() {
        super.onResume()
        camera_view.start()
        if(camera_view.isFacingBack){
            camera_view.toggleFacing()
//            btnToggle.text="Camera Belakang"
        }else{
//            btnToggle.text="Camera Depan"
        }
    }

    override fun onPause() {
        super.onPause()
        camera_view.stop()
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap?,id:Int): Uri {
        var waktu = Date()
        val cal = Calendar.getInstance()
        cal.time = waktu
        val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var tgl = "${simpleFormat.format(cal.time)}"
        var jam = "${cal.get(Calendar.HOUR_OF_DAY)}${cal.get(Calendar.MINUTE)}${cal.get(Calendar.SECOND)}"
        var pukul = "${cal.get(Calendar.HOUR_OF_DAY)}:${cal.get(Calendar.MINUTE)}:${cal.get(Calendar.SECOND)}"
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, niknya+"_Masuk_${tgl}__${jam}.jpg")
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            //var uri = Uri.parse(file.absolutePath)
            // Compress bitmap
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            // Flush the stream
            stream.flush()
            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
        var uri = Uri.parse(file.absolutePath)
        var fileUri = RequestBody.create("image/*".toMediaTypeOrNull(),file)
        //Log.d("fileUri",fileUri.toString())
        var niK = RequestBody.create(MultipartBody.FORM, niknya!!)
        var tanggal = RequestBody.create(MultipartBody.FORM, tglLupaAbsen!!)
        var jam_absen = RequestBody.create(MultipartBody.FORM, pukul)
        var status_absen = RequestBody.create(MultipartBody.FORM, "Masuk")
        var idnya = RequestBody.create(MultipartBody.FORM, id.toString())
        var lat = RequestBody.create(MultipartBody.FORM, LAT)
        var lng = RequestBody.create(MultipartBody.FORM, LNG)
        var lupa_absen = RequestBody.create(MultipartBody.FORM,"Lupa Absen Masuk")
        var fileToUpload = MultipartBody.Part.createFormData("fileToUpload",file.name,fileUri)


        //API
        val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
        val call = apiEndPoint.uploadImage(
            fileToUpload,
            niK,
            tanggal,
            jam_absen,
            status_absen,
            idnya,lupa_absen,lat,lng)
        call.enqueue(object : Callback<ImageResponse?> {
            override fun onFailure(call: Call<ImageResponse?>, t: Throwable) {
                Toasty.info(this@LupaMasukActivity,"$t", Toasty.LENGTH_SHORT).show()
            }
            override fun onResponse(
                call: Call<ImageResponse?>,
                response: Response<ImageResponse?>
            ) {
                val imageResponse = response.body()
                if (imageResponse != null) {
                    if(imageResponse.tidak_dikenal!=null) {
                        if (imageResponse.tidak_dikenal) {
                            Log.d("TIDAK",imageResponse.tidak_dikenal.toString())
                            file.delete()
                            btn_detect.visibility = View.VISIBLE
                            btnBack.visibility = View.GONE
                            waitingDialog.dismiss()
                            camera_view.start()
                            graphic_overlay.clear()
                            Toasty.info(
                                this@LupaMasukActivity,
                                "Wajah Tidak Dikenal!",
                                Toasty.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d("TIDAK",imageResponse.tidak_dikenal.toString())
                            file.delete()
                            btn_detect.visibility = View.GONE
                            btnBack.visibility = View.VISIBLE
                            waitingDialog.dismiss()
                            Toasty.info(
                                this@LupaMasukActivity,
                                "Wajah di kenali, Absen di daftar!",
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        Log.d("TIDAK",imageResponse.tidak_dikenal.toString())
                        file.delete()
                        btn_detect.visibility = View.VISIBLE
                        btnBack.visibility = View.GONE
                        waitingDialog.dismiss()
                        camera_view.start()
                        graphic_overlay.clear()
                        Toasty.info(
                            this@LupaMasukActivity,
                            "Wajah Tidak Dikenal!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
        //API
        return uri
    }
    private fun runFaceDetectorNew(bitmap: Bitmap?) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        val detector = FaceDetection.getClient(realTimeOpts)

        val result = detector.process(image!!)
            .addOnSuccessListener { result ->
                // Task completed successfully
                // ...
                progressResult(result)

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                Toast.makeText(this@LupaMasukActivity,e.message, Toast.LENGTH_SHORT).show()
            }
    }
    fun progressResult(result:List<Face>){
        var count=0
        var id=0
        for (face in result){
            val bounds = face.boundingBox

            val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
            leftEar?.let {
                //val leftEarPos = leftEar.position
            }

            if (face.smilingProbability != null) {
            }
            if (face.rightEyeOpenProbability != null) {
            }
            val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM).points

            upperLipBottomContour.let {
                mouthPos.addAll(it)
            }

            if (face.trackingId!= null) {
                id=face.trackingId
            }

            rectOverlay = RectOverlay(graphic_overlay,bounds)
//            graphic_overlay.add(rectOverlay)
            count++
        }
        if(count>0){
            //btnSave.visibility= View.VISIBLE
            //Toast.makeText(this,"$uri",Toast.LENGTH_SHORT).show()
            waitingDialog.dismiss()
            if(mouthPos.isNotEmpty()){
                saveImageToInternalStorage(bitmap,id)
                graphic_overlay.add(rectOverlay)
//                Toast.makeText(this@PulangActivity,String.format(" ${mouth_bottomPos}"),Toast.LENGTH_LONG).show()
            }else{
                Toasty.error(this@LupaMasukActivity,
                    "Wajah Tidak Terdeteksi! " +
                            "Harap Membuka Masker atau " +
                            "Pelindung Wajah dan " +
                            "Tidak Boleh Lebih dari satu Wajah .!")
                    .show()
                btn_active_camera.visibility=View.VISIBLE
                btn_detect.visibility=View.GONE
                waitingDialog.dismiss()


            }
        }else{
            waitingDialog.dismiss()
            Toasty.error(this@LupaMasukActivity,
                "Wajah Tidak Terdeteksi! " +
                        "Harap Membuka Masker atau " +
                        "Pelindung Wajah dan " +
                        "Tidak Boleh Lebih dari satu Wajah .!")
                .show()
//            waitingDialog.dismiss()
            btn_active_camera.visibility=View.VISIBLE
            btn_detect.visibility=View.GONE
//            Toasty.info(this@MasukActivity,"Wajah Tidak Terdeteksi", Toasty.LENGTH_SHORT).show()
        }
    }

    companion object{
        var NIK = "NIK"
        var TGL_LUPA_ABSEN = "TGL_LUPA_ABSEN"
        var LAT = ""
        var LNG = ""
    }
}
