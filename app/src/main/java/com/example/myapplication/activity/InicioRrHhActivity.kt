package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException


class InicioRrHhActivity: AppCompatActivity() {
    private val client = OkHttpClient()
    private var jsonArray = JSONArray()
    private val handler = Handler()
    val listaEmpleados = mutableListOf<Empleado>()
    private lateinit var runnable: Runnable

    private var nombre: String? = null
    private var apellido: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inico_rrhh)
        fetchUsers()
        nombre = intent.getStringExtra("nombre")
        apellido=intent.getStringExtra("apellido")//nombre para mostrar

        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text = "$nombre $apellido"

        // Programa la actualización de usuarios cada 10 segundos
        scheduleUserUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detén la actualización periódica cuando la actividad se destruye
        handler.removeCallbacks(runnable)
    }

    //TODO: sigue corriendo incluso despues de ir a otro activity
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

            for (i in 0 until jsonArray.length()) {
                val lastUserJsonObject = jsonArray.getJSONObject(i)
                val userIdObject = lastUserJsonObject.getJSONObject("_id")
                val userId = userIdObject.getString("\$oid")
                val userName = lastUserJsonObject.getString("nombre")
                val userSurname = lastUserJsonObject.getString("apellido")
                val fullName = "$userName $userSurname"

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)

                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = fullName
                var empleado = Empleado(fullName,userId)
                listaEmpleados.add(empleado)
                container.addView(itemView)

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    goToModificacionUsuario(userId,userName,userSurname)
                }
            }
        }
    }

    private fun fetchUsers() {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "/api/users") // Cambia esto por la URL de tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("FetchUsers", "Failed to fetch users", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        Log.d("FetchUsers", "Response data: $it")
                        jsonArray = JSONArray(it)
                        mostrarTodosLosEmpleados()
                    }
                } else {
                    Log.e("FetchUsers", "Unsuccessful response")
                }
            }
        })
    }

    private fun goToModificacionUsuario(userId: String, userName: String, userSurname: String) {
        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("user_name", userName)
        intent.putExtra("user_apellido", userSurname)
        startActivity(intent)
//        handler.removeCallbacks(runnable)
    }


    fun goToRegistroRrHhPrimeraSala(view: View) {

        val intent = Intent(applicationContext, RegistroUsuarioActivity::class.java)
        startActivity(intent)

    }


    fun goToCfgCerteza(view: View) {

        val intent = Intent(applicationContext, ConfiguracionRRHHActivity::class.java)
        startActivity(intent)

    }
    fun goToLicences(view: View){
        val intent = Intent(applicationContext, EmpleadosLicenciasActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(listaEmpleados))
        startActivity(intent)

    }

}




