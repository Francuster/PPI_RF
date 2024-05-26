package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning

class ModificacionUsuarioActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_usuario)
    }
    fun goToModificacionRol(view : View){

        val intent = Intent(applicationContext, ModificacionRolActivity::class.java)
        startActivity(intent)

    }
    fun goToModificacionHora(view : View){

        val intent = Intent(applicationContext, ModificacionHoraActivity::class.java)
        startActivity(intent)

    }
    fun goToModificacionTexto(view : View){

        val intent = Intent(applicationContext, ModificacionTextoActivity::class.java)
        startActivity(intent)

    }
    fun goToAtras(view : View){

        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)

    }

}