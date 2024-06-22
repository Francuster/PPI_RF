package com.example.myapplication.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.changeColorTemporarily
import com.example.myapplication.utils.deviceIsConnected
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioSeguridadActivity : AppCompatActivity() {
    private lateinit var miVista : View
    private lateinit var loadingOverlayout: View
    object GlobalData {
        var seguridad: Empleado? = null
    }

    private lateinit var logContainer: LinearLayout
    private lateinit var totalLogsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_seguridad)
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        miVista = findViewById(R.id.logs_del_dia)
        if (GlobalData.seguridad == null) {
            val nombre = intent.getStringExtra("nombre")
            val apellido = intent.getStringExtra("apellido")
            val empleadoId = intent.getStringExtra("_id")

            GlobalData.seguridad = Empleado(fullName = "$nombre $apellido", userId = "$empleadoId")
        }

        val textoNombreUsuario = findViewById<TextView>(R.id.seguridad)
        textoNombreUsuario.text = GlobalData.seguridad!!.fullName

        logContainer = findViewById(R.id.container)
        totalLogsTextView = findViewById(R.id.total_logs_textview)

        // Mostrar los logs del día actual
        showLogsOfTheDay()
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

    private fun showLogsOfTheDay() {
        miVista.alpha = 0.10f // 10% de opacidad
        showLoadingOverlay()
        val url = "${BuildConfig.BASE_URL}/api/logs/day?fecha=${getCurrentDate()}"
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
        hideLoadingOverlay()
        runOnUiThread {
            aumentarOpacidad()
        }
        logContainer.removeAllViews()

        try {
            val logsArray = JSONArray(responseBody)
            val totalLogs = logsArray.length()
            totalLogsTextView.text = "Total logs del día: $totalLogs"

            for (i in 0 until logsArray.length()) {
                val logNumber = i + 1
                val log = logsArray.getJSONObject(i)
                val logText = """
                $logNumber. ┌────────────────────┐
                     Nombre: ${log.getString("nombre").padEnd(18)} 
                     Apellido: ${log.getString("apellido").padEnd(16)} 
                     DNI: ${log.getInt("dni").toString().padEnd(21)} 
                     Estado: ${log.getString("estado").padEnd(16)} 
                     Horario: ${log.getString("horario").padEnd(15)} 
                     Tipo: ${log.getString("tipo").padEnd(18)} 
                    └────────────────────┘
                """.trimIndent()

                val textView = TextView(this)
                textView.text = logText
                textView.setPadding(24, 16, 24, 16)
                textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                logContainer.addView(textView, 0)  // Añadir cada log al inicio de la lista
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
        val imageView = findViewById<ImageView>(R.id.imagen_scan)
        imageView.changeColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms
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
        val imageView = findViewById<ImageView>(R.id.imagen_nav_ingresoegreso)
        imageView.changeColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms
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
        val imageView = findViewById<ImageView>(R.id.imagen_nav_ingresoegresoespecial)
        imageView.changeColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms
        if (deviceIsConnected(applicationContext)) {
            Toast.makeText(this, "Estás conectado a Internet", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, AnteEscaneaDniActivity::class.java)
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

    fun perfilSeguridadDetailAlert(view: View) {
        val imageView = findViewById<ImageView>(R.id.imagen_nav_cuenta)
        imageView.changeColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms
        obtenerYMostrarDetallesPerfil()
    }

    private fun obtenerYMostrarDetallesPerfil() {
        miVista.alpha = 0.10f // 10% de opacidad
        showLoadingOverlay()
        val empleado = GlobalData.seguridad ?: return // Verificar que el empleado de seguridad no sea nulo
        val empleadoId = empleado.userId // Obtener el ID del empleado de seguridad

        // Llamar al método del servicio para obtener los detalles del empleado por su ID
        RetrofitClient.userApiService.getById(empleadoId).enqueue(object : retrofit2.Callback<UserModel> {
            override fun onResponse(call: retrofit2.Call<UserModel>, response: retrofit2.Response<UserModel>) {
                if (response.isSuccessful) {
                    val userModel = response.body()

                    if (userModel != null) {
                        obtenerYMostrarHorarios(userModel)
                    } else {
                        mostrarDialogoError()
                    }
                } else {
                    mostrarDialogoError()
                }
            }

            override fun onFailure(call: retrofit2.Call<UserModel>, t: Throwable) {
                mostrarDialogoError()
            }
        })
    }

    private fun obtenerYMostrarHorarios(userModel: UserModel) {
        val detallesBuilder = StringBuilder()
        detallesBuilder.append("Nombre: ${userModel.nombre}\n")
        detallesBuilder.append("Apellido: ${userModel.apellido}\n")
        detallesBuilder.append("DNI: ${userModel.dni}\n")
        detallesBuilder.append("Email: ${userModel.email}\n")

        val horarios = mutableListOf<String>()
        for (horarioId in userModel.horarios) {
            RetrofitClient.horariosApiService.getById(horarioId).enqueue(object : retrofit2.Callback<HorarioModel> {
                override fun onResponse(call: retrofit2.Call<HorarioModel>, response: retrofit2.Response<HorarioModel>) {
                    if (response.isSuccessful) {
                        val horario = response.body()
                        if (horario != null) {
                            horarios.add(horario.getFullName())
                            if (horarios.size == userModel.horarios.size) {
                                hideLoadingOverlay()
                                detallesBuilder.append("Horarios: ${horarios.joinToString(", ")}\n")
                                mostrarDialogoPerfil(detallesBuilder.toString())
                            }
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<HorarioModel>, t: Throwable) {
                    hideLoadingOverlay()
                    mostrarDialogoError()
                }
            })
        }
    }

    private fun mostrarDialogoPerfil(detalles: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Detalles del Perfil")
            setMessage(detalles)

            setPositiveButton("OK") { dialog, which ->
                // Aquí puedes añadir alguna acción si lo deseas
                aumentarOpacidad()
            }

            setNegativeButton("Cerrar sesión") { dialog, which ->
                mostrarDialogoConfirmacion()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun mostrarDialogoConfirmacion() {
        val confirmDialogBuilder = AlertDialog.Builder(this)
        confirmDialogBuilder.apply {
            setTitle("Confirmación")
            setMessage("¿Está seguro que desea cerrar sesión?")

            setPositiveButton("Sí") { dialog, which ->
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()  // Finaliza la actividad actual para evitar que el usuario regrese usando el botón de atrás
            }

            setNegativeButton("No") { dialog, which ->
                aumentarOpacidad()
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val confirmDialog = confirmDialogBuilder.create()
        confirmDialog.show()
    }

    private fun mostrarDialogoError() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Error")
            setMessage("No se pudo obtener los detalles del perfil")

            setPositiveButton("OK") { dialog, which ->
                aumentarOpacidad()
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun goToReporteSeguridad(view: View) {
        val imageView = findViewById<ImageView>(R.id.imagen_nav_logs)
        imageView.changeColorTemporarily(Color.BLACK, 150) // Cambia a NEGRO por 150 ms
        if(deviceIsConnected(applicationContext)){
            val intent = Intent(applicationContext, ReportesSeguridadActivity::class.java)
            startActivity(intent)
        }else{
            Toast.makeText(this, "No estás conectado a Internet", Toast.LENGTH_SHORT).show()
        }

    }

    fun dialogCloseSession(view: View){
        val mensaje = "¿Quiere cerrar la sesión?"
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Sesion")
            setMessage(mensaje)

            setPositiveButton("OK") { dialog, which ->
                goToLogin()
            }

            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, InicioSeguridadActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goToLogin(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}
