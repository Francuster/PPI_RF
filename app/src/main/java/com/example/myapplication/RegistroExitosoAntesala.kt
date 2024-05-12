package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegistroExitosoAntesala: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)

        val extras = intent.extras
        if (extras != null) {
            val nombre = extras.getString("nombre")
            val apellido = extras.getString("apellido")
            val dni = extras.getInt("dni")
            val rolArray = extras.getStringArrayList("roles")
            val byteArray = extras.getByteArray("imagen")

            // Decodificar el byteArray a un bitmap
            val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

            // Mostrar el bitmap en el ImageView "escaneo"
            val imageViewEscaneo = findViewById<ImageView>(R.id.escaneo)
            imageViewEscaneo.setImageBitmap(bitmap)
            // Verificar si rolArray no es nulo ni está vacío
            /*if (!rolArray.isNullOrEmpty()) {
                val primerRol = rolArray[0] // Obtener el primer rol
                val textoTipoCuenta = findViewById<TextView>(R.id.tipo_cuenta)
                textoTipoCuenta.text = primerRol // Establecer el primer rol en el TextView "tipo_cuenta"
            }*/

            // Actualizar TextViews con el nombre y apellido
            val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_exitoso)
            textoNombreUsuario.text = "$nombre $apellido"
        }
    }

    fun Siguiente(view: View) {
        // Iniciar la actividad RegistroExitosoActivity
        val intent = Intent(applicationContext, RegistroExitosoActivity::class.java)
        startActivity(intent)
    }
}
