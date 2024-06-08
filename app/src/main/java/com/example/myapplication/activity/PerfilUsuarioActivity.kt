package com.example.myapplication.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.ImagenModel
import com.example.myapplication.service.ImagenesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PerfilUsuarioActivity : AppCompatActivity() {
/*
    private val imagenesApiService: ImagenesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImagenesApiService::class.java)
    }

    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userSurname: String
    private lateinit var horaEntrada: String
    private lateinit var horaSalida: String
    private lateinit var rol: String
    private lateinit var imagenUsuarioImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfilUsuario)

        userId = intent.getStringExtra("user_id") ?: ""
        userName = intent.getStringExtra("user_name") ?: ""
        userSurname = intent.getStringExtra("user_surname") ?: ""
        horaEntrada = intent.getStringExtra("hora_entrada") ?: ""
        horaSalida = intent.getStringExtra("hora_salida") ?: ""
        rol = intent.getStringExtra("rol") ?: ""

        val textoNombreUsuario = findViewById<TextView>(R.id.nombre_texto)
        val horaEntradaTextView = findViewById<TextView>(R.id.apellido_texto)
        val horaSalidaTextView = findViewById<TextView>(R.id.hora_salida)
        val rolTextView = findViewById<TextView>(R.id.hora_entrada)
        imagenUsuarioImageView = findViewById(R.id.imagenPerfil)

        textoNombreUsuario.text = "$userName $userSurname"
        horaEntradaTextView.text = "Hora de entrada: $horaEntrada"
        horaSalidaTextView.text = "Hora de salida: $horaSalida"
        rolTextView.text = "Rol: $rol"

        obtenerImagenUsuario(userId)
    }

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
    }*/
}
