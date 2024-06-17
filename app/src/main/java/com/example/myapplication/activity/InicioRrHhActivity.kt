package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioRrHhActivity : AppCompatActivity() {
    private var userModelList = arrayListOf<UserModel>()
    private val handler = Handler()
    private var listaEmpleados = arrayListOf<Empleado>()
    private lateinit var runnable: Runnable
    object GlobalData {
        var empleado: Empleado? = null
        var cantEmpleados: Int = 0
    }
    private var nombre: String? = null
    private var apellido: String? = null
    private var empleadoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inico_rrhh)
        fetchUsers()
        if (GlobalData.empleado == null) {
            nombre = intent.getStringExtra("nombre")
            apellido = intent.getStringExtra("apellido") // nombre para mostrar
            empleadoId = intent.getStringExtra("_id")
            GlobalData.empleado = Empleado(fullName = "$nombre $apellido", userId = "$empleadoId")
        }
        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text = GlobalData.empleado!!.fullName



        // Programa la actualización de usuarios cada 10 segundos
        scheduleUserUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    private fun fetchUsers() {
        RetrofitClient.userApiService.get().enqueue(object : Callback<List<UserModel>> {
            override fun onResponse(
                call: Call<List<UserModel>>,
                response: Response<List<UserModel>>
            ) {
                if (response.code() == 200) {
                    userModelList = response.body() as ArrayList<UserModel>
                    mostrarTodosLosEmpleados()
                } else {
                    Log.e("fetchUsers", "error code: " + response.code())
                }
            }

            override fun onFailure(call: Call<List<UserModel>>, t: Throwable) {
                Log.e("fetchUsers", "Error al traer usuarios")
            }
        })

    }

    // TODO: sigue corriendo incluso después de ir a otro activity
    private fun scheduleUserUpdate() {
        //        runnable = Runnable {
        //            fetchUsers()
        //            // Vuelve a programar la actualización después de 10 segundos
        //            handler.postDelayed(runnable, 10000)
        //        }

        //        // Programa la primera ejecución después de 10 segundos
        //        handler.postDelayed(runnable, 10000)
    }

    private fun mostrarTodosLosEmpleados() {
        val container: LinearLayout = findViewById(R.id.container)

        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (user in userModelList) {
                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)
                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = user.getFullName()

                listaEmpleados.add(Empleado(user.getFullName(), user._id))
                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_editar).setOnClickListener {
                    goToModificacionUsuario(user)
                }
                itemView.findViewById<View>(R.id.imagen_ojo).setOnClickListener {
                    goToVerPerfil(user)
                }

                // Ocultar la flecha
                val imagenFlecha: ImageView = itemView.findViewById(R.id.imagen_flecha)
                imagenFlecha.visibility = View.GONE
            }
        }
    }


    private fun goToVerPerfil(userModel: UserModel) {
        val intent = Intent(applicationContext, PerfilUsuarioActivity::class.java)
        intent.putExtra("userModel", userModel)
        startActivity(intent)
    }

    private fun goToModificacionUsuario(userModel: UserModel) {
        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        intent.putExtra("userModel", userModel)
        startActivity(intent)
    }

    fun goToRegistroRrHhPrimeraSala(view: View) {
        val intent = Intent(applicationContext, RegistroUsuarioActivity::class.java)
        startActivity(intent)
    }

    fun goToCfgCerteza(view: View) {
        val intent = Intent(applicationContext, ConfiguracionRRHHActivity::class.java)
        startActivity(intent)
    }
   fun perfilDetailAlert(view: View){
       obtenerYMostrarDetallesPerfil()
   }
    private fun obtenerYMostrarDetallesPerfil() {
        val empleado = GlobalData.empleado ?: return // Verificar que el empleado no sea nulo
        val empleadoId = empleado.userId // Obtener el ID del empleado

        // Llamar al método del servicio para obtener los detalles del empleado por su ID
        RetrofitClient.userApiService.getById(empleadoId).enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val userModel = response.body()

                    if (userModel != null) {
                        obtenerYMostrarHorarios(userModel)
                    } else {
                        mostrarDialogoError()
                    }
                } else {
                    mostrarDialogoError()
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                mostrarDialogoError()
            }
        })
    }

    private fun obtenerYMostrarHorarios(userModel: UserModel) {
        val detallesBuilder = StringBuilder()
        detallesBuilder.append("Nombre: ${userModel.nombre}\n")
        detallesBuilder.append("Apellido: ${userModel.apellido}\n")
        detallesBuilder.append("DNI: ${userModel.dni}\n")
        detallesBuilder.append("Email: ${userModel.email}\n")

        val horarios = mutableListOf<String>()
        for (horarioId in userModel.horarios) {
            RetrofitClient.horariosApiService.getById(horarioId).enqueue(object : Callback<HorarioModel> {
                override fun onResponse(call: Call<HorarioModel>, response: Response<HorarioModel>) {
                    if (response.isSuccessful) {
                        val horario = response.body()
                        if (horario != null) {
                            horarios.add(horario.getFullName())
                            if (horarios.size == userModel.horarios.size) {
                                detallesBuilder.append("Horarios: ${horarios.joinToString(", ")}\n")
                                mostrarDialogoPerfil(detallesBuilder.toString())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<HorarioModel>, t: Throwable) {
                    mostrarDialogoError()
                }
            })
        }
    }

    private fun mostrarDialogoPerfil(detalles: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Detalles del Perfil")
            setMessage(detalles)

            setPositiveButton("OK") { dialog, which ->
                // Aquí puedes añadir alguna acción si lo deseas
            }

            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun mostrarDialogoError() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Error")
            setMessage("No se pudo obtener los detalles del perfil")

            setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun goToLicences(view: View) {
        val intent = Intent(applicationContext, EmpleadosLicenciasActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        startActivity(intent)
    }
}
