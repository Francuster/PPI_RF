package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class RegistroRrHhPrimeraSalaActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var imagenBase64: String? = null
    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_primera_sala_rrhh)
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
            }
        }
    }

    fun enviarDatosRegistro(view: View) {
        val nombreEditText = findViewById<EditText>(R.id.nombre_texto)
        val apellidoEditText = findViewById<EditText>(R.id.apellido_texto)
        val documentoEditText = findViewById<EditText>(R.id.documento_texto)
        val tipoCuentaSpinner = findViewById<Spinner>(R.id.tipo_cuenta)
        val horaEntradaEditText = findViewById<EditText>(R.id.hora_entrada)
        val horaSalidaEditText = findViewById<EditText>(R.id.hora_salida)

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()
        val horaEntrada = horaEntradaEditText.text.toString()
        val horaSalida = horaSalidaEditText.text.toString()

        val json = JSONObject().apply {
            put("nombre", nombre)
            put("apellido", apellido)
            put("dni", documento)
            put("rol", tipoCuenta)
            put("horariosEntrada", horaEntrada)
            put("horariosSalida", horaSalida)
            imagenBase64?.let { put("imagen", it) }
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://tu-backend/api/users") // Cambia por tu URL local u online
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    goToRegistroExitoso()
                } else {
                    goToRegistroDenegado()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegistroRrHhPrimeraSalaActivity, "Fallo en la solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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

}
