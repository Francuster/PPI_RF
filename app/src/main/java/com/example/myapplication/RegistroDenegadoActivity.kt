package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class RegistroDenegadoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_denegado)
    }

    fun Siguiente(view : View){
            val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
            startActivity(intent)
    }
}