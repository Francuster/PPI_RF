package com.example.myapplication.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CamaraParaRegistroRrHhActivity : AppCompatActivity(), Camera.PreviewCallback {

    private var camera: Camera? = null
    private lateinit var cascadeClassifier: CascadeClassifier
    private lateinit var buttonScan: Button
    private var isScanning = false
    private var detecto = false
    private var surfaceView: SurfaceView? = null
    private var timer: CountDownTimer? = null
    private var timeUpToastShown = false
    private var ovalFrameView: View? = null // Vista del marco ovalado

    //variables del tiempo en las request
    private var lastRequestTimeMillis = 0L
    private val requestIntervalMillis = 15000L // 1000=1 segundo

    //FUNCIONES
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupUI()
        OpenCVLoader.initDebug()
        loadFaceCascade()
    }

    private fun setupUI() {
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

        drawOvalFrame()
    }

    //metodo para dibujar el ovalo en la vista previa de la camara
    private fun drawOvalFrame() {
        val ovalPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        val frameView = object : View(this) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)

                // Obtener las dimensiones de la pantalla
                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels.toFloat()
                val screenHeight = displayMetrics.heightPixels.toFloat()

                // Calcular las coordenadas del óvalo para que esté centrado en la pantalla
                val ovalWidth = screenWidth * 0.8f // Ancho relativo del óvalo
                val ovalHeight = screenHeight * 0.8f // Altura relativa del óvalo
                val left = (screenWidth - ovalWidth) / 2 // Coordenada X izquierda del óvalo
                val top = (screenHeight - ovalHeight) / 2 // Coordenada Y superior del óvalo
                val right = left + ovalWidth // Coordenada X derecha del óvalo
                val bottom = top + ovalHeight // Coordenada Y inferior del óvalo

                // Dibujar el óvalo en el lienzo
                canvas.drawOval(left, top, right, bottom, ovalPaint)
            }
        }

        // Añadir la vista del óvalo al FrameLayout de la actividad
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        addContentView(frameView, layoutParams)
        ovalFrameView = frameView // Asignar la vista del óvalo para referencia futura
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
                showToast("No se pudo abrir la cámara frontal")
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
                    showToast("Permiso de la cámara denegado")
                }
                return
            }
            else -> {
                // Manejar otros permisos si es necesario
            }
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        updateButtonState()
        if (isScanning) {
            startTimer()
            showToast("Escaneando 30 segundos...")
        } else {
            stopTimer()
            showToast("Escaneo detenido manualmente")
        }
    }

    // Cambia el estado del botón
    private fun updateButtonState() {
        buttonScan.text = if (isScanning) "Detener Escaneo" else "Escanear"
    }

    private fun handleScanTimeout() {
        isScanning = false
        updateButtonState()
        if (!detecto && !timeUpToastShown) {
            showToast("Tiempo de escaneo agotado")
            timeUpToastShown = true
        }
    }


    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if (!detecto && (secondsRemaining <= 3 && secondsRemaining > 2 || secondsRemaining <= 15 && secondsRemaining > 14)) { // Manejar tiempo que se muestra en toast
                    showToast("Tiempo restante: $secondsRemaining segundos")
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

    // Método para mostrar un Toast en el hilo principal
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@CamaraParaRegistroRrHhActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Método principal de detección de rostros en la vista previa de la cámara
    override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
        // Verificar si el escaneo está activo
        if (!isScanning) return

        // Obtener el tiempo actual
        val currentTimeMillis = System.currentTimeMillis()

        // Verificar si ha pasado el intervalo entre solicitudes
        if (currentTimeMillis - lastRequestTimeMillis < requestIntervalMillis) {
            // Si el intervalo entre solicitudes no ha pasado aún, salir sin enviar otra solicitud
            return
        }

        // Iniciar un hilo para procesar el fotograma de vista previa
        Thread {
            // Obtener los parámetros de la cámara
            val parameters = camera?.parameters
            val width = parameters?.previewSize?.width ?: 0
            val height = parameters?.previewSize?.height ?: 0

            // Crear una matriz para la vista previa YUV
            val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
            yuvMat.put(0, 0, data)

            // Convertir la matriz YUV a una matriz RGBA
            val rgbaMat = Mat()
            Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21, 4)

            // Detectar rostros en la matriz RGBA
            val faces = MatOfRect()
            cascadeClassifier.detectMultiScale(rgbaMat, faces, 1.1, 2, 2, Size(150.0, 150.0), Size())

            // Verificar si se detectaron rostros o si ya se ha detectado un rostro
            if (faces.toArray().isNotEmpty() || detecto) {
                // Cancelar el temporizador
                timer?.cancel()

                // Marcar que se ha detectado un rostro
                detecto = true

                // Liberar la matriz YUV
                yuvMat.release()

                // Ejecutar en el subproceso de interfaz de usuario principal
                runOnUiThread {
                    // Detener el temporizador
                    stopTimer()

                    // Verificar si se detectó al menos un rostro
                    if (faces.toArray().isNotEmpty()) {
                        // Convertir la matriz RGBA a un formato de imagen compatible con HTTP (ej. JPEG)
                        val faceMat = Mat(rgbaMat, faces.toArray()[0])
                        enviarImagen(faceMat)

                        // Actualizar el tiempo de la última solicitud
                        lastRequestTimeMillis = currentTimeMillis
                    }
                }

                // Salir del hilo
                return@Thread
            }

            // Liberar la matriz YUV
            yuvMat.release()
        }.start() // Iniciar el hilo
    }

    // Método que carga el clasificador en cascada para detección de rostros
    private fun loadFaceCascade() {
        try {
            val resourceId = R.raw.haarcascade_frontalface_default
            val isStream: InputStream = resources.openRawResource(resourceId)
            val cascadeDir: File = cacheDir
            val cascadeFile: File = File(cascadeDir, "haarcascade_frontalface_default.xml")
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

    // Método para enviar la imagen del rostro detectado al intent de registro
    private fun enviarImagen(faceMat: Mat) {
        // Rotar la imagen 90 grados en sentido antihorario
        Core.rotate(faceMat, faceMat, Core.ROTATE_90_COUNTERCLOCKWISE)

        // Verificar el tipo de la matriz de OpenCV
        if (faceMat.type() != CvType.CV_8UC3) {
            // Si la matriz no es de 3 canales (RGB), convertirla a RGB
            Imgproc.cvtColor(faceMat, faceMat, Imgproc.COLOR_BGR2RGB)
        }

        // Convertir la matriz de OpenCV a un formato de imagen compatible con HTTP (ej. JPEG)
        val byteStream = ByteArrayOutputStream()
        val imageMat = MatOfByte()
        Imgcodecs.imencode(".jpg", faceMat, imageMat)
        byteStream.write(imageMat.toArray())

        val byteArray = byteStream.toByteArray()

        // Crear un intent para pasar la imagen a la actividad anterior
        val resultIntent = Intent().apply {
            putExtra("image", byteArray)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

