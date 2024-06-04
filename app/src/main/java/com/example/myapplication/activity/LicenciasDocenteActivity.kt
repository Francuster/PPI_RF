package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Licencia

class LicenciasDocenteActivity: AppCompatActivity() {
    private lateinit var licenciasDocente: ArrayList<Licencia>
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.licencias_docente)
        userId = intent.getStringExtra("user_id")
        licenciasDocente = intent.getParcelableArrayListExtra<Licencia>("licenciasDocente") ?: arrayListOf()
        mostrarTodasLasLicencias()
    }



    private fun mostrarTodasLasLicencias() {
        val container: LinearLayout = findViewById(R.id.container)
        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (licencia in licenciasDocente) {

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
        intent.putExtra("user_id", userId)
        intent.putParcelableArrayListExtra("licenciasDocente", ArrayList(licenciasDocente))
        startActivity(intent)
    }

    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, DocentesLicenciasActivity::class.java)
        startActivity(intent)
    }



}