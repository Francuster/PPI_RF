package com.example.myapplication.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ReportesSeguridadActivity: AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var downloadButton: Button
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_seguridad)

        // Asignar los elementos de la interfaz de usuario a las variables correspondientes
        calendarView = findViewById(R.id.calendarView)
        downloadButton = findViewById(R.id.boton_siguiente)

        // Restringir la selección de fechas hasta el día actual
        calendarView.maxDate = Calendar.getInstance().timeInMillis

        // Establecer un listener para detectar cuando se cambia la fecha seleccionada en el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // El mes es 0-indexed, así que se debe sumar 1
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        // Establecer un listener para el botón de descarga
        downloadButton.setOnClickListener {
            // Verificar si se ha seleccionado una fecha
            selectedDate?.let { date ->
                // Si hay una fecha seleccionada, descargar los logs correspondientes
                downloadLogs(date)
            } ?: run {
                // Si no hay fecha seleccionada, mostrar un mensaje de error
                Toast.makeText(this, "Por favor seleccione una fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para descargar los logs en base a la fecha seleccionada
    private fun downloadLogs(date: String) {
        // Construir la URL para la solicitud HTTP
        val url = "http://192.168.1.34:5000/api/day/logs?fecha=$date"
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
                when {
                    // Si la respuesta es exitosa (código 2xx)
                    response.isSuccessful -> {
                        // Obtener el cuerpo de la respuesta como un flujo de bytes
                        val responseBody = response.body?.byteStream()
                        responseBody?.let { inputStream ->
                            // Si el cuerpo de la respuesta no es nulo, generar el PDF de los logs
                            saveLogsToPdf(inputStream, date)
                        }
                    }
                    // Si la solicitud es incorrecta (código 400)
                    response.code == 400 -> {
                        runOnUiThread {
                            Toast.makeText(this@ReportesSeguridadActivity, "Solicitud incorrecta (400): Verifique los parámetros enviados.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Si no se encontraron logs para la fecha seleccionada (código 404)
                    response.code == 404 -> {
                        runOnUiThread {
                            Toast.makeText(this@ReportesSeguridadActivity, "No se encontraron logs para la fecha seleccionada (404).", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Si ocurre un error interno del servidor (código 500)
                    response.code == 500 -> {
                        runOnUiThread {
                            Toast.makeText(this@ReportesSeguridadActivity, "Error interno del servidor (500). Inténtelo más tarde.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // En cualquier otro caso, mostrar un mensaje de error genérico
                    else -> {
                        runOnUiThread {
                            Toast.makeText(this@ReportesSeguridadActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    // Función para guardar los logs descargados en un archivo PDF
    private fun saveLogsToPdf(inputStream: java.io.InputStream, date: String) {
        // Crear un nombre de archivo basado en la fecha seleccionada
        val fileName = "logs_$date.pdf"
        // Obtener el directorio de almacenamiento externo donde se guardará el PDF
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(storageDir, fileName)

        try {
            // Inicializar un nuevo documento PDF
            val document = PrintedPdfDocument(this, PrintAttributes.Builder().build())
            // Comenzar una nueva página en el documento
            val pageInfo = document.startPage(1)
            val canvas = pageInfo.canvas
            val paint = Paint()

            // Configuración de la página
            canvas.drawColor(Color.WHITE)

            // Configuración del texto
            paint.color = Color.BLACK
            paint.textSize = 12f

            var yPos = 0f
            val xPos = 0f
            val lineHeight = 12f

            val buffer = ByteArray(4096)
            var bytesRead: Int
            // Leer el contenido de los logs y escribirlo en el canvas del PDF
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                canvas.drawText(String(buffer, 0, bytesRead), xPos, yPos, paint)
                yPos += lineHeight
            }

            // Finalizar la página actual del documento
            document.finishPage(pageInfo)

            // Guardar el documento PDF en el archivo
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            // Cerrar el documento
            document.close()

            // Mostrar un mensaje de éxito
            runOnUiThread {
                Toast.makeText(this, "Logs guardados en $file", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            // Manejar cualquier error que ocurra durante el proceso de guardado del PDF
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
