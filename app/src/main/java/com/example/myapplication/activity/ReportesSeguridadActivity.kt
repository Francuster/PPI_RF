package com.example.myapplication.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
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
    private lateinit var miVista : View
    private lateinit var loadingOverlayout: View
    private lateinit var calendarView: CalendarView
    private lateinit var downloadButton: Button
    private var selectedDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reporte_seguridad)
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        miVista = findViewById(R.id.layout_hijo)
        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text = InicioSeguridadActivity.GlobalData.seguridad?.fullName ?: "Usuario"

        // Verificar y solicitar permisos si es necesario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
            }
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
    private fun aumentarOpacidad(){
        runOnUiThread {
            val animator = ObjectAnimator.ofFloat(miVista, "alpha", 0.1f, 1f)
            animator.duration = 500
            animator.start()
        }
    }

    private fun showLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.GONE
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para descargar los logs en base a la fecha seleccionada
    private fun downloadLogs(date: String) {
        miVista.alpha = 0.10f // 10% de opacidad
        showLoadingOverlay()
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
                hideLoadingOverlay()
                runOnUiThread {
                    aumentarOpacidad()
                }
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
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
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

                val maxPageHeight = 842f // Altura máxima de la página (ISO A4)
                var currentPage = 1
                val lines = logText.split("\n")
                for (line in lines) {
                    if (yPos + lineHeight > maxPageHeight) {
                        // La línea actual supera el límite de la página actual
                        document.finishPage(page)
                        currentPage++
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPage).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPos = 20f // Reiniciar yPos para la nueva página
                    }

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

            // Crear la notificación
            createNotification(file)

            runOnUiThread {
                Toast.makeText(this, "Logs guardados en $file", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, "Error al guardar el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para crear una notificación que abra el archivo PDF
    private fun createNotification(file: File) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "download_channel"
        val channelName = "Download Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.escaneo)
            .setContentTitle("Descarga completa")
            .setContentText("Logs guardados en ${file.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permiso de notificaciones si no está concedido
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
            return
        }
        NotificationManagerCompat.from(this).notify(1, notification)
    }

    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
        startActivity(intent)
    }
}
