package com.example.myapplication.activity


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import com.example.myapplication.BuildConfig
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale


class RegistroUsuarioActivity:AppCompatActivity() {
    private val client = OkHttpClient()
    private val CAMERA_REQUEST_CODE = 100
    private var imageByteArray: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.registro_primera_sala_rrhh)
        val spinner:Spinner = findViewById<Spinner>(R.id.tipo_cuenta)
        var elementos=ArrayList<String>()

        elementos.add("ADMINISTRADOR")
        elementos.add("DOCENTE")
        elementos.add("NO DOCENTE")
        elementos.add("ESTUDIANTE")
        elementos.add("SEGURIDAD")
        elementos.add("RECURSOS HUMANOS")
        elementos.add("PERSONAL JERÁRQUICO")

        val adaptador=ArrayAdapter(this,R.layout.desplegable_tipo_cuenta,elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter=adaptador

        val hora_entrada=findViewById<TextInputEditText>(R.id.hora_entrada)
        hora_entrada.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_entrada.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()

        }

        val hora_salida=findViewById<TextInputEditText>(R.id.hora_salida)
        hora_salida.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_salida.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()
        }
        agregarFiltrosValidaciones()

    }

    private fun agregarFiltrosValidaciones() {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val emailEditText = findViewById<EditText>(R.id.email_texto)
        val horaEntradaEditText = findViewById<EditText>(R.id.hora_entrada)
        val horaSalidaEditText = findViewById<EditText>(R.id.hora_salida)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)

        // Filtro para permitir solo letras y espacios en el nombre y apellido
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

        // Filtro para permitir solo números en el documento
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

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    emailEditText.error = "El campo no puede estar vacío"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    emailEditText.error = "Formato de email incorrecto"
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



        horaEntradaEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = horaEntradaEditText.text.toString()
                if (text.isEmpty()) {
                    horaEntradaEditText.error = "El campo no puede estar vacío"
                }
            }
        }

        horaSalidaEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = horaSalidaEditText.text.toString()
                if (text.isEmpty()) {
                    horaSalidaEditText.error = "El campo no puede estar vacío"
                }
            }
        }

        tipoCuentaSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem.isNullOrEmpty()) {
                    (parent?.getChildAt(0) as? TextView)?.error = "Seleccione un tipo de cuenta válido"
                } else {
                    (parent?.getChildAt(0) as? TextView)?.error = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                (parent?.getChildAt(0) as? TextView)?.error = "Seleccione un tipo de cuenta válido"
            }
        })
    }



    private fun validarCampos(): Boolean {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val emailEditText = findViewById<EditText>(R.id.email_texto)
        val horaEntradaEditText = findViewById<EditText>(R.id.hora_entrada)
        val horaSalidaEditText = findViewById<EditText>(R.id.hora_salida)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val email = emailEditText.text.toString()
        val horaEntrada = horaEntradaEditText.text.toString()
        val horaSalida = horaSalidaEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()

        // Validar que ningún campo esté vacío
        if (nombre.isEmpty() || apellido.isEmpty() || documento.isEmpty() || email.isEmpty() || horaEntrada.isEmpty() || horaSalida.isEmpty() || tipoCuenta.isEmpty()) {
            return false
        }

        // Validar email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        return true
    }

    fun registrarUsuario(view: View) {
        if (validarCampos()) {
            enviarDatosRegistro()
        } else {
            // Mostrar mensaje de error general si algún campo no es válido
            Toast.makeText(this, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
        }
    }
    fun goToCamaraParaRegistro(view: View) {
        val intent = Intent(this, CameraxAddFaceActivity::class.java)
        intent.putExtra("fromActivity", "RegistroUsuarioActivity")
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    //TODO: no se usa
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val byteArray = data?.getByteArrayExtra("image")
            if (byteArray != null) {
                imageByteArray = byteArray
                //imagenBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                //enviarDatosRegistro()
            }
        }
    }



    fun enviarDatosRegistro() {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)
        val horaEntradaEditText = findViewById<EditText>(R.id.hora_entrada)
        val horaSalidaEditText = findViewById<EditText>(R.id.hora_salida)
        val emailEditText = findViewById<EditText>(R.id.email_texto)

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()
        val horaEntrada = horaEntradaEditText.text.toString()
        val horaSalida = horaSalidaEditText.text.toString()
        val email = emailEditText.text.toString()

        val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("nombre", nombre)
            .addFormDataPart("apellido", apellido)
            .addFormDataPart("dni", documento)
            .addFormDataPart("rol", tipoCuenta.lowercase(Locale.ENGLISH))
            .addFormDataPart("horariosEntrada", horaEntrada)
            .addFormDataPart("horariosSalida", horaSalida)
            .addFormDataPart("email", email)
            .addFormDataPart("image", "")

        imageByteArray?.let {
            val tempFile = File.createTempFile("image", ".jpg", cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(it)
            }
            multipartBodyBuilder.addFormDataPart("image", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }

        val requestBody = multipartBodyBuilder.build()
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL +"/api/users")//IP LOCAL U ONLINE
            .post(requestBody)
            .header("Content-Type", "multipart/form-data")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    when (response.code) {
                        200 -> {
                            Toast.makeText(this@RegistroUsuarioActivity, "ÉXITO EN LA SOLICITUD: USUARIO REGISTRADO", Toast.LENGTH_SHORT).show()
                            goToRegistroExitoso()
                        }
                        201 -> {
                            Toast.makeText(this@RegistroUsuarioActivity, "USUARIO REGISTRADO, PERO CON ERROR 201", Toast.LENGTH_SHORT).show()
                            goToRegistroExitoso()
                        }
                        400 ->{
                            Toast.makeText(this@RegistroUsuarioActivity, "ERROR: Debe llenar todos los campos y luego tomar la foto", Toast.LENGTH_SHORT).show()

                            goToRegistroDenegado()
                        }
                        500 -> {
                            goToRegistroDenegado()
                            Toast.makeText(this@RegistroUsuarioActivity, "Error 500", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            goToRegistroDenegado()
                            Toast.makeText(this@RegistroUsuarioActivity, "Error busque en consola del servidor: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegistroUsuarioActivity, "Fallo en la solicitud de registro de usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                goToRegistroDenegado()
            }
        })
    }




    fun goToRegistroExitoso() {

        val intent = Intent(applicationContext, RegistroExitoso2Activity::class.java)
        startActivity(intent)
    }
    fun goToRegistroDenegado() {

        val intent = Intent(applicationContext, RegistroDenegado2Activity::class.java)
        startActivity(intent)
    }
    fun Siguiente(view : View){

        val intent = Intent(applicationContext, Denegado::class.java)
        startActivity(intent)

    }

}