package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning

class ModificacionTextoActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_texto)
}
    fun goToAtras(view : View){

        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        startActivity(intent)

    }
}