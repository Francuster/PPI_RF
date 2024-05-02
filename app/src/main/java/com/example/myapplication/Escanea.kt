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
    private var detecto = false

    private var surfaceView: SurfaceView? = null

    private var timer: CountDownTimer? = null
    private var timerStoppedManual = false

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Configurar la vista de superficie para la vista previa de la cámara
        surfaceView = SurfaceView(this)
        surfaceView?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        addContentView(surfaceView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Agregar un botón para iniciar el escaneo
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

    // Detener y liberar la cámara
    private fun stopCamera() {
        camera?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
        }
        camera = null
    }

    // Iniciar la cámara
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
                Toast.makeText(this, "No se pudo abrir la cámara frontal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Obtener el mejor tamaño de vista previa de la cámara
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

    // Abrir la cámara
    private fun openCamera() {
        startCamera()
    }

    // Verificar permiso de la cámara
    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    // Solicitar permiso de la cámara
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

    // Alternar el escaneo
    private fun toggleScanning() {
        isScanning = !isScanning
        if (isScanning) {
            Toast.makeText(this, "Escaneando...", Toast.LENGTH_SHORT).show()
            startTimer()
        } else {
            stopTimer()
            if (timerStoppedManual) {
                Toast.makeText(this, "Escaneo detenido manualmente", Toast.LENGTH_SHORT).show()
                timerStoppedManual = false // Restablecer la bandera
            }
        }
    }

    // Iniciar temporizador
    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                Toast.makeText(this@Escanea, "Tiempo restante: $secondsRemaining segundos", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish() {
                isScanning = false
                Toast.makeText(this@Escanea, "Tiempo de escaneo agotado", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // Detener temporizador
    private fun stopTimer() {
        runOnUiThread {
            timer?.cancel()
            timerStoppedManual = true
            if (!detecto) {
                Toast.makeText(this@Escanea, "Escaneo detenido manualmente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método llamado cuando se recibe un fotograma de la cámara
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (!isScanning) return

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
            }

            yuvMat.release()

            runOnUiThread {
                // Actualizar la vista previa de la cámara si es necesario
            }
        }.start()

        if (detecto) {
            siguiente()
        }
    }

    // Cargar el clasificador de cascada para la detección de rostros
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

    // Ir a la siguiente actividad cuando se detecta un rostro
    private fun siguiente() {
        val intent = Intent(applicationContext, RegistroExitoso::class.java)
        startActivity(intent)
    }
}
