package com.example.myapplication.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.deviceIsConnected


class InicioSeguridadActivity: AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.inicio_seguridad)

        }
        override fun onResume() {
            super.onResume()
            setContentView(R.layout.inicio_seguridad)
        }

    fun goToAnteEscanea(view: View) {
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
    fun goToReporteSeguridad(view: View) {
        val intent = Intent(applicationContext, ReportesSeguridadActivity::class.java)
        startActivity(intent)
    }


}



