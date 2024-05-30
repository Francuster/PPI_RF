package com.example.myapplication.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.database.TAG
import com.example.myapplication.database.verificarBaseDatos
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.deviceIsConnected

class FormularioOfflineActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val onLine = deviceIsConnected(applicationContext)
                    Log.println(Log.INFO,TAG,"Esta conectado ? "+onLine)
                    RenderFormulario(context = this, offline = onLine) {
                        finish()
                    }
                    verificarBaseDatos(context)
                }
            }


        }
    }

}
