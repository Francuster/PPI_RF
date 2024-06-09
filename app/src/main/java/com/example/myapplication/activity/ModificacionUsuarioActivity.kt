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
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Patterns

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
            "PERSONAL JERÁRQUICO",
            "ESTUDIANTE",
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

        // Agregar filtros y validaciones
        agregarFiltros()
        agregarValidaciones()

    }

    fun goToModificacionRol(view: View) {
        val intent = Intent(applicationContext, ModificacionRolActivity::class.java)
        startActivityForResult(intent, ROLE_REQUEST_CODE)
    }

    fun goToModificacionHora(view: View) {
        val intent = Intent(applicationContext, ModificacionHoraActivity::class.java)
        startActivityForResult(intent, HOURS_REQUEST_CODE) // Asegúrate de usar startActivityForResult
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

    private fun agregarFiltros() {
        val letrasFilter = object : InputFilter {
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                val builder = StringBuilder()
                for (i in start until end) {
                    val char = source?.get(i)
                    if (char != null && (char.isLetter() || char.isWhitespace())) {
                        builder.append(char)
                    }
                }
                return if (builder.isEmpty()) {
                    nombreEditText.error = "Solo se permiten letras"
                    apellidoEditText.error="Solo se permiten letras"
                    ""
                } else {
                    null
                }
            }
        }

        val numerosFilter = object : InputFilter {
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                val builder = StringBuilder()
                for (i in start until end) {
                    val char = source?.get(i)
                    if (char != null && char.isDigit()) {
                        builder.append(char)
                    }
                }
                return if (builder.isEmpty()) {
                    documentoEditText.error = "Solo se permiten números"
                    ""
                } else {
                    null
                }
            }
        }

        nombreEditText.filters = arrayOf(letrasFilter)
        apellidoEditText.filters = arrayOf(letrasFilter)
        documentoEditText.filters = arrayOf(numerosFilter)
    }

    private fun agregarValidaciones() {
        nombreEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    nombreEditText.error = "El campo no puede estar vacío"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val filteredText = s.toString().filter { it.isLetter() || it.isWhitespace() }
                if (s.toString() != filteredText) {
                    nombreEditText.setText(filteredText)
                    nombreEditText.setSelection(filteredText.length)
                }
            }
        })

        apellidoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    apellidoEditText.error = "El campo no puede estar vacío"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val filteredText = s.toString().filter { it.isLetter() || it.isWhitespace() }
                if (s.toString() != filteredText) {
                    apellidoEditText.setText(filteredText)
                    apellidoEditText.setSelection(filteredText.length)
                }
            }
        })

        mailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    mailEditText.error = "El campo no puede estar vacío"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    mailEditText.error = "Formato de email incorrecto"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        documentoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    documentoEditText.error = "El campo no puede estar vacío"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val filteredText = s.toString().filter { it.isDigit() }
                if (s.toString() != filteredText) {
                    documentoEditText.setText(filteredText)
                    documentoEditText.setSelection(filteredText.length)
                }
            }
        })
    }

    fun actualizarUsuario(view: View) {
        if (validarCampos()) {
            enviarDatosModificacion()
        } else {
            Toast.makeText(this, "Por favor, corrige los errores antes de continuar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        return nombreEditText.error == null &&
                apellidoEditText.error == null &&
                mailEditText.error == null &&
                documentoEditText.error == null &&
                !nombreEditText.text.isNullOrEmpty() &&
                !apellidoEditText.text.isNullOrEmpty() &&
                !mailEditText.text.isNullOrEmpty() &&
                !documentoEditText.text.isNullOrEmpty()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                HOURS_REQUEST_CODE -> {
                    horaEntrada = data?.getStringExtra("hora_entrada")
                    horaSalida = data?.getStringExtra("hora_salida")
                    // Agrega un log para asegurarte de que los valores se están recibiendo correctamente
                    println("Hora de entrada: $horaEntrada, Hora de salida: $horaSalida")
                }
                CAMERA_REQUEST_CODE -> {
                    imageByteArray = data?.getByteArrayExtra("image")
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
