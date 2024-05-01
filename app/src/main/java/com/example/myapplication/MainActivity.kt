package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.database.Connection
import com.example.myapplication.database.verificarBaseDatos
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Siguiente()
                }
            }
        }
    }
}

@Composable
fun Siguiente() {
    val context = LocalContext.current

    if (deviceIsConnected(context)) {
        val intent = Intent(context, AnteEscanea::class.java)
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
        MainContent(context)
        verificarBaseDatos(context)
    }
}
