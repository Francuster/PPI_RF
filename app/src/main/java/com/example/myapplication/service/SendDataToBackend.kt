package com.example.myapplication.service

import android.content.Context
import android.widget.Toast
import com.example.myapplication.model.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class SendDataToBackend (private val context: Context) {

    fun  sendLog(log: Log) {
        // URL

        val url = "https://log3r.up.railway.app/api/authentication/logs"
        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Crear el cuerpo de la solicitud HTTP
        val requestBody = FormBody.Builder()
            .add("horario", log.horario)
            .add("nombre",log.nombre)
            .add("apellido",log.apellido)
            .add("dni",log.dni)
            .add("estado",log.estado)
            .add("tipo",log.tipo)
            .build()

        // Crea la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Ejecuta la solicitud de forma asíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                e.printStackTrace()
                Toast.makeText(context, "No se ha podido hacer el registro", Toast.LENGTH_SHORT).show()

            }

            override fun onResponse(call: Call, response: Response) {
                // Maneja la respuesta del servidor
                // Manejar la respuesta del servidor aquí
                    // La solicitud fue exitosa //
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()


            }



        })
    }


}