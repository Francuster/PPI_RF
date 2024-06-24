package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class IngresoDenegadoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_denegado)
    }

    fun Siguiente(view: View) {
        // Verifica si el Intent proviene de CameraxAuthenticationActivity
        val fromCameraxAuthentication = intent.getBooleanExtra("fromCameraxAuthentication", false)

        if (fromCameraxAuthentication) {
            val intent = Intent(this, InicioSeguridadActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
