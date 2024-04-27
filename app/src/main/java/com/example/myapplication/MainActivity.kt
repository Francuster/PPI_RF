package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), Camera.PreviewCallback {

    // Variables
    private var camera: Camera? = null
    private lateinit var cascadeClassifier: CascadeClassifier // Clasificador de Haar para la detección de rostros
    private var isScanning = false

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Inicializar OpenCV y cargar el clasificador de Haar
        OpenCVLoader.initDebug()
        loadFaceCascade()

        // Configurar la vista de la cámara
        val surfaceView = SurfaceView(this)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.width = 1
        layoutParams.height = 1
        addContentView(surfaceView, layoutParams)

        // Agregar botón "Escanear"
        val buttonScan = Button(this)
        buttonScan.text = "Escanear"
        buttonScan.setOnClickListener {
            toggleScanning()
            openCamera()
        }
        val buttonLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        addContentView(buttonScan, buttonLayoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (!isScanning) return

        val parameters = camera?.parameters
        val width = parameters?.previewSize?.width ?: 0
        val height = parameters?.previewSize?.height ?: 0

        val yuvMat = Mat(height + height / 2, width, org.opencv.core.CvType.CV_8UC1)
        yuvMat.put(0, 0, data)

        val rgbaMat = Mat()
        Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21, 4)

        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(rgbaMat, faces, 1.1, 2, 2, Size(150.0, 150.0), Size())

        val facesArray = faces.toArray()
        for (faceRect in facesArray) {
            Imgproc.rectangle(rgbaMat, faceRect.tl(), faceRect.br(), Scalar(255.0, 0.0, 0.0), 3)
        }

        yuvMat.release()
    }

    private fun checkCameraPermission(): Boolean {
        return if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            false
        }
    }

    private fun loadFaceCascade() {
        try {
            val resourceId = R.raw.lbpcascade_frontalface_improved
            val isStream: InputStream = resources.openRawResource(resourceId)
            val cascadeDir: File = cacheDir
            val cascadeFile: File = File(cascadeDir, "lbpcascade_frontalface_improved.xml")
            val os = FileOutputStream(cascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (isStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }
            isStream.close()
            os.close()

            cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
            if (cascadeClassifier.empty()) {
                // Manejar el error al cargar el clasificador de Haar
                throw IOException("El clasificador de Haar está vacío")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        if (isScanning) {
            Toast.makeText(this, "Escaneando...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        if (!checkCameraPermission()) return

        if (camera == null) {
            camera = Camera.open()
            camera?.setPreviewCallback(this)
        }
    }
}
