/*package com.example.myapplication.activity

CLASE DEPRECADA LA VERDADERA ES REGISRO USUARIO


import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class RegistroRrHhPrimeraSalaActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var imageByteArray: ByteArray? = null
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
                imageByteArray = byteArray
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

        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val documento = documentoEditText.text.toString()
        val tipoCuenta = tipoCuentaSpinner.selectedItem.toString()
        val horaEntrada = horaEntradaEditText.text.toString()
        val horaSalida = horaSalidaEditText.text.toString()

        val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        multipartBodyBuilder.addFormDataPart("nombre", nombre)
        multipartBodyBuilder.addFormDataPart("apellido", apellido)
        multipartBodyBuilder.addFormDataPart("dni", documento)
        multipartBodyBuilder.addFormDataPart("rol", tipoCuenta)
        multipartBodyBuilder.addFormDataPart("horariosEntrada", horaEntrada)
        multipartBodyBuilder.addFormDataPart("horariosSalida", horaSalida)

        imageByteArray?.let {
            val tempFile = File.createTempFile("image", ".jpg", cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(it)
            }
            multipartBodyBuilder.addFormDataPart("imagen", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }

        val requestBody = multipartBodyBuilder.build()
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "/api/users") // Cambia por tu URL local u online
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    when (response.code) {
                        200 -> goToRegistroExitoso()
                        400 -> Toast.makeText(this@RegistroRrHhPrimeraSalaActivity, "Debe llenar todos los campos y tomar la foto.", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(this@RegistroRrHhPrimeraSalaActivity, "Error interno del servidor.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@RegistroRrHhPrimeraSalaActivity, "Fallo en la solicitud: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
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
*/
