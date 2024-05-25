package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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
            val json = JSONObject().apply {
                put("nombre", nombre)
                put("apellido", apellido)
                put("dni", documento)
                put("rol", rol)
                put("horariosEntrada", horaEntrada)
                put("horariosSalida", horaSalida)
                put("email", mail)
                imageByteArray?.let {
                    put("image", Base64.encodeToString(it, Base64.DEFAULT))
                }
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("http://192.168.1.34:5000/api/users/$id")
                .put(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@ModificacionUsuarioActivity, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show()
                            goToModificacionExitosa()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ModificacionUsuarioActivity, "Fallo en la actualización del usuario", Toast.LENGTH_SHORT).show()
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
