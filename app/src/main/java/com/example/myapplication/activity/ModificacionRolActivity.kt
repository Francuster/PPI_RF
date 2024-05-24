package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning

class ModificacionRolActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_rol)
        val spinner: Spinner = findViewById<Spinner>(R.id.tipo_cuenta_modificacion)
        var elementos=ArrayList<String>()

        elementos.add("ESTUDIANTE")
        elementos.add("PROFESOR")
        elementos.add("SEGURIDAD")
        elementos.add("RRHH")
        val adaptador= ArrayAdapter(this, R.layout.desplegable_tipo_cuenta,elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter=adaptador
}
}