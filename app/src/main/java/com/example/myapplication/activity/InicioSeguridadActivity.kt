package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.activity.QRScannerActivity
import com.example.myapplication.model.Empleado
import com.example.myapplication.utils.deviceIsConnected


class InicioSeguridadActivity: AppCompatActivity() {

        object GlobalData {
            var seguridad: Empleado? = null
        }
        private var nombre: String? = null
        private var apellido: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.inicio_seguridad)

            if (GlobalData.seguridad == null) {
                nombre = intent.getStringExtra("nombre")
                apellido = intent.getStringExtra("apellido")//nombre para mostrar
                GlobalData.seguridad = Empleado(fullName = "$nombre $apellido", userId = "124124")

            }

            val textoNombreUsuario = findViewById<TextView>(R.id.seguridad)
            textoNombreUsuario.text = GlobalData.seguridad!!.fullName

        }
        override fun onResume() {
            super.onResume()
            setContentView(R.layout.inicio_seguridad)
            val textoNombreUsuario = findViewById<TextView>(R.id.seguridad)
            textoNombreUsuario.text = GlobalData.seguridad!!.fullName
        }

    fun goToAnteEscanea(view: View) {
        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, QRScannerActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToFormulario(view: View) {
        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, FormularioOfflineActivity::class.java)
            startActivity(intent)
        }

    }

    fun goToQREspecial(view: View) {
        if(deviceIsConnected(applicationContext)){
            Toast.makeText(this, "Estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, QRScannerActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToFormEspecial(view: View) {
        if(deviceIsConnected(applicationContext)){
            Toast.makeText(this, "Estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, FormularioOfflineActivity::class.java)
            startActivity(intent)
        }
    }
    fun goToReporteSeguridad(view: View) {
        val intent = Intent(applicationContext, ReportesSeguridadActivity::class.java)
        startActivity(intent)
    }


}



