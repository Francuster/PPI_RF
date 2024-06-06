package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Calendar

class LicenciasEmpleadoActivity: AppCompatActivity() {
    private lateinit var licenciasEmpleado: ArrayList<Licencia>
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private var empleado: Empleado? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.licencias_empleado)
        empleado = intent.getParcelableExtra("empleado")
        licenciasEmpleado = intent.getParcelableArrayListExtra<Licencia>("licenciasEmpleado") ?: arrayListOf()
        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()

        // Obtén una referencia al TextView
        val empleadoLicenciasTitulo: TextView = findViewById(R.id.empleado_licencias_titulo)
        // Establece el nuevo texto
        val texto = "LICENCIAS DE :\n${empleado?.fullName}"
        empleadoLicenciasTitulo.text = texto
        mostrarTodasLasLicencias()
    }



    private fun mostrarTodasLasLicencias() {
        val container: LinearLayout = findViewById(R.id.container)
        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas
            val fechaHoy = Calendar.getInstance().toString()
            for (licencia in licenciasEmpleado) {

                val diasDeLicencia = "Del ${licencia.fechaDesde}  al ${licencia.fechaHasta}"
                if (licencia.fechaDesde >= fechaHoy){
                    val inflater: LayoutInflater = LayoutInflater.from(this)
                    val itemView: View = inflater.inflate(R.layout.licencia, container, false)
                    val textViewLicencia: TextView = itemView.findViewById(R.id.licencia)
                    textViewLicencia.text = diasDeLicencia
                    container.addView(itemView)
                }
                else {
                    val inflater: LayoutInflater = LayoutInflater.from(this)
                    val itemView: View = inflater.inflate(R.layout.licencia_vigente, container, false)
                    val textViewLicenciaVigente: TextView = itemView.findViewById(R.id.licencia_vigente)
                    textViewLicenciaVigente.text = diasDeLicencia
                    container.addView(itemView)
                    itemView.findViewById<View>(R.id.imagen_delete).setOnClickListener {
                        eliminarLicencia(licencia.licenciaId)

                    }
                }

            }
        }
    }


    fun goToCargarLicencia(view: View) {
        val intent = Intent(applicationContext, CargarLicenciaActivity::class.java)
        intent.putExtra("empleado", empleado)
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
        startActivity(intent)
    }

    fun goToAtrasEmpleadosLicencias(view: View) {
        val intent = Intent(applicationContext, EmpleadosLicenciasActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        startActivity(intent)
    }

    private fun eliminarLicencia(licenciaId: String) {
        // Construir la URL para la solicitud HTTP
        val url = "${BuildConfig.BASE_URL}/api/licencias/$licenciaId/"
        val client = OkHttpClient()

        // Construir la solicitud HTTP DELETE
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        // Realizar la solicitud HTTP asíncronamente
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el caso en que la solicitud falle
                runOnUiThread {
                    Toast.makeText(this@LicenciasEmpleadoActivity, "Error al eliminar la licencia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta recibida del servidor
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@LicenciasEmpleadoActivity, "Licencia eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LicenciasEmpleadoActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}