package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import com.example.myapplication.model.LicenciaRequest
import com.example.myapplication.model.LicenciaResponse
import com.example.myapplication.service.RetrofitClient.apiService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import retrofit2.Callback
import retrofit2.Response

class CargarLicenciaActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var calendarViewHasta: CalendarView
    private lateinit var cargarButton: Button
    private var fechaDesde: String? = null
    private var fechaHasta: String? = null
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private lateinit var licenciasEmpleado: ArrayList<Licencia>
    private lateinit var empleadoBuscado: ArrayList<Empleado>
    private var seleccionFecha = true
    val fechasSeleccionadas: MutableList<Calendar> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar_nueva_licencia)

        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()
        licenciasEmpleado = intent.getParcelableArrayListExtra<Licencia>("licenciasEmpleado") ?: arrayListOf()
        empleadoBuscado = intent.getParcelableArrayListExtra<Empleado>("empleadoBuscado") ?: arrayListOf()
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
        val licenciaRequest = LicenciaRequest(
            userId = empleadoBuscado[0].userId,
            fechaDesde = fechaDesde!!,
            fechaHasta = fechaHasta!!
        )

        apiService.createLicencia(licenciaRequest).enqueue(object : Callback<LicenciaResponse> {
            override fun onResponse(call: retrofit2.Call<LicenciaResponse>, response: Response<LicenciaResponse>) {
                if (response.isSuccessful) {
                    val licenciaId = response.body()?.licenciaId
                    runOnUiThread {
                        Toast.makeText(this@CargarLicenciaActivity, "Licencia guardada exitosamente: $licenciaId", Toast.LENGTH_SHORT).show()
                        val licencia = Licencia(licenciaId!!, fechaDesde!!, fechaHasta!!, empleadoBuscado[0].userId!!)
                        licenciasEmpleado.add(licencia)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CargarLicenciaActivity, "Error al guardar la licencia", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LicenciaResponse>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@CargarLicenciaActivity, "Error al guardar la licencia: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    fun goToLicenciasEmpleados(view: View) {
        val intent = Intent(this, LicenciasEmpleadoActivity::class.java)
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        intent.putParcelableArrayListExtra("empleadoBuscado", ArrayList(empleadoBuscado))
        startActivity(intent)
    }
}

data class ValidationResult(val resultado: Boolean, val mensaje: String){

}