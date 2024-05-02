package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkConnection()

    }
    fun Siguiente(view : View){
        val intent = Intent(applicationContext, AnteEscanea::class.java)
        startActivity(intent)

    }

    fun checkConnection() {
        if (!deviceIsConnected(applicationContext)) {
            Toast.makeText(applicationContext, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, FormularioOfflineActivity::class.java)
            startActivity(intent)
        }
    }


}






