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
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning

import java.util.Locale
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModificacionUsuarioActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val TEXT_REQUEST_CODE = 101
    private val ROLE_REQUEST_CODE = 102
    private val HOURS_REQUEST_CODE = 103

    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var mailEditText: EditText
    private lateinit var documentoEditText: EditText
    private lateinit var rolSpinner: Spinner
    private lateinit var horarioSpinner: Spinner
    private lateinit var userModel: UserModel

    private var rolArrayList = arrayListOf(
        "ADMINISTRADOR",
        "DOCENTE",
        "NO DOCENTE",
        "SEGURIDAD",
        "RECURSOS HUMANOS",
        "PERSONAL JERÁRQUICO",
        "ESTUDIANTE"
    )

    private var horariosList = listOf<HorarioModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_usuario)

        userModel = intent.getSerializableExtra("userModel") as UserModel

        val textoNombreUsuario = findViewById<TextView>(R.id.modificacion_titulo)
        textoNombreUsuario.text = "Modificando usuario:\n${userModel.getFullName()} "

        val spinner: Spinner = findViewById(R.id.tipo_cuenta)
        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, rolArrayList)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador

        nombreEditText = findViewById(R.id.nombre_texto)
        apellidoEditText = findViewById(R.id.apellido_texto)
        mailEditText = findViewById(R.id.email_texto)
        documentoEditText = findViewById(R.id.documento_texto)
        rolSpinner = spinner

        cargarDatos(userModel)
        agregarFiltros()
        agregarValidaciones()
        getHorarios()
    }

    private fun cargarDatos(userModel: UserModel) {
        runOnUiThread {
            nombreEditText.setText(userModel.nombre, TextView.BufferType.EDITABLE)
            apellidoEditText.setText(userModel.apellido, TextView.BufferType.EDITABLE)
            mailEditText.setText(userModel.email, TextView.BufferType.EDITABLE)
            documentoEditText.setText(userModel.dni.toString(), TextView.BufferType.EDITABLE)
            rolSpinner.setSelection(rolArrayList.indexOf(userModel.rol.uppercase(Locale.ENGLISH)))
        }
    }

    fun getHorarios() {
        RetrofitClient.horariosApiService.get().enqueue(object : Callback<List<HorarioModel>> {
            override fun onResponse(
                call: Call<List<HorarioModel>>,
                response: Response<List<HorarioModel>>
            ) {
                if (response.code() == 200) {
                    response.body()?.let {
                        Log.i("PerfilUsuario", it.toString())
                        horariosList = it
                        cargarHorariosSpinner()
                    }
                }
            }

            override fun onFailure(call: Call<List<HorarioModel>>, t: Throwable) {
                Log.e("PerfilUsuario", "getHorarios onFailure")
            }
        })
    }

    fun cargarHorariosSpinner() {
        val spinner: Spinner = findViewById(R.id.horario_edit)
        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, horariosList)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador
        horarioSpinner = spinner
        selectHorario()
    }

    fun selectHorario() {
        val index = horariosList.indexOfFirst { it._id == userModel.horarios[0] }
        if (index != -1) {
            horarioSpinner.setSelection(index)
        }
    }

    fun goToModificacionRol(view: View) {
        val intent = Intent(applicationContext, ModificacionRolActivity::class.java)
        startActivityForResult(intent, ROLE_REQUEST_CODE)
    }

    fun goToModificacionHora(view: View) {
        val intent = Intent(applicationContext, ModificacionHoraActivity::class.java)
        startActivityForResult(intent, HOURS_REQUEST_CODE)
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
        intent.putExtra("userId", userModel._id)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun agregarFiltros() {
        val letrasFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && !Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        val numerosFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    return@InputFilter ""
                }
            }
            null
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

    private fun enviarDatosModificacion() {
        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val rol = rolSpinner.selectedItem.toString().toLowerCase(Locale.ENGLISH)
        val email = mailEditText.text.toString()
        val horarioModel = horarioSpinner.selectedItem as HorarioModel

        val updatedUser = UserModel(userModel._id, nombre, apellido, documento.toInt(), rol, listOf(horarioModel._id), email)

        RetrofitClient.userApiService.put(userModel._id, updatedUser).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
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
                        Toast.makeText(this@ModificacionUsuarioActivity, "Error: durante la actualización", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ModificacionUsuarioActivity, "Fallo en la solicitud: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun goToModificacionExitosa() {
        val intent = Intent(applicationContext, RegistroExitoso2Activity::class.java)
        startActivity(intent)
    }

    private fun goToModificacionError() {
        val intent = Intent(applicationContext, RegistroDenegado2Activity::class.java)
        startActivity(intent)
    }
}
