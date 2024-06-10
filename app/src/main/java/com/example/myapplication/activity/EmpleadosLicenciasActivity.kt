package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class EmpleadosLicenciasActivity: AppCompatActivity() {
    private  var licenciasEmpleado = ArrayList<Licencia>()
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private lateinit var empleadoBuscado: ArrayList<Empleado>
    private val client = OkHttpClient()
    object GlobalData {
        var licencias  = ArrayList<Licencia>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empleados)
        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text =  InicioRrHhActivity.GlobalData.empleado!!.fullName
        if(GlobalData.licencias.isEmpty()){
            fetch("Licencias","/api/licencias","FetchLicencias")

        }
        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()
        empleadoBuscado = ArrayList()
        mostrarTodosLosEmpleados()
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
                        if (search == "Licencias") {
                            try {
                                // Convertir la respuesta JSON a ArrayList<Licencia> usando Gson
                                val gson = Gson()
                                val type = object : TypeToken<ArrayList<Licencia>>() {}.type
                                GlobalData.licencias = gson.fromJson(it, type)
                                // Mostrar lista convertida en los logs
                                for (licencia in GlobalData.licencias) {
                                    Log.d(tag, "Licencia: $licencia")
                                }
                            } catch (e: Exception) {
                                Log.e(tag, "Error parsing JSON response", e)
                            } finally {
                                response.body?.close() // Cerrar el cuerpo de la respuesta
                            }
                        }
                    }
                } else {
                    Log.e(tag, "Unsuccessful response")
                    response.body?.close() // Cerrar el cuerpo de la respuesta en caso de respuesta no exitosa
                }
            }

        })
    }

    private fun cargarLicenciasDelEmpleado(userId: String) {
        runOnUiThread {
            for (licencia  in GlobalData.licencias){

                val licenciaId = licencia._id
                val userIdCheck = licencia.userId

                if (userId == userIdCheck) {
                    val fechaDesde = licencia.fechaDesde
                    val fechaHasta = licencia.fechaHasta

                    // Crear una nueva instancia de Licencia y añadirla a la lista
                    val nuevaLicencia = Licencia(licenciaId,fechaDesde, fechaHasta, userId)
                    licenciasEmpleado.add(nuevaLicencia)

                }
            }
        }
    }



    fun goToAtrasInicioRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}

