package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CargarLicenciaActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var calendarViewHasta: CalendarView
    private lateinit var cargarButton: Button
    private var fechaDesde: String? = null
    private var fechaHasta: String? = null
    private var userId: String? = null
    private var seleccionFecha =1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar_nueva_licencia)

        userId = intent.getStringExtra("user_id")
        // Asignar los elementos de la interfaz de usuario a las variables correspondientes
        calendarView = findViewById(R.id.licence_calendarView)
        //calendarViewHasta = findViewById(R.id.licence_calendarView_hasta)
        cargarButton = findViewById(R.id.boton_create)

        // Establecer un listener para detectar cuando se cambia la fecha seleccionada en el calendario "Desde"
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            if(seleccionFecha==1){
                fechaDesde = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                seleccionFecha +=1
            }else{
                fechaHasta = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                seleccionFecha =1
            }

        }


        // Establecer un listener para el botón de carga
        cargarButton.setOnClickListener {
            // Verificar si se han seleccionado ambas fechas
            if (fechaDesde != null && fechaHasta != null) {
                // Verificar que fechaDesde no sea posterior a fechaHasta
                if (esFechaValida(fechaDesde!!, fechaHasta!!)) {
                    guardarLicencia()
                } else {
                    Toast.makeText(this, "La fecha Desde no puede ser posterior a la fecha Hasta", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para verificar que fechaDesde no sea posterior a fechaHasta
    private fun esFechaValida(desde: String, hasta: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaDesdeDate = sdf.parse(desde)
        val fechaHastaDate = sdf.parse(hasta)
        return fechaDesdeDate!! <= fechaHastaDate
    }

    // Función para guardar la licencia
    private fun guardarLicencia() {
        // Construir la URL para la solicitud HTTP
        val url = BuildConfig.BASE_URL + "/api/licencias"
        val client = OkHttpClient()

        val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId!!)
            .addFormDataPart("fechaDesde", fechaDesde!!)
            .addFormDataPart("fechaHasta", fechaHasta!!)

        // Construir la solicitud HTTP POST
        val requestBody = multipartBodyBuilder.build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "multipart/form-data")
            .build()

        // Realizar la solicitud HTTP asíncronamente
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el caso en que la solicitud falle
                runOnUiThread {
                    Toast.makeText(this@CargarLicenciaActivity, "Error al guardar la licencia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta recibida del servidor
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CargarLicenciaActivity, "Licencia guardada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CargarLicenciaActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    fun goToLicenciasDocentes() {
        val intent = Intent(applicationContext, LicenciasDocenteActivity::class.java)
        startActivity(intent)
    }
}
