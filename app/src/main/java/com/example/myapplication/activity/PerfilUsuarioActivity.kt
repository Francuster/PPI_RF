package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userSurname: String
    private lateinit var horaEntrada: String
    private lateinit var horaSalida: String
    private lateinit var rol: String
    private lateinit var imagenUsuarioImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario) // Nombre corregido

        userId = intent.getStringExtra("user_id") ?: ""

        val textoNombreUsuario = findViewById<TextView>(R.id.nombre_texto)
        val horaEntradaTextView = findViewById<TextView>(R.id.hora_entrada)
        val horaSalidaTextView = findViewById<TextView>(R.id.hora_salida)
        val rolTextView = findViewById<TextView>(R.id.rol_texto)
        imagenUsuarioImageView = findViewById(R.id.imagenPerfil)

        obtenerDatosUsuario(userId, textoNombreUsuario, horaEntradaTextView, horaSalidaTextView, rolTextView)
    }

    private fun obtenerDatosUsuario(userId: String, textoNombreUsuario: TextView, horaEntradaTextView: TextView, horaSalidaTextView: TextView, rolTextView: TextView) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}/api/user/$userId")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PerfilUsuarioActivity, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@PerfilUsuarioActivity, "Error al cargar los datos: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonObject = JSONObject(responseData)
                        userName = jsonObject.getString("userName")
                        userSurname = jsonObject.getString("userSurname")
                        horaEntrada = jsonObject.getString("horaEntrada")
                        horaSalida = jsonObject.getString("horaSalida")
                        rol = jsonObject.getString("rol")

                        runOnUiThread {
                            textoNombreUsuario.text = "$userName $userSurname"
                            horaEntradaTextView.text = "Hora de entrada: $horaEntrada"
                            horaSalidaTextView.text = "Hora de salida: $horaSalida"
                            rolTextView.text = "Rol: $rol"
                        }
                    }
                }
            }
        })
    }

    /*
    private fun obtenerImagenUsuario(userId: String) {
        imagenesApiService.getImagenes(userId).enqueue(object : Callback<List<ImagenModel>> {
            override fun onResponse(call: Call<List<ImagenModel>>, response: Response<List<ImagenModel>>) {
                if (response.isSuccessful) {
                    val imagenModels = response.body()
                    if (!imagenModels.isNullOrEmpty()) {
                        val imageUrl = imagenModels[0].imageUrl
                        // Cargar la imagen con Glide o de manera nativa
                        cargarImagenDesdeUrl(imageUrl)
                    }
                } else {
                    val statusCode = response.code()
                    val message = "Error al cargar los datos: $statusCode"
                    Toast.makeText(this@PerfilUsuarioActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ImagenModel>>, t: Throwable) {
                val errorMessage = "Error al cargar los datos: ${t.message}"
                Toast.makeText(this@PerfilUsuarioActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarImagenDesdeUrl(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                withContext(Dispatchers.Main) {
                    imagenUsuarioImageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    */

    fun goToAtrasIniRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

    fun goToPerfilUsuario(view: View) {
        val intent = Intent(applicationContext, PerfilUsuarioActivity::class.java)
        startActivity(intent)
    }
}
