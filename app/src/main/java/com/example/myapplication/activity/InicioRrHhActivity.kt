package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException


class InicioRrHhActivity: AppCompatActivity() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inico_rrhh)

        fetchUser()

    }

    private fun fetchUser() {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL+"/api/users") // Cambia esto por la URL de tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        val jsonArray = JSONArray(it)
                        if (jsonArray.length() > 0) {
                            val lastUserJsonObject = jsonArray.getJSONObject(0) // Obtiene el último usuario de la lista ordenada
                            val userIdObject = lastUserJsonObject.getJSONObject("_id")
                            val userId = userIdObject.getString("\$oid")
                            val userName = lastUserJsonObject.getString("nombre")
                            val userSurname = lastUserJsonObject.getString("apellido")
                            val fullName = "$userName $userSurname"
                            runOnUiThread {
                                findViewById<TextView>(R.id.empleado).text = fullName
                                // Agregar un OnClickListener a la flecha de edición para pasar el ID al próximo Intent
                                findViewById<View>(R.id.imagen_flecha).setOnClickListener {
                                    goToModificacionUsuario(userId)
                                }
                            }
                        }
                    }
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




