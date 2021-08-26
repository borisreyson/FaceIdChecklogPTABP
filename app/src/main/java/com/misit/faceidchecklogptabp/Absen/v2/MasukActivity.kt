package com.misit.faceidchecklogptabp.Absen.v2

import android.app.AlertDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.misit.faceidchecklogptabp.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.misit.faceidchecklogptabp.Absen.v1.MasukActivity
import com.misit.faceidchecklogptabp.Helper.RectOverlay
import com.misit.faceidchecklogptabp.Utils.PrefsUtil
import com.wonderkiln.camerakit.*
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_masuk.*

lateinit var waitingDialog: AlertDialog
private var bitmap: Bitmap?=null
private var niknya:String?=null
lateinit var  rectOverlay:RectOverlay

class MasukActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk2)
        PrefsUtil.initInstance(this)
        MasukActivity.LAT = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LAT,"")
        MasukActivity.LNG = PrefsUtil.getInstance()
            .getStringState(PrefsUtil.CURRENT_LNG,"")
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
            btn_active_camera.visibility= View.GONE
            btn_detect.visibility= View.VISIBLE
        }
        niknya = intent.getStringExtra(MasukActivity.NIK)
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
                runFaceDetector(bitmap1)
            }

            override fun onError(p0: CameraKitError?) {

            }

        })
    }

    private fun runFaceDetector(bitmap: Bitmap?) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        val detector = FaceDetection.getClient(realTimeOpts)

        val result = detector.process(image!!)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                // ...
                for (face in faces) {
                    val bounds = face.boundingBox
                    val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                    val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
                    if (face.smilingProbability != null) {
                        val smileProb = face.smilingProbability
                    }
                    rectOverlay = RectOverlay(graphic_overlay,bounds)
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
    }
}

