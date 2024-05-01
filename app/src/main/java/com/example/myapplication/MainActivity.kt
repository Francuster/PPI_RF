package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.database.Connection
import com.example.myapplication.database.verificarBaseDatos
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val databaseConnection = Connection(applicationContext)
        val db = databaseConnection.readableDatabase
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(applicationContext)
                    verificarBaseDatos(applicationContext)
                }
            }
        }
    }
    fun Siguiente(view : View){
        val intent = Intent(applicationContext, AnteEscanea::class.java)
        startActivity(intent)
    }
}

