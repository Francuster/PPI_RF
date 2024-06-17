/*package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning
import com.google.android.material.textfield.TextInputEditText

class ModificacionTextoActivity : AppCompatActivity() {

    private lateinit var nombreTexto: TextInputEditText
    private var campo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_texto)

        // Referencia al campo de texto
        nombreTexto = findViewById(R.id.nombre_texto)

        // Obtener el campo de la intención
        campo = intent.getStringExtra("campo")

        // Personalizar el hint según el campo
        when (campo) {
            "nombre" -> nombreTexto.hint = "Ingrese nuevo nombre"
            "apellido" -> nombreTexto.hint = "Ingrese nuevo apellido"
            "mail" -> nombreTexto.hint = "Ingrese nuevo mail"
            "documento" -> nombreTexto.hint = "Ingrese nuevo documento"
        }
    }

    fun goToAtras(view: View) {
        // Obtener el texto del campo
        val texto = nombreTexto.text.toString()

        // Crear un Intent para regresar a la actividad anterior
        val intent = Intent()
        intent.putExtra("campo", campo)
        intent.putExtra("texto_modificado", texto)

        // Establecer el resultado y finalizar la actividad
        setResult(RESULT_OK, intent)
        finish()
    }
}
*/