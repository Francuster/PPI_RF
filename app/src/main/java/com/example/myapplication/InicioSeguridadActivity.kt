package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class InicioSeguridadActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.inicio_seguridad)

        }

    fun goToAnteEscanea(view: View) {

            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)

    }



}




