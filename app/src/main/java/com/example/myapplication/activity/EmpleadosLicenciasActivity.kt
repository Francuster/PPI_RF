package com.example.myapplication.activity

import android.animation.ObjectAnimator
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
    private lateinit var miVista : View
    private var segundos = 500L
    private lateinit var loadingOverlayout: View
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
        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()
        empleadoBuscado = ArrayList()
        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text =  InicioRrHhActivity.GlobalData.empleado!!.fullName
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        miVista = findViewById(R.id.layout_hijo)
        miVista.alpha = 0.1f

        if(GlobalData.licencias.isEmpty() || listaEmpleados.size != InicioRrHhActivity.GlobalData.cantEmpleados){
            fetch("Licencias","/api/licencias","FetchLicencias")
            InicioRrHhActivity.GlobalData.cantEmpleados = listaEmpleados.size
        }else{
            segundos = 800L
            mostrarTodosLosEmpleados()
        }
    }

    private fun aumentarOpacidad(segundos:Long){
        runOnUiThread {
            val animator = ObjectAnimator.ofFloat(miVista, "alpha", 0.1f, 1f)
            animator.duration = segundos
            animator.start()
        }
    }

    private fun showLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.GONE
        }
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

                // Disable click event for the TextView
                textViewEmpleado.isClickable = false
                textViewEmpleado.isFocusable = false

                // Ocultar imagenes de ojo y editar
                val imagenOjo: View = itemView.findViewById(R.id.imagen_ojo)
                val imagenEditar: View = itemView.findViewById(R.id.imagen_editar)
                imagenOjo.visibility = View.GONE
                imagenEditar.visibility = View.GONE

                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    empleadoBuscado.clear()
                    empleadoBuscado.add(empleado)
                    cargarLicenciasDelEmpleado(userId)
                    goToMostrarLicenciasDelEmpleado(empleado)
                }
            }

            aumentarOpacidad(segundos)
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
        showLoadingOverlay()
        miVista.alpha = 0.1f
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + endpoint) // Cambia esto por la URL de tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e(tag, "Failed to fetch $search", e)
                hideLoadingOverlay()
                runOnUiThread {
                    aumentarOpacidad(segundos)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        hideLoadingOverlay()
                        Log.d(tag, "Response data: $it")
                        if (search == "Licencias") {
                            try {
                                // Convertir la respuesta JSON a ArrayList<Licencia> usando Gson
                                val gson = Gson()
                                val type = object : TypeToken<ArrayList<Licencia>>() {}.type
                                GlobalData.licencias = gson.fromJson(it, type)
                                mostrarTodosLosEmpleados()
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
                    hideLoadingOverlay()
                    runOnUiThread {
                        aumentarOpacidad(segundos)
                    }
                    Log.e(tag, "Unsuccessful response")
                    response.body?.close() // Cerrar el cuerpo de la respuesta en caso de respuesta no exitosa
                }
            }
        })
    }

    private fun cargarLicenciasDelEmpleado(userId: String) {
        licenciasEmpleado.clear() // Clear the previous licenses
        for (licencia in GlobalData.licencias) {
            if (licencia.userId == userId) {
                licenciasEmpleado.add(licencia)
            }
        }
    }

    fun goToAtrasInicioRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }
}
