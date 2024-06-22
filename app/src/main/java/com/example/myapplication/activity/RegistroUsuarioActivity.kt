package com.example.myapplication.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.ImagenModel
import com.example.myapplication.model.Licencia
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.changeTextColorTemporarily
import com.example.myapplication.utils.imageToggleAtras
import com.example.myapplication.utils.isServiceRunning
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroUsuarioActivity : AppCompatActivity() {
    private lateinit var miVista : View
    private lateinit var loadingOverlayout: View
    private var horariosList = listOf<HorarioModel>()
    private lateinit var horarioSpinner: Spinner
    private lateinit var registrarButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.registro_primera_sala_rrhh)
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        miVista = findViewById(R.id.layout_hijo)
        val spinner: Spinner = findViewById<Spinner>(R.id.tipo_cuenta)
        var elementos = ArrayList<String>()

        registrarButton = findViewById(R.id.boton_registrar_ingreso)
        // Deshabilitar el botón registrar hasta que se carguen los horarios
        registrarButton.isEnabled = false

        elementos.add("ADMINISTRADOR")
        elementos.add("DOCENTE")
        elementos.add("NO DOCENTE")
        elementos.add("ESTUDIANTE")
        elementos.add("SEGURIDAD")
        elementos.add("RECURSOS HUMANOS")
        elementos.add("PERSONAL JERÁRQUICO")

        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador
        val imageView = findViewById<ImageView>(R.id.imagen_volver)
        imageToggleAtras(imageView,applicationContext,"irInicioRrHhActivity",ArrayList<Empleado>(),ArrayList<Licencia>(),ArrayList<Empleado>())

        agregarFiltrosValidaciones()
        getHorarios()
    }


    private fun showLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.GONE
        }
    }


    private fun agregarFiltrosValidaciones() {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val emailEditText = findViewById<EditText>(R.id.email_texto)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)

        // Filtro para permitir solo letras y espacios en el nombre y apellido
        val letrasFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val builder = StringBuilder()
                for (i in start until end) {
                    val char = source?.get(i)
                    if (char != null && (char.isLetter() || char.isWhitespace())) {
                        builder.append(char)
                    }
                }
                return if (builder.isEmpty()) {
                    nombreEditText.error = "Solo se permiten letras"
                    apellidoEditText.error = "Solo se permiten letras"
                    ""
                } else {
                    null
                }
            }
        }

        // Filtro para permitir solo números en el documento
        val numerosFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
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

        // TextWatcher para capitalizar el nombre
        nombreEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val capitalizedText = text.split(" ").joinToString(" ") { it.capitalize() }
                    if (capitalizedText != text) {
                        nombreEditText.setText(capitalizedText)
                        nombreEditText.setSelection(capitalizedText.length)
                    }
                }
            }
        })

        // TextWatcher para capitalizar el apellido
        apellidoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val capitalizedText = text.split(" ").joinToString(" ") { it.capitalize() }
                    if (capitalizedText != text) {
                        apellidoEditText.setText(capitalizedText)
                        apellidoEditText.setSelection(capitalizedText.length)
                    }
                }
            }
        })

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    emailEditText.error = "El campo no puede estar vacío"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    emailEditText.error = "El formato de mail es algo@algo.com"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        documentoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val filteredText = s.toString().filter { it.isDigit() }
                if (s.toString() != filteredText) {
                    documentoEditText.setText(filteredText)
                    documentoEditText.setSelection(filteredText.length)
                }
            }
        })

        tipoCuentaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem.isNullOrEmpty()) {
                    (parent?.getChildAt(0) as? TextView)?.error =
                        "Seleccione un tipo de cuenta válido"
                } else {
                    (parent?.getChildAt(0) as? TextView)?.error = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                (parent?.getChildAt(0) as? TextView)?.error =
                    "Seleccione un tipo de cuenta válido"
            }
        }
    }



    private fun esNombreValido(nombre: String): Boolean {
        return nombre.isNotEmpty() && nombre.all { it.isLetter() || it.isWhitespace() }
    }

    private fun esApellidoValido(apellido: String): Boolean {
        return apellido.isNotEmpty() && apellido.all { it.isLetter() || it.isWhitespace() }
    }

    private fun esDocumentoValido(documento: String): Boolean {
        return documento.isNotEmpty() && documento.all { it.isDigit() }
    }

    private fun esEmailValido(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validarCampos(): Boolean {
        val nombre = findViewById<EditText>(R.id.nombre_texto).text.toString()
        val apellido = findViewById<EditText>(R.id.apellido_texto).text.toString()
        val documento = findViewById<EditText>(R.id.documento_texto).text.toString()
        val email = findViewById<EditText>(R.id.email_texto).text.toString()
        val tipoCuenta = findViewById<Spinner>(R.id.tipo_cuenta).selectedItem.toString()
        val horarioModel = horarioSpinner.selectedItem as HorarioModel

        return esNombreValido(nombre) &&
                esApellidoValido(apellido) &&
                esDocumentoValido(documento) &&
                esEmailValido(email) &&
                tipoCuenta.isNotEmpty() &&
                horarioModel._id.isNotEmpty()
    }

    fun getHorarios() {
        RetrofitClient.horariosApiService.get().enqueue(object : Callback<List<HorarioModel>> {
            override fun onResponse(
                call: Call<List<HorarioModel>>,
                response: Response<List<HorarioModel>>
            ) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        Log.i("PerfilUsuario", response.body().toString())

                        horariosList = response.body()!!
                        cargarHorariosSpinner()
                        // Habilitar el botón registrar
                        registrarButton.isEnabled = true
                    }
                }
            }

            override fun onFailure(call: Call<List<HorarioModel>>, t: Throwable) {
                Log.e("PerfilUsuario", "getHorarios onFailure")
            }
        })
    }

    fun cargarHorariosSpinner() {
        val spinner: Spinner = findViewById(R.id.horario_create)

        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, horariosList)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador
        horarioSpinner = spinner
    }

    fun registrarUsuario(view: View) {
        registrarButton.changeTextColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms)
        if (validarCampos()) {
            val miVista = findViewById<View>(R.id.layout_hijo)
            miVista.alpha = 0.10f // 10% de opacidad
            showLoadingOverlay()
            enviarDatosRegistro()
        } else {
            mostrarDialogoCamposIncompletos()
        }
    }

    private fun mostrarDialogoCamposIncompletos() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Campos incompletos")
        builder.setMessage("Por favor, complete todos los campos correctamente.")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    fun enviarDatosRegistro() {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)
        val emailEditText = findViewById<EditText>(R.id.email_texto)

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()
        val email = emailEditText.text.toString()

        // Obtener el horario seleccionado si está inicializado
        val horarioModel = if (::horarioSpinner.isInitialized) {
            horarioSpinner.selectedItem as? HorarioModel
        } else {
            null
        }

        // Crear el modelo de usuario
        val userModel = UserModel(
            "",
            nombre,
            apellido,
            documento.toIntOrNull() ?: 0, // Manejo seguro para convertir a Int
            tipoCuenta,
            horarioModel?.let { listOf(it._id) } ?: emptyList(),
            email
        )

        // Hacer la llamada a la API
        RetrofitClient.userApiService.post(userModel).enqueue(object : Callback<ImagenModel> {
            override fun onResponse(call: Call<ImagenModel>, response: Response<ImagenModel>) {
                when (response.code()) {
                    200 -> {
                        val userModelResponse = response.body()
                        hideLoadingOverlay()
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "ÉXITO EN LA SOLICITUD: USUARIO REGISTRADO",
                            Toast.LENGTH_SHORT
                        ).show()

                        userModelResponse?._id?.let { userId ->
                            goToRegistroExitoso(userId)
                        }
                    }

                    201 -> {
                        val userModelResponse = response.body()
                        hideLoadingOverlay()
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "USUARIO REGISTRADO, PERO CON ERROR 201",
                            Toast.LENGTH_SHORT
                        ).show()

                        userModelResponse?._id?.let { userId ->
                            goToRegistroExitoso(userId)
                        }
                    }

                    400 -> {
                        hideLoadingOverlay()
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "ERROR: DNI o MAIL ya registrados en la base de datos",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToRegistroDenegado()
                    }

                    500 -> {
                        hideLoadingOverlay()
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "Error 500",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToRegistroDenegado()
                    }

                    else -> {
                        hideLoadingOverlay()
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "Error desconocido, revise el servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToRegistroDenegado()
                    }
                }
            }

            override fun onFailure(call: Call<ImagenModel>, t: Throwable) {
                Toast.makeText(
                    this@RegistroUsuarioActivity,
                    "Fallo en la solicitud de registro de usuario: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                hideLoadingOverlay()
                goToRegistroDenegado()
            }
        })
    }



    private fun goToRegistroExitoso(userId: String) {
        val intent = Intent(this, RegistroExitoso2Activity::class.java)
        intent.putExtra("origen", "RegistroUsuarioActivity")
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    fun goToRegistroDenegado() {
        val intent = Intent(applicationContext, RegistroDenegado2Activity::class.java)
        intent.putExtra("origen", "RegistroUsuario")
        startActivity(intent)
    }

    fun Siguiente(view: View) {
        val intent = Intent(applicationContext, RegistroDenegado2Activity::class.java)
        intent.putExtra("origen", "RegistroUsuario")
        startActivity(intent)
    }

    fun goInicioRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }
}
