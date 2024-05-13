package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RegistroExitosoAntesala: AppCompatActivity() {
    var nombre = ""
    var apellido = ""
    var dni = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)

        val extras = intent.extras
        if (extras != null) {
            nombre = extras.getString("nombre").toString()
            apellido = extras.getString("apellido").toString()
            val rol = extras.getString("roles")
            dni = extras.getInt("dni").toString()
            //val byteArray = extras.getByteArray("imagen")

            // IMAGEN --- Decodificar el byteArray a un bitmap
            //val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

            // Mostrar el bitmap en el ImageView "escaneo"
            //val imageViewEscaneo = findViewById<ImageView>(R.id.escaneo)
            //imageViewEscaneo.setImageBitmap(bitmap)

            // ROL TIPO CUENTA --- Verificar si rolArray no es nulo ni está vacío
            /*if (!rolArray.isNullOrEmpty()) {
                val primerRol = rolArray[0] // Obtener el primer rol
                val textoTipoCuenta = findViewById<TextView>(R.id.tipo_cuenta)
                textoTipoCuenta.text = primerRol // Establecer el primer rol en el TextView "tipo_cuenta"
            }*/

            val textoTipoCuenta = findViewById<TextView>(R.id.tipo_cuenta_texto)
            textoTipoCuenta.text = "$rol"// Harcodear el texto para probar

            // NOMBRE Y APELLIDO --- Actualizar TextViews con el nombre y apellido
            val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_exitoso)
            textoNombreUsuario.text = "$nombre $apellido"
        }
    }

    fun Siguiente(view: View) {
        // Iniciar la actividad RegistroExitosoActivity
        val intent = Intent(applicationContext, RegistroExitosoActivity::class.java)
         val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
           val horario = formato.format(Date())
            enviarDatosAlBackend(nombre,apellido,dni, horario )
        startActivity(intent)
    }

    private fun enviarDatosAlBackend(nombre: String, apellido: String, dni: String, horario: String) {
        // URL
        val url = "https://log3r.up.railway.app/api/authentication/logs"
        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Crear el cuerpo de la solicitud HTTP
        val requestBody = FormBody.Builder()
            .add("mensaje","ingreso ${nombre} ${apellido}")
            .add("horario", horario)
            .add("dni",dni)
            .build()

        // Crea la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Ejecuta la solicitud de forma asíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                e.printStackTrace()
                showToastOnUiThread("No se ha podido hacer el registro")

            }

            override fun onResponse(call: Call, response: Response) {
                // Maneja la respuesta del servidor
                // Manejar la respuesta del servidor aquí
                if (response.isSuccessful) {
                    // La solicitud fue exitosa //
                    showToastOnUiThread("Registro exitoso")
                }
            }

            private fun showToastOnUiThread(message: String) {
                runOnUiThread {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }


        })
    }
    fun rechazarClick(view: View) {

        val intent = Intent(applicationContext, RegistroDenegadoActivity::class.java)
        startActivity(intent)
    }


}
