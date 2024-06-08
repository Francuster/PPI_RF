package com.example.myapplication.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class ReportesSeguridadActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var downloadButton: Button
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_seguridad)

        // Verificar y solicitar permisos si es necesario
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        // Asignar los elementos de la interfaz de usuario a las variables correspondientes
        calendarView = findViewById(R.id.calendarView)
        downloadButton = findViewById(R.id.boton_siguiente)

        // Restringir la selección de fechas hasta el día actual
        calendarView.maxDate = Calendar.getInstance().timeInMillis

        // Establecer un listener para detectar cuando se cambia la fecha seleccionada en el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        // Establecer un listener para el botón de descarga
        downloadButton.setOnClickListener {
            selectedDate?.let { date ->
                downloadLogs(date)
            } ?: run {
                Toast.makeText(this, "Por favor seleccione una fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Función para descargar los logs en base a la fecha seleccionada
    private fun downloadLogs(date: String) {
        // Construir la URL para la solicitud HTTP
        val url = BuildConfig.BASE_URL + "/api/logs/day?fecha=$date"
        val client = OkHttpClient()

        // Construir la solicitud HTTP GET
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Realizar la solicitud HTTP asíncronamente
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el caso en que la solicitud falle
                runOnUiThread {
                    Toast.makeText(this@ReportesSeguridadActivity, "Error al descargar los logs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta recibida del servidor
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        // Si el cuerpo de la respuesta no es nulo, generar el PDF de los logs
                        saveLogsToPdf(responseBody, date)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ReportesSeguridadActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Función para guardar los logs descargados en un archivo PDF
    private fun saveLogsToPdf(responseBody: String, date: String) {
        val fileName = "logs_$date.pdf"
        // Obtener el directorio de descargas público
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(storageDir, fileName)

        try {
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("default", "default", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            val document = PrintedPdfDocument(this, printAttributes)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            canvas.drawColor(Color.WHITE)
            paint.color = Color.BLACK
            paint.textSize = 12f

            var yPos = 20f
            val xPos = 20f
            val lineHeight = 20f

            val logsArray = JSONArray(responseBody)
            for (i in 0 until logsArray.length()) {
                val log = logsArray.getJSONObject(i)
                val logText = """
                ID: ${log.getString("_id")}
                Horario: ${log.getString("horario")}
                Nombre: ${log.getString("nombre")}
                Apellido: ${log.getString("apellido")}
                DNI: ${log.getInt("dni")}
                Estado: ${log.getString("estado")}
                Tipo: ${log.getString("tipo")}
            """.trimIndent()

                val lines = logText.split("\n")
                for (line in lines) {
                    canvas.drawText(line, xPos, yPos, paint)
                    yPos += lineHeight
                }
                yPos += lineHeight
            }

            document.finishPage(page)

            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            document.close()

            runOnUiThread {
                Toast.makeText(this, "Logs guardados en $file", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, "Error al guardar el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
        startActivity(intent)
    }
}