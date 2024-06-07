package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.NetworkChangeService
import com.example.myapplication.utils.isServiceRunning

class ModificacionRolActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isServiceRunning(applicationContext, NetworkChangeService::class.java)) {
            val intent = Intent(this, NetworkChangeService::class.java)
            startService(intent)
        }

        setContentView(R.layout.modificacion_rol)
        spinner = findViewById<Spinner>(R.id.tipo_cuenta_modificacion)
        val elementos = listOf("DOCENTE","NO DOCENTE", "SEGURIDAD","PERSONAL JERÁRQUICO","ADMINISTRADOR","ESTUDIANTE","RECURSOS HUMANOS")


        val adaptador = ArrayAdapter(this, R.layout.desplegable_tipo_cuenta, elementos)
        adaptador.setDropDownViewResource(R.layout.desplegable_tipo_cuenta)
        spinner.adapter = adaptador
    }
    fun goToAtras(view: View) {
        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        startActivity(intent)
    }
    fun Listo(view: View) {
        val selectedRole = spinner.selectedItem.toString()

        // Validar que se haya seleccionado un rol antes de avanzar
        if (selectedRole.isNotEmpty()) {
            val intent = Intent()
            intent.putExtra("rol_modificado", selectedRole)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            // Mostrar un mensaje de error al usuario
            Toast.makeText(this, "Por favor selecciona un rol", Toast.LENGTH_SHORT).show()

        }
    }
}
