package com.example.myapplication.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LicenciasEmpleadoActivity : AppCompatActivity() {
    private lateinit var miVista: View
    private lateinit var licenciasEmpleado: ArrayList<Licencia>
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private lateinit var empleadoBuscado: ArrayList<Empleado>
    private lateinit var loadingOverlayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.licencias_empleado)

        // Inicializar vistas después de inflar el layout
        miVista = findViewById(R.id.layout_hijo)
        loadingOverlayout = findViewById(R.id.loading_overlayout)

        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text = InicioRrHhActivity.GlobalData.empleado!!.fullName

        licenciasEmpleado = intent.getParcelableArrayListExtra("licenciasEmpleado") ?: arrayListOf()
        listaEmpleados = intent.getParcelableArrayListExtra("listaEmpleados") ?: arrayListOf()
        empleadoBuscado = intent.getParcelableArrayListExtra("empleadoBuscado") ?: arrayListOf()

        // Obtén una referencia al TextView
        val empleadoLicenciasTitulo: TextView = findViewById(R.id.empleado_licencias_titulo)
        // Establece el nuevo texto
        val texto = "LICENCIAS DE :\n${empleadoBuscado[0].fullName}"
        empleadoLicenciasTitulo.text = texto

        mostrarTodasLasLicencias()
    }

    private fun aumentarOpacidad(segundos: Long) {
        runOnUiThread {
            val animator = ObjectAnimator.ofFloat(miVista, "alpha", 0.1f, 1f)
            animator.duration = segundos
            animator.start()
        }
    }

    private fun mostrarTodasLasLicencias() {
        val container: LinearLayout = findViewById(R.id.container)
        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaHoy = sdf.format(Calendar.getInstance().time)

            for (licencia in licenciasEmpleado) {
                val dateHoy = sdf.parse(fechaHoy)
                val dateDesde = sdf.parse(licencia.fechaDesde)
                val diasDeLicencia = "Del ${licencia.fechaDesde}  al ${licencia.fechaHasta}"

                if (dateDesde != null && dateDesde <= dateHoy) {
                    val inflater: LayoutInflater = LayoutInflater.from(this@LicenciasEmpleadoActivity)
                    val itemView: View = inflater.inflate(R.layout.licencia, container, false)
                    val textViewLicencia: TextView = itemView.findViewById(R.id.licencia)
                    textViewLicencia.text = diasDeLicencia
                    container.addView(itemView)
                } else {
                    val inflater: LayoutInflater = LayoutInflater.from(this@LicenciasEmpleadoActivity)
                    val itemView: View = inflater.inflate(R.layout.licencia_vigente, container, false)
                    val textViewLicenciaVigente: TextView = itemView.findViewById(R.id.licencia_vigente)
                    textViewLicenciaVigente.text = diasDeLicencia
                    container.addView(itemView)
                    itemView.findViewById<View>(R.id.imagen_delete).setOnClickListener {
                        eliminarLicencia(licencia._id)
                    }
                }
            }
            aumentarOpacidad(800L)
        }
    }

    fun goToCargarLicencia(view: View) {
        val intent = Intent(applicationContext, CargarLicenciaActivity::class.java)
        intent.putParcelableArrayListExtra("empleadoBuscado", ArrayList(empleadoBuscado))
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        startActivity(intent)
    }

    fun goToAtrasEmpleadosLicencias(view: View) {
        val intent = Intent(applicationContext, EmpleadosLicenciasActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        startActivity(intent)
    }

    private fun eliminarLicencia(licenciaId: String) {
        showLoadingOverlay()

        // Construir la URL para la solicitud HTTP
        val url = "${BuildConfig.BASE_URL}/api/licencias/$licenciaId"
        val client = OkHttpClient()

        // Construir la solicitud HTTP DELETE
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Content-Type", "application/json")
            .build()

        // Realizar la solicitud HTTP asíncronamente
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el caso en que la solicitud falle
                runOnUiThread {
                    hideLoadingOverlay()
                    Log.e("HTTP DELETE Error", e.message ?: "Unknown error")
                    Toast.makeText(this@LicenciasEmpleadoActivity, "Error al eliminar la licencia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta recibida del servidor
                runOnUiThread {
                    hideLoadingOverlay()
                }
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@LicenciasEmpleadoActivity, "Licencia eliminada exitosamente", Toast.LENGTH_SHORT).show()
                        // Iterar sobre la lista y eliminar la licencia con el ID deseado
                        licenciasEmpleado.removeAll { it._id == licenciaId }
                        EmpleadosLicenciasActivity.GlobalData.licencias.removeAll { it._id == licenciaId }
                        mostrarTodasLasLicencias()
                    }
                } else {
                    runOnUiThread {
                        Log.e("HTTP DELETE Error", "Error: ${response.code} - ${response.message}")
                        Toast.makeText(this@LicenciasEmpleadoActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
}
