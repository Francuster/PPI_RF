package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
    private var userName: String? = null
    private var userSurname: String? = null
    private var nombre: String? = null
    private var apellido: String? = null
    private var mail: String? = null
    private var documento: String? = null
    private var rol: String? = null
    private var horaEntrada: String? = null
    private var horaSalida: String? = null
    private var imageByteArray: ByteArray? = null
    private val client = OkHttpClient()
    private lateinit var nombreTextView: TextView
    private lateinit var apellidoTextView: TextView
    private lateinit var mailTextView: TextView
    private lateinit var documentoTextView: TextView
    private lateinit var rolTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_usuario)
        userId = intent.getStringExtra("user_id")
        userName=intent.getStringExtra("user_name")//nombre para mostrar
        userSurname=intent.getStringExtra("user_apellido")//apellido para mostrar
        nombreTextView = findViewById(R.id.nombre_texto)
        apellidoTextView = findViewById(R.id.apellido_texto)
        mailTextView = findViewById(R.id.mail_texto)
        documentoTextView = findViewById(R.id.documento_texto)
        rolTextView = findViewById(R.id.tipo_cuenta_texto)
        val textoNombreUsuario = findViewById<TextView>(R.id.modificacion_titulo)
        textoNombreUsuario.text = "Modificando usuario:\n$userName $userSurname "
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

    fun actualizarUsuario() {
        enviarDatosModificacion() // Llama a enviarDatosModificacion aquí
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                TEXT_REQUEST_CODE -> {
                    val campo = data?.getStringExtra("campo")
                    val textoModificado = data?.getStringExtra("texto_modificado")
                    when (campo) {
                        "nombre" -> {
                            nombre = textoModificado
                            nombreTextView.text = nombre
                        }
                        "apellido" -> {
                            apellido = textoModificado
                            apellidoTextView.text = apellido
                        }
                        "mail" -> {
                            mail = textoModificado
                            mailTextView.text = mail
                        }
                        "documento" -> {
                            documento = textoModificado
                            documentoTextView.text = documento
                        }
                    }
                }
                ROLE_REQUEST_CODE -> {
                    rol = data?.getStringExtra("rol_modificado")
                    rolTextView.text=rol
                    // Actualiza el TextView correspondiente si tienes uno para el rol
                }
                HOURS_REQUEST_CODE -> {
                    horaEntrada = data?.getStringExtra("hora_entrada")
                    horaSalida = data?.getStringExtra("hora_salida")
                    // Actualiza los TextView correspondientes si tienes
                }
                CAMERA_REQUEST_CODE -> {
                    imageByteArray = data?.getByteArrayExtra("image")
                    //enviarDatosModificacion() // Llama a enviarDatosModificacion aquí
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
                .put(requestBody)
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
