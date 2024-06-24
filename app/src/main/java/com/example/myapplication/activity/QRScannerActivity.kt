package com.example.myapplication.activity
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.myapplication.R
import com.example.myapplication.database.TAG
import com.example.myapplication.database.getNuevoEstadoByDniLocal
import com.example.myapplication.database.registrarLogs
import com.example.myapplication.utils.deviceIsConnected
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class QRScannerActivity: AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var barcodeScanner: BarcodeScanner
    private var activeCall: Call? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private var isRegisteringLocally = false
    data class LastEstadoResponse(val estado: String)

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val rawValue = barcode.rawValue
                                Toast.makeText(this, "Código QR escaneado: $rawValue", Toast.LENGTH_SHORT).show()
                                enviarDatos(this, rawValue)

                                // Aquí puedes manejar el valor del código QR escaneado
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            // Manejar el error
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch(exc: Exception) {
                // Manejar el error
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permisos no concedidos por el usuario.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun extraerDatosDelCrudo(input: String?): Array<String> {
        if (input != null) {
            return input.split("@").toTypedArray()
        } else {
            return emptyArray()
        }
    }

    fun enviarDatos(context: Context, input: String?) {
        if (activeCall != null) {
            Log.w("TAG", "Solicitud HTTP en curso. No se iniciará una nueva solicitud.")
            return
        }

        if (isRegisteringLocally) {
            Log.w("TAG", "Registro ya se ejecutó. No se iniciará una nueva solicitud.")
            return
        }

        val data = extraerDatosDelCrudo(input)
        data.forEach { Log.i("TAG", "Ingreso de datos: $it") }
        val dniSinLetrasa = obtenerSoloNumeros(data[4])
        if (deviceIsConnected(context)) {
            val nuevoEstadoOnline = getLastEstadoByDniOnline(dniSinLetrasa)

            Log.i("TAG", "Nuevo esto: ${nuevoEstadoOnline} para dni: ${data[4]} ")
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("horario", obtenerFechaActualISO())
                .addFormDataPart("nombre", data[1])
                .addFormDataPart("apellido", data[2])
                .addFormDataPart("dni", dniSinLetrasa)
                .addFormDataPart("estado", nuevoEstadoOnline)
                .addFormDataPart("tipo", "especial")
                .build()

            val request = Request.Builder()
                .url("https://log3r.up.railway.app/api/logs/authentication") // Cambiar por IP local para prueba o IP online
                .post(requestBody)
                .build()

            activeCall = client.newCall(request)
            activeCall?.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Error en la solicitud HTTP", Toast.LENGTH_LONG).show()
                    }
                    activeCall = null
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
                            val jsonObject = JSONObject(responseBody)
                            (context as Activity).runOnUiThread {
                                Toast.makeText(context, "Usuario ingresado exitosamente", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.body?.string()
                            Log.e("TAG", "HTTP request unsuccessful with status code ${response.code}, message: $errorBody")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("TAG", "Error en la respuesta: ${e.message}")
                    } finally {
                        response.body?.close()
                        activeCall = null
                    }
                }
            })
        } else {
            val lastEstadoLocal = getNuevoEstadoByDniLocal(this, Integer.parseInt(dniSinLetrasa))
            registrarLogs(context, data[1], data[2], dniSinLetrasa.toInt(), lastEstadoLocal, "offline")
            Toast.makeText(context, "Usuario ingresado de manera local exitosamente", Toast.LENGTH_LONG).show()
            isRegisteringLocally = true;
        }
    }

    fun getLastEstadoByDniOnline(dni: String): String {
        val future = CompletableFuture<String?>()
        val url = "http://log3r-dev.up.railway.app/api/logs/lastEstadoByDni?dni=$dni"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error al realizar la solicitud: ${e.message}")
                future.complete(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    // Parsear la respuesta JSON
                    val gson = Gson()
                    val responseData = gson.fromJson(responseBody, LastEstadoResponse::class.java)
                    future.complete(responseData.estado)
                } else {
                    println("Error en la respuesta del servidor: ${response.code}")
                    future.complete(null)
                }
            }
        })
        val nuevoEstado = future.get()
        Log.i("TAG", "--- Estado Actual: ${nuevoEstado.toString()} para dni: ${dni} ")
        if(nuevoEstado.toString() == "Ingresando"){
            return "Saliendo"
        }else{
            return "Ingresando"
        }
    }

    fun obtenerSoloNumeros(input: String): String {
        Log.i("TAG", "!!! dni Actual: ${input}")
        Log.i("TAG", "!!! nuevo dni: ${input.filter { it.isDigit() }}")
        return input.filter { it.isDigit() }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}