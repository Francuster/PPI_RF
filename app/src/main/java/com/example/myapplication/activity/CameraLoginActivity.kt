package com.example.myapplication.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.YuvImage
import android.hardware.Camera
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
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.service.FaceRecognition
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
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

class CameraLoginActivity : AppCompatActivity(), Camera.PreviewCallback {

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

    private var faceRecognition: FaceRecognition? = null


    init {
        faceRecognition = FaceRecognition()

    }

    // Cliente HTTP
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()


    //FUNCIONES
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)



        setupUI()
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
    // Método para dibujar el óvalo en la vista previa de la cámara de manera centrada y responsive
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
                // Manejar otros permisos si es necesario
            }
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        updateButtonState()
        if (isScanning) {
            timeUpToastShown = false // Restablecer la bandera cuando se reinicia el escaneo
            startTimer() // Reiniciar el temporizador al iniciar el escaneo
            showToastOnUiThread("Escaneando 30 segundos...")
        } else {
            stopTimer() // Detener el temporizador al detener el escaneo manualmente
            showToastOnUiThread("Escaneo detenido manualmente")
        }
    }


    //cambia el estado del boton
    private fun updateButtonState() {
        buttonScan.text = if (isScanning) "Detener Escaneo" else "Escanear"
    }

    private fun handleScanTimeout() {
        isScanning = false
        updateButtonState()
        if (!detecto && !timeUpToastShown) {
            showToastOnUiThread("Tiempo de escaneo agotado")
            timeUpToastShown = true
            mostrarPantallaErrorIngreso()  // Redirigir a la pantalla de error
        }
    }

    private fun mostrarPantallaErrorIngreso() {
        val intent = Intent(applicationContext, IngresoDenegadoActivity::class.java)
        startActivity(intent)
    }


    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if (!detecto && (secondsRemaining <= 3&& secondsRemaining >2 ||secondsRemaining <= 15&& secondsRemaining >14 )) { //manejar tiempo que se muestra en toast
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



    //funcion para pasar los datos a otra activity
    private fun registro_exitoso_antesala(nombre: String, apellido: String, dni: Int, roles: String) {

        // Crear el Intent y pasar los datos
        if(roles.equals("recursos humanos")){
            val intent = Intent(this, InicioRrHhActivity::class.java)
            startActivity(intent)
        } else if(roles.equals("seguridad")){
            val intent = Intent(this, InicioSeguridadActivity::class.java)
            startActivity(intent)
        } else {
            // Crear el Intent y pasar los datos solo si el rol no es "recursos humanos" ni "seguridad"
            val intent = Intent(this, InicioSeguridadActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("apellido", apellido)
            intent.putExtra("dni", dni)
            intent.putExtra("roles", roles)
            startActivity(intent)
        }
    }





    //metodo para los toasts en el hilo principal
    private fun showToastOnUiThread(message: String) {
        runOnUiThread {
            Toast.makeText(this@CameraLoginActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    //metodo principal de deteccion de rostros en la vista previa de la camara, se ejecuta en otro hilo para optimizar recursos
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
            val yuvMat = Mat(height + height / 2, width, org.opencv.core.CvType.CV_8UC1)
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

                        // Enviar la matriz RGBA completa como una solicitud HTTP
                        enviarMatrizComoHTTPRequest(rgbaMat)

                        // Actualizar el tiempo de la última solicitud
                        lastRequestTimeMillis = currentTimeMillis
                    }

                    // Mostrar la imagen en un Toast
                    /*val bitmap = Bitmap.createBitmap(rgbaMat.cols(), rgbaMat.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(rgbaMat, bitmap)

                    val toast = Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT)
                    val imageView = ImageView(applicationContext)
                    imageView.setImageBitmap(bitmap)
                    toast.view = imageView
                    toast.show()*/
                }



                // Salir del hilo
                return@Thread
            }

            // Liberar la matriz YUV
            yuvMat.release()
        }.start() // Iniciar el hilo
    }



    private var activeCall: Call? = null // Guardo la solicitud HTTP activa

    private fun enviarMatrizComoHTTPRequest(faceMat: Mat) {
        // Rotar la imagen 90 grados en sentido antihorario
        Core.rotate(faceMat, faceMat, Core.ROTATE_90_COUNTERCLOCKWISE)

        // Verificar el tipo de la matriz de OpenCV
        if (faceMat.type() != CvType.CV_8UC3) {
            // Si la matriz no es de 3 canales (RGB), convertirla a RGB
            Imgproc.cvtColor(faceMat, faceMat, Imgproc.COLOR_BGR2RGB)
        }

        val byteStream = ByteArrayOutputStream()
        val imageMat = MatOfByte()

        try {
            // Convertir la matriz de OpenCV a un formato de imagen compatible con HTTP (ej, JPEG, PNG)
            if (!Imgcodecs.imencode(".png", faceMat, imageMat)) {
                throw IOException("Failed to encode image")
            }
            byteStream.write(imageMat.toArray())

            // Crear el cuerpo de la solicitud HTTP
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "filename.png", byteStream.toByteArray().toRequestBody("image/png".toMediaTypeOrNull()))
                .build()

            // Construir y enviar la solicitud HTTP
            val request = Request.Builder()
                .url("https://log3r.up.railway.app/api/authentication") // Cambiar por IP local para prueba o IP online
                .post(requestBody)
                .build()

            // Cancelar cualquier solicitud HTTP activa antes de enviar la nueva solicitud
            activeCall?.cancel()

            // Enviar la solicitud HTTP y guardar una referencia a la solicitud activa
            activeCall = client.newCall(request)
            activeCall?.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            // La solicitud fue exitosa
                            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
                            val jsonObject = JSONObject(responseBody)

                            // Obtener el objeto "data" que contiene los datos de la persona
                            val dataObject = jsonObject.getJSONObject("data")

                            // Extraer los datos de la persona del objeto "data"
                            val nombre = dataObject.getString("nombre")
                            val apellido = dataObject.getString("apellido")
                            val dni = dataObject.getInt("dni")
                            val rolArray = dataObject.getJSONArray("rol")

                            // Convertir el JSONArray de roles a una lista de cadenas
                            val primerRol = rolArray.getString(0)
                            val lugaresArray = dataObject.getJSONArray("lugares")
                            var lugares = lugaresArray.getString(0)

                            // Recorrer el JSONArray y almacenar cada elemento en el array
                            for (i in 1 until lugaresArray.length()) {
                                lugares = "$lugares\n${lugaresArray.getString(i)}"
                            }


                            registro_exitoso_antesala(nombre, apellido, dni, primerRol)
                        } else if (response.code == 401) {
                        // Si la solicitud fue no autorizada, mostrar pantalla inicio
                            showToastOnUiThread("Rostro detectado no registrado en la base de datos\nPor favor regístrese y vuelva a intentarlo\n")
                            //mostrarPantallaInicio()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToastOnUiThread("Error en la respuesta: ${e.message}")
                    } finally {
                        // Cerrar el cuerpo de la respuesta y limpiar la referencia a la solicitud activa
                        response.body?.close()
                        activeCall = null
                    }

                }


                override fun onFailure(call: Call, e: IOException) {
                    try {
                        // OnFailure se utiliza para manejar fallos de conexión,
                        // errores de tiempo de espera y otros problemas de red.
                        e.printStackTrace()
                        showToastOnUiThread("Rostro detectado no registrado en la base de datos\nPor favor regístrese y vuelva a intentarlo\nError en la solicitud HTTP")
                    } finally {
                        // Limpiar la referencia a la solicitud activa
                        activeCall = null
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            showToastOnUiThread("Error en la conversión o envío de la imagen: ${e.message}")
        } finally {
            // Bloque de liberación de recursos que siempre se ejecuta
            try {
                byteStream.close() // Cerrar ByteArrayOutputStream para liberar el recurso de memoria
            } catch (e: IOException) {
                e.printStackTrace() // Registrar el error si no se puede cerrar el flujo
            }
            imageMat.release() // Liberar el recurso MatOfByte para liberar la memoria nativa de OpenCV
        }

    }

    private fun mostrarPantallaInicio() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }



    private fun convertNV21ToBitmap(data: ByteArray, width: Int, height: Int): Bitmap? {
        // Convert the NV21 format byte array to a YuvImage
        val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)

        // Create an output stream to hold the JPEG data
        val out = ByteArrayOutputStream()

        // Compress the YuvImage to JPEG
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)

        // Get the JPEG byte array
        val jpegData = out.toByteArray()

        // Decode the JPEG byte array to a Bitmap
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
    }


    //metodo que carga el clasificador en cascada para deteccion de rostros
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

}