package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.hardware.Camera
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class Escanea : AppCompatActivity(), Camera.PreviewCallback {

    private var camera: Camera? = null
    private lateinit var cascadeClassifier: CascadeClassifier
    private var isScanning = false

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    private val paint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val rect: RectF = RectF()

    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = SurfaceView(this)
        surfaceView?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        addContentView(surfaceView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

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
        OpenCVLoader.initDebug()
        loadFaceCascade()
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCamera()
    }

    private fun stopCamera() {
        camera?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
        }
        camera = null
    }

    private fun startCamera() {
        if (!checkCameraPermission()) return

        if (camera == null) {
            val cameraCount = Camera.getNumberOfCameras()
            var frontCameraId = -1
            for (i in 0 until cameraCount) {
                val info = Camera.CameraInfo()
                Camera.getCameraInfo(i, info)
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCameraId = i
                    break
                }
            }
            if (frontCameraId != -1) {
                camera = Camera.open(frontCameraId)
                camera?.setPreviewCallback(this)

                // Configure camera parameters
                val parameters = camera?.parameters

                // Set camera preview surface
                try {
                    camera?.setDisplayOrientation(90)
                    camera?.setPreviewDisplay(surfaceView?.holder)

                    // Find the best preview size for the camera
                    val bestSize = getBestPreviewSize(parameters)
                    parameters?.setPreviewSize(bestSize.width.toInt(), bestSize.height.toInt())

                    camera?.parameters = parameters
                    camera?.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "No se pudo abrir la cámara frontal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBestPreviewSize(parameters: Camera.Parameters?): org.opencv.core.Size {
        var bestSize = parameters?.supportedPreviewSizes?.get(0)?.let {
            org.opencv.core.Size(it.width.toDouble(), it.height.toDouble())
        } ?: org.opencv.core.Size(640.0, 480.0)

        val targetRatio = bestSize.width / bestSize.height

        for (size in parameters?.supportedPreviewSizes ?: emptyList()) {
            val ratio = size.width.toDouble() / size.height.toDouble()
            if (Math.abs(ratio - targetRatio) < Math.abs(bestSize.width / bestSize.height - targetRatio)) {
                bestSize = org.opencv.core.Size(size.width.toDouble(), size.height.toDouble())
            }
        }

        return bestSize
    }





    private fun openCamera() {
        startCamera()
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
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de la cámara denegado", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Handle other permission cases if necessary
            }
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        if (isScanning) {
            Toast.makeText(this, "Escaneando...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Escaneo terminado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        // Verificar si el escaneo está activado
        if (!isScanning) return

        // Crear un nuevo hilo para el procesamiento de la imagen
        Thread {
            // Obtener el tamaño de la imagen de la vista previa de la cámara
            val parameters = camera?.parameters
            val width = parameters?.previewSize?.width ?: 0
            val height = parameters?.previewSize?.height ?: 0

            // Crear una matriz para los datos de la imagen en formato YUV
            val yuvMat = Mat(height + height / 2, width, org.opencv.core.CvType.CV_8UC1)
            yuvMat.put(0, 0, data)

            // Crear una matriz para la imagen en formato RGBA
            val rgbaMat = Mat()
            // Convertir la imagen de YUV a RGBA
            Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21, 4)

            // Detectar rostros en la imagen
            val faces = MatOfRect()
            cascadeClassifier.detectMultiScale(rgbaMat, faces, 1.1, 2, 2, Size(150.0, 150.0), Size())

            // Obtener la lista de rostros detectados
            val facesArray = faces.toArray()
            // Iterar sobre cada rostro detectado y dibujar un rectángulo alrededor de él
            for (faceRect in facesArray) {
                // Dibujar el rectángulo con las coordenadas ajustadas
                Imgproc.rectangle(rgbaMat, Point(faceRect.x.toDouble(), faceRect.y.toDouble()), Point((faceRect.x + faceRect.width).toDouble(), (faceRect.y + faceRect.height).toDouble()), Scalar(255.0, 0.0, 0.0), 3)
                // Mostrar un mensaje indicando que se detectó un rostro
                runOnUiThread {
                    Toast.makeText(this, "Rostro detectado", Toast.LENGTH_SHORT).show()
                }
            }

            // Liberar la matriz de datos de YUV
            yuvMat.release()

            // Actualizar la vista previa de la cámara en el hilo principal
            runOnUiThread {
                // Aquí puedes actualizar la vista previa de la cámara si es necesario
            }
        }.start()
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
                throw IOException("El clasificador de Haar está vacío")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
