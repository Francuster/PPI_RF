package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.*
import java.io.IOException

class ConfiguracionRRHHActivity : AppCompatActivity() {

        private lateinit var seekBar: SeekBar
        private lateinit var client: OkHttpClient
        private lateinit var textCerteza: TextView
        private var certeza: Double = 0.0

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.configuracion_rrhh)

                seekBar = findViewById(R.id.seekBar)
                client = OkHttpClient()
                textCerteza = findViewById(R.id.certeza_configuracion) // Inicializar textCerteza después de setContentView()

                obtenerCerteza() // Llamar a obtenerCerteza() después de inicializar textCerteza
        }

        fun goBackInicioRRHH(view: View) {
                val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
                startActivity(intent)
        }

        private fun obtenerCerteza() {
                val request = Request.Builder()
                        .url("${BuildConfig.BASE_URL}/api/certeza")
                        .build()

                client.newCall(request).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                                val body = response.body?.string()
                                body?.let {
                                        try {
                                                val certezaValor: Double = it.toDouble()
                                                val progress = (certezaValor * 100).toInt()

                                                runOnUiThread {
                                                        seekBar.progress = progress

                                                        // Actualizar el TextView con el valor de la certeza
                                                        textCerteza.text = certezaValor.toString()

                                                        // Mostrar Toast con la certeza actual
                                                        val certezaActual = "Su certeza actual es de: $certezaValor"
                                                        mostrarToast(certezaActual)
                                                }
                                        } catch (e: NumberFormatException) {
                                                e.printStackTrace()
                                        }
                                }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                        }
                })
        }

        fun configurarCerteza(view: View) {
                val nuevoValor = seekBar.progress.toDouble() / 100

                val requestBody = FormBody.Builder()
                        .add("valor", nuevoValor.toString())
                        .build()

                val request = Request.Builder()
                        .url("${BuildConfig.BASE_URL}/api/certeza")
                        .put(requestBody)
                        .build()

                client.newCall(request).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                        obtenerCerteza()
                                        mostrarToast("Certeza configurada, su certeza ahora es de: $nuevoValor")
                                } else {
                                        obtenerCerteza()
                                        mostrarToast("El valor de certeza no se cambió debido a un error")
                                }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                                mostrarToast("El valor de certeza no se cambió debido a un error")
                        }
                })
        }

        private fun mostrarToast(mensaje: String) {
                runOnUiThread {
                        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
                }
        }
}
