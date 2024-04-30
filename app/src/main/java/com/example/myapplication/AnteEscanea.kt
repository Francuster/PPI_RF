package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class AnteEscanea: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escanear)

    }
    fun Siguiente(view : View){
        val intent = Intent(applicationContext, Escanea::class.java)
        startActivity(intent)

    }
}