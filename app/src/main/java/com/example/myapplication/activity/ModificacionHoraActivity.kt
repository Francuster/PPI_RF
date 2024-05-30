package com.example.myapplication.activity

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ModificacionHoraActivity : AppCompatActivity() {

    private lateinit var horaEntradaModificacion: TextInputEditText
    private lateinit var horaSalidaModificacion: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }
        setContentView(R.layout.modificacion_hora)

        horaEntradaModificacion = findViewById(R.id.hora_entrada_modificacion)
        horaSalidaModificacion = findViewById(R.id.hora_salida_modificacion)

        horaEntradaModificacion.setOnClickListener {
            mostrarTimePickerDialog(horaEntradaModificacion)
        }

        horaSalidaModificacion.setOnClickListener {
            mostrarTimePickerDialog(horaSalidaModificacion)
        }
    }

    private fun mostrarTimePickerDialog(inputEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                inputEditText.setText(formattedTime)
            },
            hour,
            minute,
            true
        )

        // Mostrar el TimePickerDialog
        timePickerDialog.show()
    }

    fun Listo(view: View) {
        val horaEntrada = horaEntradaModificacion.text.toString()
        val horaSalida = horaSalidaModificacion.text.toString()

        if (horaEntrada.isNotBlank() && horaSalida.isNotBlank()) {
            // Crear un Intent para regresar a la actividad anterior
            val intent = Intent()
            intent.putExtra("hora_entrada", horaEntrada)
            intent.putExtra("hora_salida", horaSalida)

            // Establecer el resultado y finalizar la actividad
            setResult(RESULT_OK, intent)
            finish()
        } else {
            Toast.makeText(this, "Por favor selecciona ambas horas", Toast.LENGTH_SHORT).show()
        }
    }

    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        startActivity(intent)
    }
}
