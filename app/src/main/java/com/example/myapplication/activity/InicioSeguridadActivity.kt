package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.service.SendDataToBackend
import com.example.myapplication.utils.deviceIsConnected


class InicioSeguridadActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.inicio_seguridad)
        }

    fun goToAnteEscanea(view: View) {
            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)
    }

    fun reconectar(view: View){
         if(deviceIsConnected(applicationContext)){
            Toast.makeText(this, "Sincronizando...", Toast.LENGTH_SHORT).show()
            val regRequest = SendDataToBackend(applicationContext)
            if(regRequest.sendLocalRegs()){
                Toast.makeText(this, "Sincronización exitosa", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "No existen nuevos registros para sincronizar.", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Para sincronizar debes estar conectado a Internet", Toast.LENGTH_SHORT).show()
        }
    }
}




