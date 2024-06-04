package com.example.myapplication.utils

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder
import android.widget.Toast
import com.example.myapplication.model.CorteInternet
import com.example.myapplication.service.SendDataToBackend
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NetworkChangeService : Service() {
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

     private var horarioDesconexion: String? = null
     private var horarioReconexion: String? = null

    override fun onCreate() {
        super.onCreate()
        networkChangeReceiver = NetworkChangeReceiver { isConnected ->
              // Aquí puedes manejar la lógica cuando cambia la conexión de red
                val currentTime = getCurrentTime()
                if (isConnected) {
                    // El dispositivo volvió a tener conexión a Internet
                    if (fallos_conexion >0){
                        horarioReconexion = currentTime
                        Toast.makeText(this, "El dispositivo volvió a tener conexión a Internet", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "Aguarde unos segundos, estamos sincronizando los datos.", Toast.LENGTH_LONG).show()

                        val regRequest = SendDataToBackend(applicationContext)

                        val cantRegistrosSincronizados=regRequest.sendLocalRegs()

                        if(cantRegistrosSincronizados>0){
                            Toast.makeText(this, "Sincronización exitosa", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this, "No existen nuevos registros para sincronizar.", Toast.LENGTH_SHORT).show()
                        }
                        //Enviar el período de corte acá
                        val corte=CorteInternet(horarioDesconexion,horarioReconexion,cantRegistrosSincronizados)
                        //regRequest.sendDisconnectReports(corte)
                    }

                } else {
                    // El dispositivo está sin conexión a Internet
                    horarioDesconexion = currentTime
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

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return dateFormat.format(Date())
    }
}
