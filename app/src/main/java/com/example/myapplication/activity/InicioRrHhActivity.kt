package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R


class InicioRrHhActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inico_rrhh)



    }

    fun goToRegistroRrHhPrimeraSala(view: View) {

        val intent = Intent(applicationContext, RegistroUsuarioActivity::class.java)
        startActivity(intent)

    }



}




