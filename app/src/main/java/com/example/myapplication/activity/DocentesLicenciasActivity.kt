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
import com.example.myapplication.model.Licencia
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class DocentesLicenciasActivity: AppCompatActivity() {
    val licenciasDocente = mutableListOf<Licencia>()
    private val client = OkHttpClient()
    private var licenciasJSONArray = JSONArray()
    private var docentesJSONArray = JSONArray()
    private var jsonArray = JSONArray()
    private val handler = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.docentes)
        fetch("Profesores","/api/profesores","FetchProfesores")
        fetch("Licencias","/api/licencias","FetchLicencias")

        scheduleUserUpdate()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    private fun scheduleUserUpdate() {
        runnable = Runnable {
            fetch("Profesores","/api/profesores","FetchProfesores")
            // Vuelve a programar la actualización después de 10 segundos
            handler.postDelayed(runnable, 10000)
        }
        // Programa la primera ejecución después de 10 segundos
        handler.postDelayed(runnable, 10000)
    }

    private fun mostrarTodosLosDocentes() {
        val container: LinearLayout = findViewById(R.id.container)

        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (i in 0 until docentesJSONArray.length()) {
                val lastUserJsonObject = docentesJSONArray.getJSONObject(i)
                val userId = lastUserJsonObject.getString("_id")
                val userName = lastUserJsonObject.getString("nombre")
                val userSurname = lastUserJsonObject.getString("apellido")
                val fullName = "$userName $userSurname"

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)

                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = fullName

                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    cargarLicenciasDelDocente(userId)
                    goToMostrarLicenciasDelDocente(userId)
                }
            }
        }
    }

    private fun goToMostrarLicenciasDelDocente(userId: String) {
        val intent = Intent(applicationContext, LicenciasDocenteActivity::class.java)
        intent.putExtra("user_id",userId)
        intent.putParcelableArrayListExtra("licenciasDocente", ArrayList(licenciasDocente))
        startActivity(intent)

    }

    private fun fetch(search: String, endpoint: String, tag: String) {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + endpoint) // Cambia esto por la URL de tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e(tag, "Failed to fetch $search", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        Log.d(tag, "Response data: $it")
                        jsonArray = JSONArray(it)
                        if(search == "Profesores"){
                            docentesJSONArray = jsonArray
                            mostrarTodosLosDocentes()
                        }
                        if(search == "Licencias"){
                            licenciasJSONArray = jsonArray
                        }

                    }
                } else {
                    Log.e(tag, "Unsuccessful response")
                }
            }
        })
    }

    private fun cargarLicenciasDelDocente(userId: String) {
        runOnUiThread {
            for (i in 0 until licenciasJSONArray.length()) {
                val lastUserJsonObject = licenciasJSONArray.getJSONObject(i)
                // Depuración: imprimir el JSON completo del objeto actual
                println("JSON del objeto actual: $lastUserJsonObject")

                val teacherId = lastUserJsonObject.getString("userId")

                if (userId == teacherId) {
                    val fechaDesde = lastUserJsonObject.getString("fechaDesde")
                    val fechaHasta = lastUserJsonObject.getString("fechaHasta")

                    // Crear una nueva instancia de Licencia y añadirla a la lista
                    val licencia = Licencia(fechaDesde, fechaHasta, userId)
                    licenciasDocente.add(licencia)

                    println("Licencia añadida: $licencia")
                }
            }
        }
    }



    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}

