package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class RegistroExitoso2Activity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_exitoso)
        //  --- Actualizar TextViews estado
        val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_denegado)
        textoNombreUsuario.text = "REGISTRO EXITOSO"
    }

    fun Siguiente(view : View){
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

}
