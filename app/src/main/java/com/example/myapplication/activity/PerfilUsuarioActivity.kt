package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.Licencia
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.imageToggleAtras
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilUsuarioActivity : AppCompatActivity() {
    private lateinit var loadingOverlayout: View
    private lateinit var userModel: UserModel
    private lateinit var horariosList: List<HorarioModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario) // Nombre corregido
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        userModel = intent.getSerializableExtra("userModel") as UserModel
        val imageView = findViewById<ImageView>(R.id.imagen_volver)
        imageToggleAtras(imageView,applicationContext,"irInicioRrHhActivity",ArrayList<Empleado>(),ArrayList<Licencia>(),ArrayList<Empleado>())
        cargarDatos(userModel)
        getHorarios()
    }

    private fun cargarDatos(userModel: UserModel){
        val textoNombreUsuario = findViewById<TextView>(R.id.nombre_texto)
        val textoApellidoUsuario = findViewById<TextView>(R.id.apellido_texto)
        val textoMailUsuario =findViewById<TextView>(R.id.email_texto)
        val textoDocumentoUsuario =findViewById<TextView>(R.id.documento_texto)
        val rolTextView = findViewById<TextView>(R.id.rol_texto)

        runOnUiThread {
            textoNombreUsuario.text = userModel.nombre
            textoApellidoUsuario.text = userModel.apellido
            rolTextView.text = userModel.rol
            textoDocumentoUsuario.text = userModel.dni.toString()
            textoMailUsuario.text= userModel.email

        }
    }

    private fun showLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingOverlay() {
        runOnUiThread {
            loadingOverlayout.visibility = View.GONE
        }
    }

    fun eliminarUsuario(view: View) {
        val miVista = findViewById<View>(R.id.layout_hijo)
        miVista.alpha = 0.10f // 10% de opacidad
        showLoadingOverlay()
        // Crear un AlertDialog de confirmación antes de eliminar
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Está seguro que desea eliminar este usuario?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Proceder con la eliminación del usuario si el usuario confirma
                RetrofitClient.userApiService.delete(userModel._id)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            hideLoadingOverlay()
                            if (response.isSuccessful) {
                                // Eliminar usuario correctamente, regresar a InicioRrHhActivity
                                Toast.makeText(applicationContext, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                                goToAtrasIniRRHH(view)
                            } else {
                                Log.e("PerfilUsuario", "Eliminar usuario fallido: ${response.code()}")
                                Toast.makeText(applicationContext, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("PerfilUsuario", "Eliminar usuario onFailure", t)
                            hideLoadingOverlay()
                            Toast.makeText(applicationContext, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancelar", null) // Opción para cancelar la eliminación
            .show()
    }


    fun getHorarios(){
        RetrofitClient.horariosApiService.get().enqueue(object: Callback<List<HorarioModel>>{
            override fun onResponse(
                call: Call<List<HorarioModel>>,
                response: Response<List<HorarioModel>>
            ) {

                if(response.code() == 200){
                    if(response.body() != null){
                        Log.i("PerfilUsuario", response.body().toString())

                        horariosList = response.body()!!
                        selectHorario()
                    }
                }
            }

            override fun onFailure(call: Call<List<HorarioModel>>, t: Throwable) {
                Log.e("PerfilUsuario", "getHorarios onFailure")
            }
        })
    }

    fun selectHorario(){
        val textoHorario = findViewById<TextView>(R.id.horario_texto)

        val horario = horariosList.find {
            it._id == userModel.horarios[0]
        }
        if (horario != null) {
            textoHorario.text = horario.getFullName()
        }
//        val index = horariosList.indexOfFirst {
//            it._id == userModel.horarios[0]
//        }


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
