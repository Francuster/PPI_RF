package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class ReportesSeguridadActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_seguridad)
    }

    fun goToInicioSeguridad(view: View) {

        val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
        startActivity(intent)

    }
}