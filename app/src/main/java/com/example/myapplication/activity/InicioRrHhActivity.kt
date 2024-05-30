package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class InicioRrHhActivity: AppCompatActivity() {
    private val client = OkHttpClient()
    private var JsonArray = JSONArray()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inico_rrhh)
        fetchUsers()
    }

    private fun mostrarTodosLosEmpleados() {
        val container: LinearLayout = findViewById(R.id.container)

        runOnUiThread {
            Log.d("MostrarEmpleados", "JsonArray length: ${JsonArray.length()}")

            for (i in 0 until JsonArray.length()) {
                val lastUserJsonObject = JsonArray.getJSONObject(i)
                val userIdObject = lastUserJsonObject.getJSONObject("_id")
                val userId = userIdObject.getString("\$oid")
                val userName = lastUserJsonObject.getString("nombre")
                val userSurname = lastUserJsonObject.getString("apellido")
                val fullName = "$userName $userSurname"

                Log.d("MostrarEmpleados", "User: $fullName")

                val inflater: LayoutInflater = LayoutInflater.from(this)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)

                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = fullName

                container.addView(itemView)

                Log.d("LinearLayout", "Se ha agregado una vista de usuario al LinearLayout")

                itemView.findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                    goToModificacionUsuario(userId)
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
                        JsonArray = JSONArray(it)
                        mostrarTodosLosEmpleados()
                    }
                } else {
                    Log.e("FetchUsers", "Unsuccessful response")
                }
            }
        })
    }

    private fun goToModificacionUsuario(userId: String) {
        val intent = Intent(applicationContext, ModificacionUsuarioActivity::class.java)
        intent.putExtra("user_id", userId)
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

}




