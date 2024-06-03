package com.example.myapplication.service

import android.content.Context
import android.widget.Toast
import com.example.myapplication.BuildConfig
import com.example.myapplication.database.Connection
import com.example.myapplication.model.Log
import com.example.myapplication.model.Registro
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SendDataToBackend (private val context: Context) {

    private var activeCall: Call? = null

    fun sendLog(log: Log) {

        // URL

        val url = BuildConfig.BASE_URL + "/api/logs/authentication"
        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Crear el cuerpo de la solicitud HTTP
        val requestBody = FormBody.Builder()
            .add("horario", log.horario)
            .add("nombre", log.nombre)
            .add("apellido", log.apellido)
            .add("dni", log.dni)
            .add("estado", log.estado)
            .add("tipo", log.tipo)
            .build()

        // Crea la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        activeCall?.cancel()


        activeCall = client.newCall(request)


        // Ejecuta la solicitud de forma asíncrona
        activeCall?.enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    // La solicitud fue exitosa //
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(context, "HTTP request unsuccessful with status code ${response.code}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error en la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }finally {
                    response.body?.close()
                    activeCall = null
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                e.printStackTrace()
                Toast.makeText(context, "No se ha podido hacer la sincronizacion", Toast.LENGTH_SHORT)
                    .show()
                activeCall=null

            }
        })
    }

    fun sendLocalRegs(): Boolean {
            var count:Int=0
            val connection = Connection(context)
            val db = connection.writableDatabase
            val puntero = db.rawQuery("SELECT * FROM LOGS", null)

        if (puntero.moveToFirst()) {
            db.beginTransaction()
            try {
                do {
                    val reg = Registro(
                        puntero.getString(puntero.getColumnIndexOrThrow("horario")),
                        puntero.getString(puntero.getColumnIndexOrThrow("nombre")),
                        puntero.getString(puntero.getColumnIndexOrThrow("apellido")),
                        puntero.getString(puntero.getColumnIndexOrThrow("dni")),
                        puntero.getString(puntero.getColumnIndexOrThrow("estado")),
                        puntero.getString(puntero.getColumnIndexOrThrow("tipo")),
                    )
                    // Envía el registro
                    if (sendRegistro(reg)) {
                        count+=
                        // Borra el registro si se envió correctamente
                        db.delete(
                            "LOGS",
                            "horario = ? AND nombre = ? AND apellido = ? AND dni = ? AND estado = ? AND tipo = ?",
                            arrayOf(reg.horario, reg.nombre, reg.apellido, reg.dni, reg.estado, reg.tipo)
                        )
                    } else {
                        // Si falla el envío, se puede decidir si continuar o revertir la transacción
                        throw Exception("Error al eliminar el registro")
                    }
                } while (puntero.moveToNext())

                Toast.makeText(context, "Cantidad de registros sincronizados: " + count, Toast.LENGTH_SHORT)
                    .show()

                db.setTransactionSuccessful()
            } catch (e: Exception) {
                // Manejo del error, si se desea loggear o realizar alguna acción
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }

            puntero.close()
            db.close()
            return true
        } else {
            puntero.close()
            db.close()
            return false
        }
    }

    fun sendRegistro(reg: Registro): Boolean {
        // URL
        var sended:Boolean=true

        val url = BuildConfig.BASE_URL + "/api/logs/authentication"
        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val horario = formato.format(Date())

        // Crear el cuerpo de la solicitud HTTP
        val requestBody = FormBody.Builder()
            .add("horario", horario.toString())
            .add("nombre", reg.nombre)
            .add("apellido", reg.apellido)
            .add("dni", reg.dni)
            .add("estado", reg.estado)
            .add("tipo", reg.tipo)
            .build()

        // Crea la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        activeCall?.cancel()

        activeCall = client.newCall(request)

        activeCall?.enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    // La solicitud fue exitosa //
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "HTTP request unsuccessful with status code ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                        sended=false

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Error en la respuesta: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    sended=false
                } finally {
                    response.body?.close()
                    activeCall = null
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                e.printStackTrace()
                Toast.makeText(context,"No se ha podido hacer la sincronizacion",Toast.LENGTH_SHORT).show()
                activeCall = null
                sended=false

            }
        })
        return sended
    }
}
