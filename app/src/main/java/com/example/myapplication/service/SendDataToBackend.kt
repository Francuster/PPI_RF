package com.example.myapplication.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.myapplication.BuildConfig
import com.example.myapplication.database.Connection
import com.example.myapplication.model.CorteInternet
import com.example.myapplication.model.Log
import com.example.myapplication.model.Registro
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
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


    fun getLocalRegs(): Int {
        var count: Int = 0
        val connection = Connection(context)
        val db = connection.writableDatabase
        val puntero = db.rawQuery("SELECT * FROM LOGS", null)

        if (puntero.moveToFirst()) {
            val registros = mutableListOf<Registro>()
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
                    registros.add(reg)
                } while (puntero.moveToNext())

                // Envía los registros en lote
                if (sendLocalRegs(registros)) {
                    count = registros.size
                    // Borra los registros si se enviaron correctamente
                    db.delete("LOGS", null, null)
                } else {
                    throw Exception("Error al enviar los registros")
                }

                Toast.makeText(context, "Cantidad de registros sincronizados: " + count, Toast.LENGTH_SHORT).show()
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }

            puntero.close()
            db.close()

        } else {
            puntero.close()
            db.close()

        }
        return count
    }

    fun sendLocalRegs(registros: List<Registro>): Boolean {
        var sended = true
        val url = BuildConfig.BASE_URL + "/api/logs/authenticationOffline"

        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Crear el cuerpo de la solicitud HTTP
        val registrosJson = JSONArray()
        for (reg in registros) {
            val jsonObject = JSONObject()
            jsonObject.put("horario", reg.horario)
            jsonObject.put("nombre", reg.nombre)
            jsonObject.put("apellido", reg.apellido)
            jsonObject.put("dni", reg.dni)
            jsonObject.put("estado", reg.estado)
            jsonObject.put("tipo", reg.tipo)
            registrosJson.put(jsonObject)
        }

        val requestBody = registrosJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()


        activeCall = client.newCall(request)

        activeCall?.enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "HTTP request unsuccessful with status code ${response.code}", Toast.LENGTH_SHORT).show()
                        sended = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error en la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                    sended = false
                } finally {
                    response.body?.close()
                    activeCall = null
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "No se ha podido hacer la sincronización", Toast.LENGTH_SHORT).show()
                activeCall = null
                sended = false
            }
        })

        return sended
    }


    fun sendDisconnectReports(corte : CorteInternet): Boolean{

        var sended:Boolean=true

        val url = BuildConfig.BASE_URL + "/api/reportes/cortes"

        // CREAR CONEXION
        val client = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        // Crear el cuerpo de la solicitud HTTP
        val requestBody = FormBody.Builder()
            .add("horarioDesconexion", corte.horarioDesconexion)
            .add("horarioReconexion", corte.horarioReconexion)
            .add("cantRegSincronizados", corte.cantRegistros.toString())
            .add("periodoDeCorte",corte.periodoDeCorte)
            .build()

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
                        sended=true
                    } else {
                        sended=false

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
}
