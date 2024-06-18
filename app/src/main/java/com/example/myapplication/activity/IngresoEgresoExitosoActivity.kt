package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class IngresoEgresoExitosoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_exitoso)

        // Obtener el TextView donde se mostrará el mensaje
        val mensajeTextView = findViewById<TextView>(R.id.ingreso_denegado)

        // Obtener la acción desde el Intent
        val action = intent.getStringExtra("action")

        // Establecer el texto del TextView basado en la acción
        if (action == "Egreso") {
            mensajeTextView.text = "EGRESO EXITOSO"
        } else if (action == "Ingreso") {
            mensajeTextView.text = "INGRESO EXITOSO"
        }
    }

    fun Siguiente(view : View){

        val intent = Intent(this, InicioSeguridadActivity::class.java)
        startActivity(intent)

        }
    }
