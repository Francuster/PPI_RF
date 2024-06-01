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
import java.text.SimpleDateFormat
import java.util.Locale

class DocentesLicenciasActivity: AppCompatActivity() {
    val licenciasDocente = mutableListOf<Licencia>()
    private val client = OkHttpClient()
    private var licencesJSONArray = JSONArray()
    private var docentesJSONArray = JSONArray()
    private var jsonArray = JSONArray()
    private val handler = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.docentes)
        fetch("Teachers","/api/teachers","FetchTeachers")
        fetch("Licenses","/api/licences","FetchLicenses")

        scheduleUserUpdate()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    private fun scheduleUserUpdate() {
        runnable = Runnable {
            fetch("Teachers","/api/teachers","FetchTeachers")
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

            for (i in 0 until jsonArray.length()) {
                val lastUserJsonObject = jsonArray.getJSONObject(i)
                val userIdObject = lastUserJsonObject.getJSONObject("_id")
                val userId = userIdObject.getString("\$oid")
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
                        if(search == "Teachers"){
                            docentesJSONArray = jsonArray
                            mostrarTodosLosDocentes()
                        }
                        if(search == "Licenses"){
                            licencesJSONArray = jsonArray
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

            for (i in 0 until licencesJSONArray.length()) {
                val lastUserJsonObject = licencesJSONArray.getJSONObject(i)
                // Depuración: imprimir el JSON completo del objeto actual
                println("JSON del objeto actual: $lastUserJsonObject")
                
                val userIdObject = lastUserJsonObject.getJSONObject("userId")
                val teacherId = userIdObject.getString("\$oid")
                if (userId == teacherId) {
                    val dateDesdeObject = lastUserJsonObject.getJSONObject("fechaDesde")
                    val dateDesde = dateDesdeObject.getString("\$date")
                    val fechaDesde = convertDate(dateDesde)

                    val dateHastaObject = lastUserJsonObject.getJSONObject("fechaHasta")
                    val dateHasta = dateHastaObject.getString("\$date")
                    val fechaHasta = convertDate(dateHasta)

                    // Crear una nueva instancia de Licencia y añadirla a la lista
                    val licencia = Licencia(fechaDesde, fechaHasta, userId)
                    licenciasDocente.add(licencia)

                    println("Licencia añadida: $licencia")
                }
            }



            }
        }


    private fun convertDate(originalDate: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = originalFormat.parse(originalDate)
            targetFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}

