package com.example.myapplication.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia
import com.example.myapplication.model.LicenciaRequest
import com.example.myapplication.model.LicenciaResponse
import com.example.myapplication.service.RetrofitClient.apiService
import com.example.myapplication.utils.imageToggleAtras
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class CargarLicenciaActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var calendarViewContainer: LinearLayout
    private lateinit var cargarButton: Button
    private var fechaDesde: String? = null
    private var fechaHasta: String? = null
    private lateinit var listaEmpleados: ArrayList<Empleado>
    private lateinit var licenciasEmpleado: ArrayList<Licencia>
    private lateinit var empleadoBuscado: ArrayList<Empleado>
    private var seleccionFecha = true
    private var primeraFechaSeleccionada: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cargar_nueva_licencia)

        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text =  InicioRrHhActivity.GlobalData.empleado!!.fullName
        listaEmpleados = intent.getParcelableArrayListExtra<Empleado>("listaEmpleados") ?: arrayListOf()
        licenciasEmpleado = intent.getParcelableArrayListExtra<Licencia>("licenciasEmpleado") ?: arrayListOf()
        empleadoBuscado = intent.getParcelableArrayListExtra<Empleado>("empleadoBuscado") ?: arrayListOf()

        val imageView = findViewById<ImageView>(R.id.imagen_volver)
        imageToggleAtras(imageView,applicationContext,"irLicenciasEmpleadoActivity",listaEmpleados,licenciasEmpleado,empleadoBuscado)
        // Asignar los elementos de la interfaz de usuario a las variables correspondientes
        calendarViewContainer = findViewById(R.id.calendar_view_container)
        cargarButton = findViewById(R.id.boton_create)
        cargarButton.isEnabled = false

        inicializarCalendarView()

        cargarButton.setOnClickListener {
            if (fechaDesde != null && fechaHasta != null) {
                val validationResult = esFechaValida(fechaDesde!!, fechaHasta!!)
                if (validationResult.resultado) {
                    guardarLicencia()
                } else {
                    Toast.makeText(this, validationResult.mensaje, Toast.LENGTH_SHORT).show()
                    resetearFechas()
                }
            } else {
                Toast.makeText(this, "Por favor seleccione ambas fechas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun inicializarCalendarView() {
        calendarView = CalendarView(this)
        val calendar = Calendar.getInstance()
        val minDate = calendar.timeInMillis
        calendarView.minDate = minDate

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            if (seleccionFecha) {
                if (selectedCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    Toast.makeText(this, "La Licencia tiene que comenzar un Lunes", Toast.LENGTH_SHORT).show()
                } else {
                    fechaDesde = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                    mostrarDialogoConfirmacionFechaInicio(selectedCalendar)
                }
            } else {
                val diffInDays = ((selectedCalendar.timeInMillis - primeraFechaSeleccionada!!.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                if ((diffInDays + 1) % 7 != 0 || diffInDays + 1 > 35) {
                    Toast.makeText(this, "La Licencia tiene que ser múltiplo de 7 días y no mayor a 35 días", Toast.LENGTH_SHORT).show()
                } else {
                    fechaHasta = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                    mostrarDialogoConfirmacionFechaHasta(selectedCalendar)
                }
            }
        }

        calendarViewContainer.addView(calendarView)
    }

    private fun mostrarDialogoConfirmacionFechaInicio(selectedCalendar: Calendar) {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
        AlertDialog.Builder(this)
            .setTitle("Confirmar fecha de inicio")
            .setMessage("¿Desea seleccionar el día $fecha como inicio de licencia?")
            .setPositiveButton("Sí") { _, _ ->
                primeraFechaSeleccionada = selectedCalendar
                seleccionFecha = false
                actualizarMinDate(primeraFechaSeleccionada!!.timeInMillis + (1000 * 60 * 60 * 24))
                Toast.makeText(this, "Seleccione la fecha de finalización", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                resetearFechas()
            }
            .show()
    }

    private fun mostrarDialogoConfirmacionFechaHasta(selectedCalendar: Calendar) {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
        AlertDialog.Builder(this)
            .setTitle("Confirmar fecha de finalización")
            .setMessage("¿Está seguro que desea seleccionar el día $fecha como finalización de licencia?")
            .setPositiveButton("Sí") { _, _ ->
                mostrarDialogoFechasSeleccionadas()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                fechaHasta = null
                seleccionFecha = false
                actualizarMinDate(primeraFechaSeleccionada!!.timeInMillis + (1000 * 60 * 60 * 24))
            }
            .show()
    }

    private fun mostrarDialogoFechasSeleccionadas() {
        val mensaje = "Fechas seleccionadas:\nDesde: $fechaDesde\nHasta: $fechaHasta\n Si son correctas, seleccione OK y luego CREAR"
        AlertDialog.Builder(this)
            .setTitle("Confirmar Fechas")
            .setMessage(mensaje)
            .setPositiveButton("OK") { _, _ ->
                cargarButton.isEnabled = true
            }
            .setNegativeButton("Reiniciar Fechas") { _, _ ->
                resetearFechas()
            }
            .show()
    }

    private fun resetearFechas() {
        fechaDesde = null
        fechaHasta = null
        primeraFechaSeleccionada = null
        seleccionFecha = true
        actualizarMinDate(Calendar.getInstance().timeInMillis)
        cargarButton.isEnabled = false
    }

    private fun actualizarMinDate(minDate: Long) {
        runOnUiThread {
            calendarViewContainer.removeAllViews()
            calendarView = CalendarView(this@CargarLicenciaActivity)
            calendarView.minDate = minDate
            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                if (seleccionFecha) {
                    if (selectedCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        Toast.makeText(this@CargarLicenciaActivity, "La Licencia tiene que comenzar un Lunes", Toast.LENGTH_SHORT).show()
                    } else {
                        fechaDesde = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                        mostrarDialogoConfirmacionFechaInicio(selectedCalendar)
                    }
                } else {
                    val diffInDays = ((selectedCalendar.timeInMillis - primeraFechaSeleccionada!!.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    if ((diffInDays + 1) % 7 != 0 || diffInDays + 1 > 35) {
                        Toast.makeText(this@CargarLicenciaActivity, "La Licencia tiene que ser múltiplo de 7 días y no mayor a 35 días", Toast.LENGTH_SHORT).show()
                    } else {
                        fechaHasta = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                        mostrarDialogoConfirmacionFechaHasta(selectedCalendar)
                    }
                }
            }
            calendarViewContainer.addView(calendarView)
        }
    }

    private fun esFechaValida(desde: String, hasta: String): ValidationResult {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateDesde = sdf.parse(desde)
        val dateHasta = sdf.parse(hasta)

        if (dateDesde >= dateHasta) {
            return ValidationResult(false, "La fecha Desde no puede ser posterior a la fecha Hasta")
        }

        val cal = Calendar.getInstance()
        cal.time = dateDesde
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            return ValidationResult(false, "La Licencia tiene que comenzar un Lunes")
        }

        val diffInMillis = abs(dateHasta.time - dateDesde.time)
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        if ((diffInDays + 1) > 35) {
            return ValidationResult(false, "La Licencia no puede ser mayor a 35 días")
        }
        if ((diffInDays + 1) % 7 != 0L) {
            return ValidationResult(false, "La Licencia tiene que ser múltiplo de 7 días")
        }
        return ValidationResult(true, "Licencia válida, y guardando.")
    }

    private fun guardarLicencia() {
        val licenciaRequest = LicenciaRequest(
            userId = empleadoBuscado[0].userId,
            fechaDesde = fechaDesde!!,
            fechaHasta = fechaHasta!!
        )

        apiService.createLicencia(licenciaRequest).enqueue(object : Callback<LicenciaResponse> {
            override fun onResponse(call: Call<LicenciaResponse>, response: Response<LicenciaResponse>) {
                if (response.isSuccessful) {
                    val licenciaId = response.body()?.licenciaId
                    val fechaDesde = response.body()?.fechaDesde
                    val fechaHasta = response.body()?.fechaHasta
                    val userId = response.body()?.userId

                    runOnUiThread {
                        Toast.makeText(this@CargarLicenciaActivity, "Licencia guardada exitosamente: $licenciaId", Toast.LENGTH_SHORT).show()
                        val licencia = Licencia(licenciaId!!, fechaDesde!!, fechaHasta!!, userId!!)
                        licenciasEmpleado.add(licencia)
                        EmpleadosLicenciasActivity.GlobalData.licencias.add(licencia)

                        goToLicenciasEmpleados()
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

    private fun goToLicenciasEmpleados() {
        val intent = Intent(this, LicenciasEmpleadoActivity::class.java).apply {
            putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licenciasEmpleado))
            putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
            putParcelableArrayListExtra("empleadoBuscado", ArrayList(empleadoBuscado))
        }
        startActivity(intent)
    }
}

data class ValidationResult(val resultado: Boolean, val mensaje: String)
