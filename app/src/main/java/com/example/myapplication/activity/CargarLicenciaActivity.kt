package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class CargarLicenciaActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var calendarViewHasta: CalendarView
    private lateinit var cargarButton: Button
    private var fechaDesde: String? = null
    private var fechaHasta: String? = null
    private lateinit var licenciasEmpleado: ArrayList<Licencia>
    private var empleado: Empleado? = null
    private var seleccionFecha = true
    val fechasSeleccionadas: MutableList<Calendar> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar_nueva_licencia)

        licenciasEmpleado = intent.getParcelableArrayListExtra<Licencia>("licenciasDocente") ?: arrayListOf()
        empleado = intent.getParcelableExtra("empleado")
        // Asignar los elementos de la interfaz de usuario a las variables correspondientes
        calendarView = findViewById(R.id.licence_calendarView)
        cargarButton = findViewById(R.id.boton_create)

        val calendar = Calendar.getInstance()
        val minDate = calendar.timeInMillis // Obtener la fecha actual en milisegundos
        calendarView.minDate = minDate
        // Establecer un listener para detectar cuando se cambia la fecha seleccionada en el calendario "Desde"
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            if(seleccionFecha){
                fechaDesde = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                seleccionFecha = false
            }else{
                fechaHasta = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                seleccionFecha = true
            }

        }

        // Establecer un listener para el botón de carga
        cargarButton.setOnClickListener {
            // Verificar si se han seleccionado ambas fechas
            if (fechaDesde != null && fechaHasta != null) {
                // Verificar que fechaDesde no sea posterior a fechaHasta
                var validationResult = esFechaValida(fechaDesde!!, fechaHasta!!)
                if (validationResult.resultado) {
                    val licencia = Licencia("",fechaDesde!!, fechaHasta!!, empleado?.userId!!)
                    licenciasEmpleado.add(licencia)
                    guardarLicencia()
                } else {
                    Toast.makeText(this, validationResult.mensaje, Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show()
                    seleccionFecha = true
                }
            } else {
                Toast.makeText(this, "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show()
                seleccionFecha = true
            }
        }
    }

    // Función para verificar que fechaDesde no sea posterior a fechaHasta
    private fun esFechaValida(desde: String, hasta: String): ValidationResult {

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateDesde = sdf.parse(desde)
        val dateHasta = sdf.parse(hasta)

        if (dateDesde >= dateHasta){
            return ValidationResult(false, "La fecha Desde no puede ser posterior a la fecha Hasta")
        }
        // Verificar que fechaDesde comience en lunes
        val cal = Calendar.getInstance()
        cal.time = dateDesde
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            return ValidationResult(false, "La Licencia tiene que comenzar un Lunes")
        }

        // Calcular la diferencia de días entre fechaDesde y fechaHasta
        val diffInMillis = abs(dateHasta.time - dateDesde.time)
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        // Verificar que la diferencia de días sea menor o igual a 35 y múltiplo de 7
        if ((diffInDays+1) > 35 ){
            return ValidationResult(false, "La Licencia no puede ser mayor a 35 dias")
        }
        if ((diffInDays+1) % 7 != 0L){
            return ValidationResult(false, "La Licencia tiene que ser multiplo de 7")
        }
        return ValidationResult(true,"Licencia valida, y guardando.")
    }



    // Función para guardar la licencia
    private fun guardarLicencia() {
        // Construir la URL para la solicitud HTTP
        val url = BuildConfig.BASE_URL + "/api/licencias"
        val client = OkHttpClient()

        val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", empleado?.userId!!)
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

    fun goToLicenciasEmpleados() {
        val intent = Intent(applicationContext, LicenciasEmpleadoActivity::class.java)
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
        startActivity(intent)
    }
}

data class ValidationResult(val resultado: Boolean, val mensaje: String){

}