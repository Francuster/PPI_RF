package com.example.myapplication.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.changeTextColorTemporarily

class RegistroDenegado2Activity : AppCompatActivity() {
    private lateinit var boton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_denegado)

        // Actualizar TextViews estado
        val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_denegado)
        val botonSiguiente = findViewById<Button>(R.id.boton_siguiente) // Asumiendo que el botón tiene este ID

        // Obtener los valores de los extras del intent
        val error = intent.getBooleanExtra("error", false)
        val origen = intent.getStringExtra("origen")

        if (error && origen == "ModificacionUsuarioActivity") {
            textoNombreUsuario.text = "MODIFICACIÓN RECHAZADA"
        } else if (origen == "RegistroExitosoAntesalaRrHh") {
            textoNombreUsuario.text = "INGRESO DENEGADO"
        } else {
            textoNombreUsuario.text = "REGISTRO DENEGADO"
        }

        // Configurar el texto del botón según el origen
        when (origen) {
            "RegistroUsuario", "RegistroExitosoAntesalaRrHh" -> {
                botonSiguiente.text = "Volver a Inicio"
            }
            else -> {
                botonSiguiente.text = "Volver a Inicio"
            }
        }
    }


    fun Siguiente(view: View) {
        boton =  findViewById(R.id.boton_siguiente)
        boton.changeTextColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms)
        val origen = intent.getStringExtra("origen")

        when (origen) {
            "RegistroExitosoAntesalaRrHh"-> {
                val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
                startActivity(intent)
            }
            "RegistroUsuario"-> {
                val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
                startActivity(intent)
            }
            else -> {
                val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
