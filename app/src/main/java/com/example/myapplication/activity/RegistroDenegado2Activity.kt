package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class RegistroDenegado2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_denegado)

        // Actualizar TextViews estado
        val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_denegado)

        // Obtener los valores de los extras del intent
        val error = intent.getBooleanExtra("error", false)
        val origen = intent.getStringExtra("origen")

        if (error && origen == "ModificacionUsuarioActivity") {
            textoNombreUsuario.text = "MODIFICACIÓN RECHAZADA"
        } else {
            textoNombreUsuario.text = "REGISTRO DENEGADO"
        }
    }

    fun Siguiente(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }
}
