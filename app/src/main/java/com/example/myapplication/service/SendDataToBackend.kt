package com.example.myapplication.service

import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.myapplication.BuildConfig
import com.example.myapplication.database.Connection
import com.example.myapplication.model.CorteInternet
import com.example.myapplication.model.Log
import com.example.myapplication.model.Registro
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.wait
import org.json.JSONArray
import org.json.JSONObject
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
                    // La solicitud fue exitosa
                    if (response.isSuccessful) {

                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "HTTP request unsuccessful with status code ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Error en la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    response.body?.close()
                    activeCall = null
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "No se ha podido hacer la sincronizacion", Toast.LENGTH_SHORT).show()
                }
                activeCall = null
            }
        })
    }


    fun getLocalRegs(): List<Registro> {
        val connection = Connection(context)
        val db = connection.readableDatabase
        val puntero = db.rawQuery("SELECT * FROM LOGS WHERE sincronizado = 0", null)
        val registros = mutableListOf<Registro>()
        if (puntero.moveToFirst()) {

            try {
                do {
                    val reg = Registro(
                        puntero.getString(puntero.getColumnIndexOrThrow("horario")),
                        puntero.getString(puntero.getColumnIndexOrThrow("nombre")),
                        puntero.getString(puntero.getColumnIndexOrThrow("apellido")),
                        puntero.getString(puntero.getColumnIndexOrThrow("dni")),
                        puntero.getString(puntero.getColumnIndexOrThrow("estado")),
                        puntero.getString(puntero.getColumnIndexOrThrow("tipo")),
                        false
                    )
                    registros.add(reg)
                    marcarRegistroComoSincronizado(reg.dni)
                } while (puntero.moveToNext())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            puntero.close()
            db.close()
        } else {
            puntero.close()
            db.close()
        }
        return registros
    }

    fun sendDisconnectInfo(corte : CorteInternet,registros: List<Registro>): Boolean{

        var sended:Boolean=true

        val url = BuildConfig.BASE_URL + "/api/reportes/infoSync"

        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val jsonObject = JSONObject()
        jsonObject.put("horarioDesconexion", corte.horarioDesconexion)
        jsonObject.put("horarioReconexion", corte.horarioReconexion)
        jsonObject.put("cantRegSincronizados", corte.cantRegistros)
        jsonObject.put("periodoDeCorte", corte.periodoDeCorte)

        val registrosArray = JSONArray()
        for (registro in registros) {
            val registroObject = JSONObject()
            registroObject.put("horario", registro.horario)
            registroObject.put("nombre", registro.nombre)
            registroObject.put("apellido", registro.apellido)
            registroObject.put("dni", registro.dni)
            registroObject.put("estado", registro.estado)
            registroObject.put("tipo", registro.tipo)
            registrosArray.put(registroObject)
        }

        jsonObject.put("registros", registrosArray)

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonObject.toString())

        // Crea la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()


        activeCall = client.newCall(request)

        activeCall?.enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    // La solicitud fue exitosa //
                    if (response.isSuccessful) {
                        sended = true
                    } else {
                        sended = false
                    }

                } catch (e: Exception) {e.printStackTrace()
                    sended=false
                } finally {
                    response.body?.close()
                    activeCall = null
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Maneja el fallo de la solicitud
                activeCall = null
                sended=false

            }
        })
        return sended
    }

    fun marcarRegistroComoSincronizado(dni: String) {
        val connection = Connection(context)
        val db = connection.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("sincronizado", 1)

        val whereClause = "dni = ?"
        val whereArgs = arrayOf(dni)

        try {
            db.update("logs", contentValues, whereClause, whereArgs)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

}
