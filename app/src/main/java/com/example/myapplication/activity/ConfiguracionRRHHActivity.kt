package com.example.myapplication.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.ConfiguracionModel
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.imageToggleAtras
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfiguracionRRHHActivity : AppCompatActivity() {

        private lateinit var seekBar: SeekBar
        private lateinit var textCerteza: TextView
        private var certeza: Double = 0.0

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.configuracion_rrhh)

                seekBar = findViewById(R.id.seekBar)
                textCerteza = findViewById(R.id.certeza_configuracion) // Inicializar textCerteza después de setContentView()
                val imageView = findViewById<ImageView>(R.id.imagen_volver)
                imageToggleAtras(imageView,applicationContext,"irInicioRrHhActivity",ArrayList<Empleado>(),ArrayList<Licencia>(),ArrayList<Empleado>())
                onChangeSeekBar()
                obtenerCerteza() // Llamar a obtenerCerteza() después de inicializar textCerteza
        }


        private fun onChangeSeekBar(){
                seekBar.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener{
                        override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                        ) {
                                val certezaDouble = progress.toDouble() / 100
                                textCerteza.text =  certezaDouble.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                
                        }
                })
        }


        private fun obtenerCerteza() {

                RetrofitClient.configuracionesApiService.getByName("certeza").enqueue(object: Callback<ConfiguracionModel>{
                        override fun onResponse(
                                call: Call<ConfiguracionModel>,
                                response: Response<ConfiguracionModel>
                        ) {
                                if (response.isSuccessful){

                                        val configuracionModel = response.body() as ConfiguracionModel
                                        val progress = (configuracionModel.valor * 100).toInt()

                                        runOnUiThread {
                                                seekBar.progress = progress

                                                // Actualizar el TextView con el valor de la certeza
                                                textCerteza.text = configuracionModel.valor.toString()

                                                // Mostrar Toast con la certeza actual
                                                val certezaActual = "Su certeza actual es de: ${configuracionModel.valor}"
                                                mostrarToast(certezaActual)
                                        }
                                }

                        }

                        override fun onFailure(call: Call<ConfiguracionModel>, t: Throwable) {
                                t.printStackTrace()
                        }
                })

        }

        fun configurarCerteza(view: View) {
                val nuevoValor = seekBar.progress.toDouble() / 100

                val configuracionModel = ConfiguracionModel("", "certeza", nuevoValor)

                RetrofitClient.configuracionesApiService.put(configuracionModel).enqueue(object : Callback<Void>{
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                        obtenerCerteza()
                                        mostrarToast("Certeza configurada, su certeza ahora es de: $nuevoValor")
                                } else {
                                        obtenerCerteza()
                                        mostrarToast("El valor de certeza no se cambió debido a un error")
                                }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                t.printStackTrace()
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
