package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.CountDownTimer
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
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.io.ByteArrayOutputStream

import java.util.concurrent.TimeUnit

class Escanea : AppCompatActivity(), Camera.PreviewCallback {

    private var camera: Camera? = null
    private lateinit var cascadeClassifier: CascadeClassifier
    private lateinit var buttonScan: Button
    private var isScanning = false
    private var detecto = false
    private var surfaceView: SurfaceView? = null
    private var timer: CountDownTimer? = null
    private var timeUpToastShown = false



    //variables del tiempo en las request
    private var lastRequestTimeMillis = 0L
    private val requestIntervalMillis = 1000L // 1 segundo
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

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

        buttonScan = Button(this)
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

                val parameters = camera?.parameters

                try {
                    camera?.setDisplayOrientation(90)
                    camera?.setPreviewDisplay(surfaceView?.holder)

                    val bestSize = getBestPreviewSize(parameters)
                    parameters?.setPreviewSize(bestSize.width.toInt(), bestSize.height.toInt())

                    camera?.parameters = parameters
                    camera?.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                showToastOnUiThread("No se pudo abrir la cámara frontal")
            }
        }
    }

    private fun getBestPreviewSize(parameters: Camera.Parameters?): Size {
        var bestSize = parameters?.supportedPreviewSizes?.get(0)?.let {
            Size(it.width.toDouble(), it.height.toDouble())
        } ?: Size(640.0, 480.0)

        val targetRatio = bestSize.width / bestSize.height

        for (size in parameters?.supportedPreviewSizes ?: emptyList()) {
            val ratio = size.width.toDouble() / size.height.toDouble()
            if (Math.abs(ratio - targetRatio) < Math.abs(bestSize.width / bestSize.height - targetRatio)) {
                bestSize = Size(size.width.toDouble(), size.height.toDouble())
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
                    showToastOnUiThread("Permiso de la cámara denegado")
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
        updateButtonState()
        if (isScanning) {
            startTimer()
            showToastOnUiThread("Escaneando 30 segundos...")
            buttonScan.text = "Detener Escaneo"
        } else {
            stopTimer()
            handleScanTimeout()
        }
    }

    private fun updateButtonState() {
        buttonScan.text = if (isScanning) "Detener Escaneo" else "Escanear"
    }

    private fun handleScanTimeout() {
        isScanning = false
        stopCamera()
        updateButtonState()
        if (!detecto && !timeUpToastShown) {
            showToastOnUiThread("Tiempo de escaneo agotado")
            timeUpToastShown = true
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if (!detecto && secondsRemaining <= 3) {
                    showToastOnUiThread("Tiempo restante: $secondsRemaining segundos")
                }
            }
            override fun onFinish() {
                handleScanTimeout()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (!isScanning) return

        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastRequestTimeMillis < requestIntervalMillis) {
            // Si el intervalo entre solicitudes no ha pasado aún, salir sin enviar otra solicitud
            return
        }

        Thread {
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
            if (facesArray.isNotEmpty() || detecto) {
                timer?.cancel()
                detecto = true
                yuvMat.release()
                runOnUiThread {
                    stopTimer()
                    if (facesArray.isNotEmpty()) {
                        val faceMat = extractFaceFrame(rgbaMat, facesArray[0])
                        enviarMatrizComoHTTPRequest(faceMat)
                        lastRequestTimeMillis = currentTimeMillis // Actualizar el tiempo de la última solicitud
                    } else {
                        showToastOnUiThread("No se detectó ningún rostro")
                    }
                }
                return@Thread
            }
            yuvMat.release()
        }.start()
    }


    private fun extractFaceFrame(rgbaMat: Mat, faceRect: Rect): Mat {
        // Obtener las coordenadas de la cara
        val x = faceRect.x
        val y = faceRect.y
        val width = faceRect.width
        val height = faceRect.height

        // Crear un rectángulo que delimita la cara en la imagen original
        val faceROI = Rect(x, y, width, height)

        // Extraer la región de interés (ROI) que contiene la cara
        val faceMat = Mat(rgbaMat, faceROI)

        // Clonar la región de interés para evitar problemas de memoria
        return faceMat.clone()
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

    private fun siguiente() {
        val intent = Intent(applicationContext, RegistroExitoso::class.java)
        startActivity(intent)
    }

    private fun showToastOnUiThread(message: String) {
        runOnUiThread {
            Toast.makeText(this@Escanea, message, Toast.LENGTH_SHORT).show()
        }
    }
}



val client = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .writeTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

private fun enviarMatrizComoHTTPRequest(faceMat: Mat) {
    // Convertir la matriz de OpenCV a un formato de imagen compatible con HTTP (por ejemplo, JPEG)
    val byteStream = ByteArrayOutputStream()
    val imageMat = MatOfByte()
    Imgcodecs.imencode(".jpg", faceMat, imageMat)
    byteStream.write(imageMat.toArray())

    val requestBody = byteStream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://tu_servidor.com/api/upload")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            // Manejar la respuesta del servidor aquí
            if (response.isSuccessful) {
                // La solicitud fue exitosa
                val responseBody = response.body?.string()
                // Acà deberia extraer los datos que me devuevle la solicitud y pasarlos a una clase
            } else {
                // La solicitud no fue exitosa
                // Manejar el error
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            // Manejar el fallo de la solicitud aquí
            e.printStackTrace()
        }
    })
}
