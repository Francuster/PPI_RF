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
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class EmpleadosLicenciasActivity: AppCompatActivity() {
    private val licenciasEmpleado = mutableListOf<Licencia>()
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private lateinit var empleadoBuscado: ArrayList<Empleado>
    private val client = OkHttpClient()
    private var licenciasJSONArray = JSONArray()
    private var jsonArray = JSONArray()
    private val handler = Handler()
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empleados)
        fetch("Licencias","/api/licencias","FetchLicencias")
        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()
        empleadoBuscado = ArrayList()
        scheduleUserUpdate()
        mostrarTodosLosEmpleados()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    private fun scheduleUserUpdate() {
        runnable = Runnable {
            fetch("Licencias","/api/licencias","FetchLicencias")
            // Vuelve a programar la actualización después de 10 segundos
            handler.postDelayed(runnable, 10000)
        }
        // Programa la primera ejecución después de 10 segundos
        handler.postDelayed(runnable, 10000)
    }

    private fun mostrarTodosLosEmpleados() {
        val container: LinearLayout = findViewById(R.id.container_empleado_licencias)

        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (empleado in listaEmpleados) {

                val userId = empleado.userId
                val fullName = empleado.fullName

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)

                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = fullName

                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    empleadoBuscado.add(empleado)
                    cargarLicenciasDelEmpleado(userId)
                    goToMostrarLicenciasDelEmpleado(empleado)
                }
            }
        }
    }

    private fun goToMostrarLicenciasDelEmpleado(empleado: Empleado) {
        val intent = Intent(applicationContext, LicenciasEmpleadoActivity::class.java)
        intent.putParcelableArrayListExtra("empleadoBuscado", ArrayList(empleadoBuscado))
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
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

    private fun cargarLicenciasDelEmpleado(userId: String) {
        runOnUiThread {
            for (i in 0 until licenciasJSONArray.length()) {
                val lastUserJsonObject = licenciasJSONArray.getJSONObject(i)
                val licenciaId = lastUserJsonObject.getString("_id")
                val userIdCheck = lastUserJsonObject.getString("userId")

                if (userId == userIdCheck) {
                    val fechaDesde = lastUserJsonObject.getString("fechaDesde")
                    val fechaHasta = lastUserJsonObject.getString("fechaHasta")

                    // Crear una nueva instancia de Licencia y añadirla a la lista
                    val licencia = Licencia(licenciaId,fechaDesde, fechaHasta, userId)
                    licenciasEmpleado.add(licencia)

                }
            }
        }
    }



    fun goToAtrasInicioRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}

