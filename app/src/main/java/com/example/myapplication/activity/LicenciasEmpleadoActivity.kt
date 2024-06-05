package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia

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

            for (licencia in licenciasEmpleado) {

                val diasDeLicencia = "Del ${licencia.fechaDesde}  al ${licencia.fechaHasta}"

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.licencia, container, false)

                val textViewLicencia: TextView = itemView.findViewById(R.id.licencia)
                textViewLicencia.text = diasDeLicencia
                container.addView(itemView)

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



}