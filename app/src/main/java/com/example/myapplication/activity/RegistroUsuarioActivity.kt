package com.example.myapplication.activity


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import android.widget.Spinner
import android.widget.Toast
import com.example.myapplication.BuildConfig
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar


class RegistroUsuarioActivity:AppCompatActivity() {
    private val client = OkHttpClient()
    private var imagenBase64: String? = null
    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.registro_primera_sala_rrhh)
        val spinner:Spinner = findViewById<Spinner>(R.id.tipo_cuenta)
        var elementos=ArrayList<String>()

        elementos.add("ESTUDIANTE")
        elementos.add("PROFESOR")
        elementos.add("SEGURIDAD")
        elementos.add("RRHH")
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

    }



    fun goToCamaraParaRegistro(view: View) {
        val intent = Intent(this, CamaraParaRegistroRrHhActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val byteArray = data?.getByteArrayExtra("image")
            if (byteArray != null) {
                imagenBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                enviarDatosRegistro()
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

        val json = JSONObject().apply {
            put("nombre", nombre)
            put("apellido", apellido)
            put("dni", documento)
            put("rol", tipoCuenta)
            put("horariosEntrada", horaEntrada)
            put("horariosSalida", horaSalida)
            put("email", email)
            imagenBase64?.let { put("imagen", it) }
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL+"/api/users")//IP LOCAL U ONLINE
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@RegistroUsuarioActivity, "EXITO EN LA SOLICITUD USUARIO REGISTRADO", Toast.LENGTH_SHORT).show()
                    }
                    goToRegistroExitoso()
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