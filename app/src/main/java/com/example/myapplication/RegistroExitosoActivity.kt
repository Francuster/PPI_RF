package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class RegistroExitosoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_exitoso)
    }

    fun Siguiente(view : View){
        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
            startActivity(intent)
        }

    }
}