package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(deviceIsConnected(applicationContext)){
            setContentView(R.layout.activity_main)
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
    fun Siguiente(view : View){
        val intent = Intent(applicationContext, AnteEscanea::class.java)
        startActivity(intent)

    }

    private fun deviceIsConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

}