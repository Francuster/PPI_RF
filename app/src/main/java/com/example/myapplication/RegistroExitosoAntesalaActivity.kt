package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.service.Log
import com.example.myapplication.service.SendDataToBackend
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroExitosoAntesalaActivity: AppCompatActivity() {

    var nombre = ""
    var apellido = ""
    var dni = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_exitoso_antesala)

        val extras = intent.extras
        if (extras != null) {
            nombre = extras.getString("nombre").toString()
            apellido = extras.getString("apellido").toString()
            val rol = extras.getString("roles")
            val rolArray = extras.getStringArrayList("roles")
            dni = extras.getInt("dni").toString()
            val lugares=extras.getString("lugares")
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
            val textoLugares= findViewById<TextView>(R.id.lugares_text)
            textoLugares.text = lugares
        }
    }

    fun Siguiente(view: View) {
        // Iniciar la actividad RegistroExitosoActivity
        val intent = Intent(applicationContext, RegistroExitosoActivity::class.java)
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val horario = formato.format(Date())
        val log =Log( horario,nombre,apellido,dni,estado ="ingresando/hardodeado",tipo = "online/harcodeado")
        val logRequest = SendDataToBackend(applicationContext)
        logRequest.sendLog(log)
        startActivity(intent)
    }


    fun rechazarClick(view: View) {

        val intent = Intent(applicationContext, RegistroDenegadoActivity::class.java)
        startActivity(intent)
    }


}
