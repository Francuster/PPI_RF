package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.UserModel

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var userModel: UserModel

    private lateinit var imagenUsuarioImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario) // Nombre corregido

        userModel = intent.getSerializableExtra("userModel") as UserModel

        imagenUsuarioImageView = findViewById(R.id.imagenPerfil)

        cargarDatos(userModel)
    }

    private fun cargarDatos(userModel: UserModel){
        val textoNombreUsuario = findViewById<TextView>(R.id.nombre_texto)
        val textoApellidoUsuario = findViewById<TextView>(R.id.apellido_texto)
        val textoMailUsuario =findViewById<TextView>(R.id.email_texto)
        val textoDocumentoUsuario =findViewById<TextView>(R.id.documento_texto)
        val horaEntradaTextView = findViewById<TextView>(R.id.hora_entrada)
        val horaSalidaTextView = findViewById<TextView>(R.id.hora_salida)
        val rolTextView = findViewById<TextView>(R.id.rol_texto)

        runOnUiThread {
            textoNombreUsuario.text = userModel.nombre
            textoApellidoUsuario.text = userModel.apellido
            horaEntradaTextView.text = ""
            horaSalidaTextView.text = ""
            rolTextView.text = userModel.rol
            textoDocumentoUsuario.text = userModel.dni.toString()
            textoMailUsuario.text= userModel.email

        }
    }

    fun goToAtrasIniRRHH(view: View) {
        val intent = Intent(applicationContext, InicioRrHhActivity::class.java)
        startActivity(intent)
    }

    fun goToPerfilUsuario(view: View) {
        val intent = Intent(applicationContext, PerfilUsuarioActivity::class.java)
        startActivity(intent)
    }
}
