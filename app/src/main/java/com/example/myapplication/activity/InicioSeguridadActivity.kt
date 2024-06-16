package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.utils.deviceIsConnected
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class InicioSeguridadActivity : AppCompatActivity() {

    object GlobalData {
        var seguridad: Empleado? = null
    }

    private lateinit var logContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_seguridad)

        if (GlobalData.seguridad == null) {
            val nombre = intent.getStringExtra("nombre")
            val apellido = intent.getStringExtra("apellido")
            GlobalData.seguridad = Empleado(fullName = "$nombre $apellido", userId = "124124")
        }

        val textoNombreUsuario = findViewById<TextView>(R.id.seguridad)
        textoNombreUsuario.text = GlobalData.seguridad!!.fullName

        logContainer = findViewById(R.id.container)

        // Mostrar los logs del día actual
        showLogsOfTheDay()
    }

    private fun showLogsOfTheDay() {
        val url = BuildConfig.BASE_URL + "/api/logs/day?fecha=${getCurrentDate()}"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@InicioSeguridadActivity, "Error al cargar los logs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        runOnUiThread {
                            displayLogs(responseBody)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@InicioSeguridadActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun displayLogs(responseBody: String) {
        logContainer.removeAllViews()
        try {
            val logsArray = JSONArray(responseBody)
            for (i in 0 until logsArray.length()) {
                val log = logsArray.getJSONObject(i)
                val logText = """
                ┌────────────────────┐
                │ Nombre: ${log.getString("nombre").padEnd(18)} 
                │ Apellido: ${log.getString("apellido").padEnd(16)} 
                │ DNI: ${log.getInt("dni").toString().padEnd(21)} 
                │ Estado: ${log.getString("estado").padEnd(16)} 
                │ Horario: ${log.getString("horario").padEnd(15)} 
                │ Tipo: ${log.getString("tipo").padEnd(18)} 
                └────────────────────┘
                """.trimIndent()

                val textView = TextView(this)
                textView.text = logText
                textView.setPadding(24, 16, 24, 16)
                textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                logContainer.addView(textView)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al parsear los logs: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun goToAnteEscanea(view: View) {
        if (deviceIsConnected(applicationContext)) {
            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, QRScannerActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToFormulario(view: View) {
        if (deviceIsConnected(applicationContext)) {
            val intent = Intent(applicationContext, AnteEscaneaActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, FormularioOfflineActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToQREspecial(view: View) {
        if (deviceIsConnected(applicationContext)) {
            Toast.makeText(this, "Estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, QRScannerActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToFormEspecial(view: View) {
        if (deviceIsConnected(applicationContext)) {
            Toast.makeText(this, "Estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, FormularioOfflineActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToReporteSeguridad(view: View) {
        val intent = Intent(applicationContext, ReportesSeguridadActivity::class.java)
        startActivity(intent)
    }
}
