package com.example.myapplication.activity

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ModificacionHoraActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }
        setContentView(R.layout.modificacion_hora)
        val hora_entrada_modificacion=findViewById<TextInputEditText>(R.id.hora_entrada_modificacion)
        hora_entrada_modificacion.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_entrada_modificacion.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()
        }
        val hora_salida_modificacion=findViewById<TextInputEditText>(R.id.hora_salida_modificacion)
        hora_salida_modificacion.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_salida_modificacion.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()
        }

    }
    fun goToAtras(view : View){

        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        startActivity(intent)

    }
}