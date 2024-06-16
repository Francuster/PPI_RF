package com.example.myapplication.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.activity.CameraxAddFaceActivity
import com.example.myapplication.activity.InicioRrHhActivity

class RegistroExitoso2Activity : AppCompatActivity() {

    private lateinit var userId: String // Variable para almacenar el userId
    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingreso_exitoso)

        // Actualizar TextViews estado
        val textoNombreUsuario = findViewById<TextView>(R.id.ingreso_denegado)
        textoNombreUsuario.text = "REGISTRO EXITOSO"

        // Mostrar diálogo al iniciar la actividad
        val origen = intent.getStringExtra("origen")
        if (origen == "RegistroUsuarioActivity") {
            userId = intent.getStringExtra("userId") ?: ""
            mostrarDialogoRegistroRostro()
        }
    }

    private fun mostrarDialogoRegistroRostro() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Tu registro de datos fue exitoso, ahora solo falta registrar tu rostro. ¿Deseas registrarlo ahora?")
            .setCancelable(false)
            .setPositiveButton("Registrar rostro") { dialog, id ->
                // Llamar a la función para registrar el rostro
                registrarRostro()
            }
            .setNegativeButton("No, gracias") { dialog, id ->
                // No hacer nada o cerrar el diálogo sin hacer ninguna acción
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Registro de Rostro")
        alert.show()
    }

    private fun registrarRostro() {
        val intent = Intent(this, CameraxAddFaceActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("fromActivity", "RegistroUsuarioActivity")

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
        // Opcional: Finaliza esta actividad si no deseas volver a ella después de iniciar la siguiente
         finish()
    }

    fun Siguiente(view : View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
        finish() // Finaliza esta actividad al iniciar la siguiente
    }
}
