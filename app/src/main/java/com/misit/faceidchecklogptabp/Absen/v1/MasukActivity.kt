package com.misit.faceidchecklogptabp.Absen.v1

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.PointF
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
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
import kotlinx.android.synthetic.main.activity_masuk.*
import kotlinx.android.synthetic.main.activity_masuk.btnBack
import kotlinx.android.synthetic.main.activity_masuk.btnToggle
import kotlinx.android.synthetic.main.activity_masuk.btn_active_camera
import kotlinx.android.synthetic.main.activity_masuk.btn_detect
import kotlinx.android.synthetic.main.activity_masuk.camera_view
import kotlinx.android.synthetic.main.activity_masuk.graphic_overlay
import kotlinx.android.synthetic.main.activity_pulang.*
import okhttp3.MediaType
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

class MasukActivity : AppCompatActivity() {

    lateinit var waitingDialog: AlertDialog
    private var bitmap:Bitmap?=null
    private var niknya:String?=null
    lateinit var  rectOverlay:RectOverlay
    lateinit var mouthPos:MutableList<PointF>
//    private var mLocationManager : LocationManager?=null
//    private var mLocation : Location?= null
//    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    lateinit var leftEyPos:MutableList<PointF>
    lateinit var leftEarPos:PointF
    lateinit var rightEarPos:PointF
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk)
//        SystemUtils.fullscreen(window,actionBar!!)
        PrefsUtil.initInstance(this)
        LAT = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LAT,"")
        LNG = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LNG,"")

//        Toasty.info(this,"Lat : ${LAT}, Lng : ${LNG}",Toasty.LENGTH_LONG).show()

        mouthPos = ArrayList()
        leftEyPos = ArrayList()

        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("Please Wait...")
            .setCancelable(false)
            .build()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        }
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
            btn_detect.visibility=View.VISIBLE
        }

        niknya = intent.getStringExtra(NIK)
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
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adViewIndex)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                Log.d("errorCode",errorCode.toString())
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
        var fileUri = RequestBody.create(MediaType.parse("image/*"),file)
        //Log.d("fileUri",fileUri.toString())
        var niK = RequestBody.create(MultipartBody.FORM, niknya!!)
        var tanggal = RequestBody.create(MultipartBody.FORM, tgl)
        var jam_absen = RequestBody.create(MultipartBody.FORM, pukul)
        var status_absen = RequestBody.create(MultipartBody.FORM, "Masuk")
        var idnya = RequestBody.create(MultipartBody.FORM, id.toString())
        var lupa_absen = RequestBody.create(MultipartBody.FORM,"")
        var lat = RequestBody.create(MultipartBody.FORM, LAT)
        var lng = RequestBody.create(MultipartBody.FORM, LNG)
        var fileToUpload = MultipartBody.Part.createFormData("fileToUpload",file.name,fileUri)
        try {
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
                    Toasty.info(this@MasukActivity,"$t", Toasty.LENGTH_SHORT).show()
                }
                override fun onResponse(
                    call: Call<ImageResponse?>,
                    response: Response<ImageResponse?>
                ) {
                    val imageResponse = response.body()
                    if (imageResponse != null) {
                        if(imageResponse.tidak_dikenal!=null){
                            if(imageResponse.tidak_dikenal){
                                file.delete()
                                btn_detect.visibility=View.VISIBLE
                                btnBack.visibility=View.GONE
                                waitingDialog.dismiss()
                                camera_view.start()
                                graphic_overlay.clear()
                                Toasty.info(this@MasukActivity,"Wajah Tidak Dikenal!", Toasty.LENGTH_SHORT).show()
                            }else{
                                file.delete()
                                btn_detect.visibility=View.GONE
                                btnBack.visibility=View.VISIBLE
                                waitingDialog.dismiss()
                                Toasty.info(this@MasukActivity,"Wajah di kenali, Absen di daftar!", Toasty.LENGTH_SHORT).show()
                            }
                        }else{
                            file.delete()
                            btn_detect.visibility=View.VISIBLE
                            btnBack.visibility=View.GONE
                            waitingDialog.dismiss()
                            camera_view.start()
                            graphic_overlay.clear()
                            Toasty.info(this@MasukActivity,"Wajah Tidak Dikenal!", Toasty.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }catch (e:Exception){
            Log.e("ErrorMasuk","Cek Masuk :${e.message}")
            saveImageToInternalStorage(bitmap,id)
        }catch (e:InterruptedException){
            Log.e("ErrorMasuk","Cek Masuk :${e.message}")
            saveImageToInternalStorage(bitmap,id)
        }

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
                Toast.makeText(this@MasukActivity,e.message, Toast.LENGTH_SHORT).show()
            }
    }

//    fun runFaceDetector(bitmap: Bitmap?){
//        val image = FirebaseVisionImage.fromBitmap(bitmap!!)
//
//        val options  = FirebaseVisionFaceDetectorOptions.Builder()
//            .setPerformanceMode(
//                FirebaseVisionFaceDetectorOptions.ACCURATE)
//            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//            .setClassificationMode(
//                FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
//            .enableTracking()
//            .build()
//        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
//
//        detector.detectInImage(image)
//            .addOnSuccessListener {
////                    result -> progressResult(result)
//            }
//            .addOnFailureListener{
//                    e -> Toast.makeText(this@MasukActivity,e.message, Toast.LENGTH_SHORT).show()
//            }
//
//    }
fun progressResult(result:List<Face>){
    var count=0
    var id=0
    var smileProb=0f
    mouthPos.clear()
    leftEyPos.clear()
    for (face in result){

        val bounds = face.boundingBox
        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
        // nose available):
        val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
        val rightEar = face.getLandmark(FaceLandmark.LEFT_EAR)
        leftEar?.let {
            leftEarPos = leftEar.position
        }
        rightEar?.let {
            rightEarPos = rightEar.position
        }
        val mouth_bottom = face.getLandmark(FaceContour.UPPER_LIP_BOTTOM)
        mouth_bottom?.let {
            //mouth_bottomPos = mouth_bottom!!.position!!

        }
        // If contour detection was enabled:
        val leftEyeContour = face.getContour(FaceContour.LEFT_EYE).points
        leftEyeContour.let {
            leftEyPos.addAll(it)
        }
        val rightEyeContour = face.getContour(FaceContour.RIGHT_CHEEK).points
        val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM).points
        upperLipBottomContour.let {
            mouthPos.addAll(it)
        }
        // If classification was enabled:
        if (face.smilingProbability != null) {
            smileProb = face.smilingProbability
        }
        if (face.rightEyeOpenProbability !=null) {
            val rightEyeOpenProb = face.rightEyeOpenProbability
        }

        // If face tracking was enabled:
        if (face.trackingId !=null) {
            id = face.trackingId
//                saveImageToInternalStorage(bitmap,id)
        }
        rectOverlay = RectOverlay(graphic_overlay,bounds)

        count++
    }
    if(count>0){
        //btnSave.visibility= View.VISIBLE
        //btnSave.visibility= View.VISIBLE

        waitingDialog.dismiss()
        if(mouthPos.isNotEmpty()){
            saveImageToInternalStorage(bitmap,id)
            graphic_overlay.add(rectOverlay)
//                Toasty.info(this@PulangActivity," LEFT: ${leftEarPos.toString()} " +
//                        "|" +
//                        " RIGHT: ${rightEarPos.toString()}", Toasty.LENGTH_SHORT).show()
//                Toasty.info(this@PulangActivity," ${leftEarPos.toString()}", Toasty.LENGTH_SHORT).show()

//                Toast.makeText(this@PulangActivity,String.format(" ${leftEarPos}"),Toast.LENGTH_LONG).show()
        }else{
            Toasty.error(this@MasukActivity,
                "Wajah Tidak Terdeteksi! " +
                        "Harap Membuka Masker atau " +
                        "Pelindung Wajah dan " +
                        "Tidak Boleh Lebih dari satu Wajah .!")
                .show()

            btn_active_camera.visibility=View.VISIBLE
            btn_detect.visibility=View.GONE
            waitingDialog.dismiss()
        }

        //Toast.makeText(this,"$uri",Toast.LENGTH_SHORT).show()
    }else{
        waitingDialog.dismiss()
        Toasty.error(this,
            "Wajah Tidak Terdeteksi! " +
                    "Harap Membuka Masker atau " +
                    "Pelindung Wajah dan " +
                    "Tidak Boleh Lebih dari satu Wajah .!")
            .show()

        btn_active_camera.visibility=View.VISIBLE
        btn_detect.visibility=View.GONE
//            Toasty.info(this@PulangActivity,"Wajah Tidak Terdeteksi", Toasty.LENGTH_SHORT).show()
    }

}

    companion object{
        var NIK = "NIK"
        var LAT = "0"
        var LNG = "0"
    }
}
