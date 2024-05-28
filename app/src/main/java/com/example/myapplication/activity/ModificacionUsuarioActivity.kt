package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ModificacionUsuarioActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val TEXT_REQUEST_CODE = 101
    private val ROLE_REQUEST_CODE = 102
    private val HOURS_REQUEST_CODE = 103
    private var userId: String? = null

    private var nombre: String? = null
    private var apellido: String? = null
    private var mail: String? = null
    private var documento: String? = null
    private var rol: String? = null
    private var horaEntrada: String? = null
    private var horaSalida: String? = null
    private var imageByteArray: ByteArray? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_usuario)
        // Recupera el ID del usuario del intent
        userId = intent.getStringExtra("user_id")
    }

    fun goToModificacionRol(view: View) {
        val intent = Intent(applicationContext, ModificacionRolActivity::class.java)
        startActivityForResult(intent, ROLE_REQUEST_CODE)
    }

    fun goToModificacionHora(view: View) {
        val intent = Intent(applicationContext, ModificacionHoraActivity::class.java)
        startActivity(intent)
    }

    fun goToModificacionTexto(view: View) {
        val tag = view.tag as? String
        tag?.let {
            val intent = Intent(this, ModificacionTextoActivity::class.java)
            intent.putExtra("campo", tag)
            startActivityForResult(intent, TEXT_REQUEST_CODE)
        }
    }

    fun goToAtrasInicioRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

    fun goToCameraParaRegistro(view: View) {
        val intent = Intent(this, CamaraParaRegistroRrHhActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                TEXT_REQUEST_CODE -> {
                    val campo = data?.getStringExtra("campo")
                    val textoModificado = data?.getStringExtra("texto_modificado")
                    when (campo) {
                        "nombre" -> nombre = textoModificado
                        "apellido" -> apellido = textoModificado
                        "mail" -> mail = textoModificado
                        "documento" -> documento = textoModificado
                    }
                }
                ROLE_REQUEST_CODE -> {
                    rol = data?.getStringExtra("rol_modificado")
                }
                HOURS_REQUEST_CODE -> {
                    horaEntrada = data?.getStringExtra("hora_entrada")
                    horaSalida = data?.getStringExtra("hora_salida")
                }
                CAMERA_REQUEST_CODE -> {
                    imageByteArray = data?.getByteArrayExtra("image")
                    enviarDatosModificacion() // Llama a enviarDatosModificacion aquí
                }
            }
        }
    }

    fun enviarDatosModificacion() {
        userId?.let { id ->
            val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            nombre?.let { multipartBodyBuilder.addFormDataPart("nombre", it) }
            apellido?.let { multipartBodyBuilder.addFormDataPart("apellido", it) }
            documento?.let { multipartBodyBuilder.addFormDataPart("dni", it) }
            rol?.let { multipartBodyBuilder.addFormDataPart("rol", it) }
            horaEntrada?.let { multipartBodyBuilder.addFormDataPart("horariosEntrada", it) }
            horaSalida?.let { multipartBodyBuilder.addFormDataPart("horariosSalida", it) }
            mail?.let { multipartBodyBuilder.addFormDataPart("email", it) }

            imageByteArray?.let {
                val tempFile = File.createTempFile("image", ".jpg", cacheDir)
                FileOutputStream(tempFile).use { fos ->
                    fos.write(it)
                }
                multipartBodyBuilder.addFormDataPart("image", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            }

            val requestBody = multipartBodyBuilder.build()
            val request = Request.Builder()
                .url(BuildConfig.BASE_URL+"/api/users/$id")
                .post(requestBody)
                .header("Content-Type", "multipart/form-data")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        when (response.code) {
                            200 -> {
                                Toast.makeText(this@ModificacionUsuarioActivity, "ÉXITO EN LA SOLICITUD: USUARIO REGISTRADO", Toast.LENGTH_SHORT).show()
                                goToModificacionExitosa()
                            }
                            500 -> {
                                Toast.makeText(this@ModificacionUsuarioActivity, "Error 500", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@ModificacionUsuarioActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@ModificacionUsuarioActivity, "Fallo en la solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    fun goToModificacionExitosa() {
        val intent = Intent(applicationContext, RegistroExitoso2Activity::class.java)
        startActivity(intent)
    }
}
