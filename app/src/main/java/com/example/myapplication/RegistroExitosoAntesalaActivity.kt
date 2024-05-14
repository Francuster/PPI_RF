package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegistroExitosoAntesalaActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)

        val extras = intent.extras
        if (extras != null) {
            val nombre = extras.getString("nombre")
            val apellido = extras.getString("apellido")
            val rol = extras.getString("roles")
            val dni = extras.getInt("dni")
            val rolArray = extras.getStringArrayList("roles")


            //val byteArray = extras.getByteArray("imagen")

            // IMAGEN --- Decodificar el byteArray a un bitmap
            //val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

            // Mostrar el bitmap en el ImageView "escaneo"
            //val imageViewEscaneo = findViewById<ImageView>(R.id.escaneo)
            //imageViewEscaneo.setImageBitmap(bitmap)

            // ROL TIPO CUENTA --- Verificar si rolArray no es nulo ni está vacío
            /*if (!rolArray.isNullOrEmpty()) {
                val primerRol = rolArray[0] // Obtener el primer rol
                val textoTipoCuenta = findViewById<TextView>(R.id.tipo_cuenta)
                textoTipoCuenta.text = primerRol // Establecer el primer rol en el TextView "tipo_cuenta"
            }*/
            val textoTipoCuenta = findViewById<TextView>(R.id.tipo_cuenta_texto)
            textoTipoCuenta.text = "$rol"// Harcodear el texto para probar

            // NOMBRE Y APELLIDO --- Actualizar TextViews con el nombre y apellido
            val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_exitoso)
            textoNombreUsuario.text = "$nombre $apellido \n"+"DNI:$dni"
        }
    }

    fun Siguiente(view: View) {
        // Iniciar la actividad RegistroExitosoActivity
        val intent = Intent(applicationContext, RegistroExitosoActivity::class.java)
        startActivity(intent)
    }
    fun rechazarClick(view: View) {
        //ACA LO DEJO ASI PORQ LA CLASE DENEGADO ME ESTA DANDO ERROR
        val intent = Intent(applicationContext, RegistroDenegadoActivity::class.java)
        startActivity(intent)
    }
}
