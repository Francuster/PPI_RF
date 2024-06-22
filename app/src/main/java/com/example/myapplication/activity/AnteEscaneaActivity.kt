package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import com.example.myapplication.utils.deviceIsConnected
import com.example.myapplication.utils.imageToggleAtras

class AnteEscaneaActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escanear)
        val imageView = findViewById<ImageView>(R.id.imagen_volver)
        imageToggleAtras(imageView,applicationContext,"irInicioSeguridadActivity",ArrayList<Empleado>(),ArrayList<Licencia>(),ArrayList<Empleado>())
    }
    fun Siguiente(view : View){
        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, CameraxAuthenticationActivity::class.java)
            startActivity(intent)
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
        startActivity(intent)
        finish()
    }


}