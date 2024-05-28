package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.deviceIsConnected

class AnteEscaneaActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escanear)

    }
    fun Siguiente(view : View){
        if(deviceIsConnected(applicationContext)){
//            val intent = Intent(applicationContext, CameraIngresoEgresoActivity::class.java)
            val intent = Intent(applicationContext, CameraxAuthenticationActivity::class.java)

            startActivity(intent)
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}