package com.example.myapplication.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.RetrofitClient
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class InicioRrHhActivity : AppCompatActivity() {
    private lateinit var miVista: View
    private lateinit var loadingOverlayout: View
    private var userModelList = arrayListOf<UserModel>()
    private var listaEmpleados = arrayListOf<Empleado>()

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
        loadingOverlayout = findViewById(R.id.loading_overlayout)
        miVista = findViewById(R.id.layout_hijo)
        if (GlobalData.empleado == null) {
            nombre = intent.getStringExtra("nombre")
            apellido = intent.getStringExtra("apellido") // nombre para mostrar
            empleadoId = intent.getStringExtra("_id")
            GlobalData.empleado = Empleado(fullName = "$nombre $apellido", userId = "$empleadoId")
        }

        val textoNombreUsuario = findViewById<TextView>(R.id.usuario)
        textoNombreUsuario.text = GlobalData.empleado!!.fullName
    }

    override fun onResume() {
        super.onResume()
        fetchUsers()
    }

    private fun aumentarOpacidad() {
        runOnUiThread {
            val animator = ObjectAnimator.ofFloat(miVista, "alpha", 0.1f, 1f)
            animator.duration = 500
            animator.start()
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

    private fun fetchUsers() {
        miVista.alpha = 0.10f // 10% de opacidad
        showLoadingOverlay()
        RetrofitClient.userApiService.get().enqueue(object : retrofit2.Callback<List<UserModel>> {
            override fun onResponse(
                call: retrofit2.Call<List<UserModel>>,
                response: retrofit2.Response<List<UserModel>>
            ) {
                runOnUiThread {
                    aumentarOpacidad()
                }
                hideLoadingOverlay()
                if (response.isSuccessful) {
                    userModelList = response.body() as ArrayList<UserModel>
                    mostrarTodosLosEmpleados()
                } else {
                    Log.e("fetchUsers", "error code: " + response.code())
                }
            }

            override fun onFailure(call: retrofit2.Call<List<UserModel>>, t: Throwable) {
                hideLoadingOverlay()
                runOnUiThread {
                    aumentarOpacidad()
                }
                Log.e("fetchUsers", "Error al traer usuarios", t)
            }
        })
    }

    private fun mostrarTodosLosEmpleados() {
        val container: LinearLayout = findViewById(R.id.container)

        runOnUiThread {
            container.removeAllViews() // Elimina vistas antiguas antes de agregar las nuevas

            for (user in userModelList) {
                val inflater: LayoutInflater = LayoutInflater.from(this@InicioRrHhActivity)
                val itemView: View = inflater.inflate(R.layout.item_usuario, container, false)
                val textViewEmpleado: TextView = itemView.findViewById(R.id.empleado)
                textViewEmpleado.text = user.getFullName()
                if (user.rol.uppercase() != "ESTUDIANTE") {
                    listaEmpleados.add(Empleado(user.getFullName(), user._id))
                }
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

    fun perfilDetailAlert(view: View) {
        obtenerYMostrarDetallesPerfil()
    }

    fun mostrarIngresosEgresosDelDia(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Ingresos y Egresos del Día")

            val dialogLayout = ScrollView(this@InicioRrHhActivity)
            dialogLayout.setPadding(24, 24, 24, 24)

            val container = LinearLayout(this@InicioRrHhActivity)
            container.orientation = LinearLayout.VERTICAL
            dialogLayout.addView(container)

            val totalLogsTextView = TextView(this@InicioRrHhActivity)
            totalLogsTextView.textSize = 16f
            totalLogsTextView.setTextColor(ContextCompat.getColor(this@InicioRrHhActivity, R.color.black))
            totalLogsTextView.setPadding(0, 16, 0, 16)
            container.addView(totalLogsTextView)

            val logContainer = LinearLayout(this@InicioRrHhActivity)
            logContainer.orientation = LinearLayout.VERTICAL
            container.addView(logContainer)

            val url = "${BuildConfig.BASE_URL}/api/logs/day?fecha=${getCurrentDate()}"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@InicioRrHhActivity, "Error al cargar los logs: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseBody ->
                            runOnUiThread {
                                displayLogs(responseBody, logContainer, totalLogsTextView)
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@InicioRrHhActivity, "Error: ${response.code} - ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

            setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }

            setView(dialogLayout)
            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        // Configurar el tamaño del diálogo
        val window = alertDialog.window
        val attributes = window?.attributes
        attributes?.width = LinearLayout.LayoutParams.MATCH_PARENT
        attributes?.height = LinearLayout.LayoutParams.WRAP_CONTENT
        window?.attributes = attributes as WindowManager.LayoutParams
    }



    private fun displayLogs(responseBody: String, logContainer: LinearLayout, totalLogsTextView: TextView) {
        logContainer.removeAllViews()

        try {
            val logsArray = JSONArray(responseBody)
            val totalLogs = logsArray.length()
            totalLogsTextView.text = "Total logs del día: $totalLogs"

            for (i in 0 until logsArray.length()) {
                val logNumber = i + 1
                val log = logsArray.getJSONObject(i)
                val logText = """
                    $logNumber. ┌────────────────────┐
                         Nombre: ${log.getString("nombre").padEnd(18)} 
                         Apellido: ${log.getString("apellido").padEnd(16)} 
                         DNI: ${log.getInt("dni").toString().padEnd(21)} 
                         Estado: ${log.getString("estado").padEnd(16)} 
                         Horario: ${log.getString("horario").padEnd(15)} 
                         Tipo: ${log.getString("tipo").padEnd(18)} 
                        └────────────────────┘
                    """.trimIndent()

                val textView = TextView(this@InicioRrHhActivity)
                textView.text = logText
                textView.setPadding(24, 16, 24, 16)
                textView.setTextColor(ContextCompat.getColor(this@InicioRrHhActivity, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                logContainer.addView(textView)  // Añadir cada log a la lista
            }
        } catch (e: Exception) {
            Toast.makeText(this@InicioRrHhActivity, "Error al parsear los logs: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun obtenerYMostrarDetallesPerfil() {
        val empleado = GlobalData.empleado ?: return // Verificar que el empleado no sea nulo
        val empleadoId = empleado.userId // Obtener el ID del empleado

        // Llamar al método del servicio para obtener los detalles del empleado por su ID
        RetrofitClient.userApiService.getById(empleadoId).enqueue(object : retrofit2.Callback<UserModel> {
            override fun onResponse(call: retrofit2.Call<UserModel>, response: retrofit2.Response<UserModel>) {
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

            override fun onFailure(call: retrofit2.Call<UserModel>, t: Throwable) {
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
            RetrofitClient.horariosApiService.getById(horarioId).enqueue(object : retrofit2.Callback<HorarioModel> {
                override fun onResponse(call: retrofit2.Call<HorarioModel>, response: retrofit2.Response<HorarioModel>) {
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

                override fun onFailure(call: retrofit2.Call<HorarioModel>, t: Throwable) {
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

    fun dialogCloseSession(view: View) {
        val mensaje = "¿Quiere cerrar la sesión?"
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Sesion")
            setMessage(mensaje)

            setPositiveButton("OK") { dialog, which ->
                goToLogin()
            }

            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            setCancelable(true)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun goToLogin() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}