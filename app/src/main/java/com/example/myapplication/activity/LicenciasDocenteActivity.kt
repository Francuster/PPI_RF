package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class LicenciasDocenteActivity: AppCompatActivity() {
    private val client = OkHttpClient()
    private var jsonArray = JSONArray()
    private val handler = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.docentes)
        fetchLicences()

        // Programa la actualización de usuarios cada 10 segundos
        scheduleUserUpdate()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    private fun scheduleUserUpdate() {
        runnable = Runnable {
            fetchLicences()
            // Vuelve a programar la actualización después de 10 segundos
            handler.postDelayed(runnable, 10000)
        }
        // Programa la primera ejecución después de 10 segundos
        handler.postDelayed(runnable, 10000)
    }

    private fun mostrarTodasLasLicencias() {
        val container: LinearLayout = findViewById(R.id.container)

        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (i in 0 until jsonArray.length()) {
                val lastUserJsonObject = jsonArray.getJSONObject(i)
                val userIdObject = lastUserJsonObject.getJSONObject("_id")
                val userId = userIdObject.getString("\$oid")
                val fechaDesde = lastUserJsonObject.getString("fechaDesde")
                val fechaHasta = lastUserJsonObject.getString("fechaHasta")
                val licencia = "$fechaDesde $fechaHasta"

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)

                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = licencia

                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    goToCarcarLicencia(userId)
                }
            }
        }
    }

    private fun fetchLicences() {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "/api/licences") // Cambia esto por la URL de tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("FetchLicense", "Failed to fetch license", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        Log.d("FetchLicense", "Response data: $it")
                        jsonArray = JSONArray(it)
                        mostrarTodasLasLicencias()
                    }
                } else {
                    Log.e("FetchLicense", "Unsuccessful response")
                }
            }
        })
    }

    private fun goToCarcarLicencia(userId: String) {
        val intent = Intent(applicationContext, CargarLicenciaActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

}