package com.example.myapplication.activity


import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import android.widget.Spinner
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar


class RegistroUsuarioActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isServiceRunning(applicationContext, NetworkChangeService ::class.java)){
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.registro_primera_sala_rrhh)
        val spinner:Spinner = findViewById<Spinner>(R.id.tipo_cuenta)
        var elementos=ArrayList<String>()

        elementos.add("ESTUDIANTE")
        elementos.add("PROFESOR")
        elementos.add("SEGURIDAD")
        elementos.add("RRHH")
        val adaptador=ArrayAdapter(this,R.layout.desplegable_tipo_cuenta,elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter=adaptador

        val hora_entrada=findViewById<TextInputEditText>(R.id.hora_entrada)
        hora_entrada.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_entrada.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()
        }

        val hora_salida=findViewById<TextInputEditText>(R.id.hora_salida)
        hora_salida.setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Formatear la hora seleccionada y mostrarla en el TextInputEditText
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                hora_salida.setText(formattedTime)
            }, hour, minute, true)

            // Mostrar el TimePickerDialog
            timePickerDialog.show()
        }

    }
    fun Siguiente(view : View){

        val intent = Intent(applicationContext, Denegado::class.java)
        startActivity(intent)

    }

}