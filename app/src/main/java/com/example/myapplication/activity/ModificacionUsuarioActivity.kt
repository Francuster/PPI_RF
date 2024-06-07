package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
import java.util.Locale

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
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var mailEditText: EditText
    private lateinit var documentoEditText: EditText
    private lateinit var rolSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_usuario)
        userId = intent.getStringExtra("user_id")
        userName = intent.getStringExtra("user_name") // nombre para mostrar
        userSurname = intent.getStringExtra("user_apellido") // apellido para mostrar
        val textoNombreUsuario = findViewById<TextView>(R.id.modificacion_titulo)
        textoNombreUsuario.text = "Modificando usuario:\n$userName $userSurname "

        val spinner: Spinner = findViewById(R.id.tipo_cuenta)
        val elementos = arrayListOf(
            "ADMINISTRADOR",
            "DOCENTE",
            "NO DOCENTE",
            "SEGURIDAD",
            "RECURSOS HUMANOS",
            "PERSONAL JERÁRQUICO"
        )

        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador

        // Obtén las referencias a los campos de texto y spinner
        nombreEditText = findViewById(R.id.nombre_texto)
        apellidoEditText = findViewById(R.id.apellido_texto)
        mailEditText = findViewById(R.id.email_texto)
        documentoEditText = findViewById(R.id.documento_texto)
        rolSpinner = findViewById(R.id.tipo_cuenta)
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
        val intent = Intent(this, CameraxAddFaceActivity::class.java)
        intent.putExtra("fromActivity", "ModificacionUsuarioActivity")
        intent.putExtra("userId", userId)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    fun actualizarUsuario(view: View) {
        enviarDatosModificacion()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
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

            val nombre = nombreEditText.text.toString()
            val apellido = apellidoEditText.text.toString()
            val documento = documentoEditText.text.toString()
            val tipoCuenta = rolSpinner.selectedItem.toString()
            val email = mailEditText.text.toString()

            val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("nombre", nombre)
                .addFormDataPart("apellido", apellido)
                .addFormDataPart("dni", documento)
                .addFormDataPart("rol", tipoCuenta.lowercase(Locale.ENGLISH))
                .addFormDataPart("horariosEntrada", horaEntrada ?: "")
                .addFormDataPart("horariosSalida", horaSalida ?: "")
                .addFormDataPart("email", email)

            imageByteArray?.let {
                val tempFile = File.createTempFile("image", ".jpg", cacheDir)
                FileOutputStream(tempFile).use { fos ->
                    fos.write(it)
                }
                multipartBodyBuilder.addFormDataPart("image", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            }

            val requestBody = multipartBodyBuilder.build()
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}/api/users/$id")
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
                                goToModificacionError()
                                Toast.makeText(this@ModificacionUsuarioActivity, "Error 500", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                goToModificacionError()
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

    fun goToModificacionError() {
        val intent = Intent(applicationContext, RegistroDenegado2Activity::class.java)
        startActivity(intent)
    }
}
