package com.example.myapplication.activity


import android.content.Intent
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
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.ImagenModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegistroUsuarioActivity:AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 100
    private var imageByteArray: ByteArray? = null
    private var horariosList = listOf<HorarioModel>()
    private lateinit var horarioSpinner: Spinner


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

        agregarFiltrosValidaciones()
        getHorarios()


    }

    private fun agregarFiltrosValidaciones() {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val emailEditText = findViewById<EditText>(R.id.email_texto)
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

    fun getHorarios(){
        RetrofitClient.horariosApiService.get().enqueue(object: Callback<List<HorarioModel>>{
            override fun onResponse(
                call: Call<List<HorarioModel>>,
                response: Response<List<HorarioModel>>
            ) {

                if(response.code() == 200){
                    if(response.body() != null){
                        Log.i("PerfilUsuario", response.body().toString())

                        horariosList = response.body()!!
                        cargarHorariosSpinner()
                    }
                }
            }

            override fun onFailure(call: Call<List<HorarioModel>>, t: Throwable) {
                Log.e("PerfilUsuario", "getHorarios onFailure")
            }
        })
    }

    fun cargarHorariosSpinner(){
        val spinner: Spinner = findViewById(R.id.horario_create)

        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, horariosList)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador
        horarioSpinner = spinner

    }



    private fun validarCampos(): Boolean {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val emailEditText = findViewById<EditText>(R.id.email_texto)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val email = emailEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()
        val horarioModel = horarioSpinner.selectedItem as HorarioModel


        // Validar que ningún campo esté vacío
        if (nombre.isEmpty() || apellido.isEmpty() || documento.isEmpty() || email.isEmpty() || tipoCuenta.isEmpty() || horarioModel._id.isEmpty()) {
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
        val horarioModel = horarioSpinner.selectedItem as HorarioModel

        val userModel = UserModel("", nombre, apellido, documento.toInt(), tipoCuenta, listOf(horarioModel._id), email)
        RetrofitClient.userApiService.post(userModel).enqueue(object : Callback<ImagenModel> {
            override fun onResponse(call: Call<ImagenModel>, response: Response<ImagenModel>) {
                when (response.code()) {
                    200 -> {
                        val userModelResponse = response.body()

                        Toast.makeText(this@RegistroUsuarioActivity, "ÉXITO EN LA SOLICITUD: USUARIO REGISTRADO", Toast.LENGTH_SHORT).show()

                        userModelResponse?._id?.let { userId ->
                            goToRegistroExitoso(userId)
                        }
                    }
                    201 -> {
                        Toast.makeText(this@RegistroUsuarioActivity, "USUARIO REGISTRADO, PERO CON ERROR 201", Toast.LENGTH_SHORT).show()
                        goToRegistroDenegado()
                    }
                    400 -> {
                        Toast.makeText(this@RegistroUsuarioActivity, "ERROR: Debe llenar todos los campos y luego tomar la foto", Toast.LENGTH_SHORT).show()
                        goToRegistroDenegado()
                    }
                    500 -> {
                        Toast.makeText(this@RegistroUsuarioActivity, "Error 500", Toast.LENGTH_SHORT).show()
                        goToRegistroDenegado()
                    }
                    else -> {
                        Toast.makeText(this@RegistroUsuarioActivity, "Error desconocido, revise el servidor", Toast.LENGTH_SHORT).show()
                        goToRegistroDenegado()
                    }
                }
            }

            override fun onFailure(call: Call<ImagenModel>, t: Throwable) {
                Toast.makeText(this@RegistroUsuarioActivity, "Fallo en la solicitud de registro de usuario: ${t.message}", Toast.LENGTH_SHORT).show()
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
        startActivity(intent)
    }
    fun Siguiente(view : View){

        val intent = Intent(applicationContext, Denegado::class.java)
        startActivity(intent)

    }
    fun goInicioRRHH(view : View){
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}