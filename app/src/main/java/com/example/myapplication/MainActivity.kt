package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

    // Variables para dibujar el óvalo y el mensaje
    private val paint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val ovalRect: RectF = RectF()

    // Callback para dibujar el óvalo en la superficie de la cámara
    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            startCameraPreview(holder)
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            stopCameraPreview()
        }
    }

    // Variable para la vista previa de la cámara
    private var surfaceView: SurfaceView? = null

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCamera()
    }

    private fun stopCamera() {
        // Detener la vista previa de la cámara
        camera?.stopPreview()
        // Liberar la cámara
        camera?.release()
        camera = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Crear SurfaceView para la vista previa de la cámara
        surfaceView = SurfaceView(this)
        surfaceView?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        setContentView(surfaceView)

        // Crear botón "Escanear"
        val buttonScan = Button(this)
        buttonScan.text = "Escanear"
        buttonScan.setOnClickListener {
            toggleScanning()
            openCamera()
        }
        val buttonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        addContentView(buttonScan, buttonParams)
    }

    override fun onResume() {
        super.onResume()
        // Inicializar OpenCV y cargar el clasificador de Haar
        OpenCVLoader.initDebug()
        loadFaceCascade()
    }

    private fun startCameraPreview(holder: SurfaceHolder) {
        camera = Camera.open()
        camera?.setPreviewDisplay(holder)
        camera?.startPreview()
    }

    private fun stopCameraPreview() {
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

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, abrir la cámara
                    openCamera()
                } else {
                    // Permiso denegado, mostrar un mensaje o tomar una acción adecuada
                    Toast.makeText(this, "Permiso de la cámara denegado", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Manejar otros casos de permisos si es necesario
            }
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
}
