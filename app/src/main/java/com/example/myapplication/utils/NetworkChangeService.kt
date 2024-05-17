package com.example.myapplication.utils

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder
import android.widget.Toast


class NetworkChangeService : Service() {
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate() {
        super.onCreate()
        networkChangeReceiver = NetworkChangeReceiver { isConnected ->
              // Aquí puedes manejar la lógica cuando cambia la conexión de red
                if (isConnected) {
                    // El dispositivo volvió a tener conexión a Internet
                    if (fallos_conexion >0){
                        Toast.makeText(this, "El dispositivo volvió a tener conexión a Internet", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "Aguarde unos segundos, estamos Sincronizando los datos.", Toast.LENGTH_LONG).show()
                    }

                } else {
                    // El dispositivo está sin conexión a Internet
                    if (fallos_conexion >0){
                        Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
                    }
                    fallos_conexion += 1
                }
            }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
