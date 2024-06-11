package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var userModel: UserModel
    private lateinit var horariosList: List<HorarioModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario) // Nombre corregido

        userModel = intent.getSerializableExtra("userModel") as UserModel

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
