package com.example.myapplication.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.activity.EmpleadosLicenciasActivity
import com.example.myapplication.activity.InicioRrHhActivity
import com.example.myapplication.activity.InicioSeguridadActivity
import com.example.myapplication.activity.LicenciasEmpleadoActivity
import com.example.myapplication.model.Empleado
import com.example.myapplication.model.Licencia

var fallos_conexion: Int = 0


fun deviceIsConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}

fun isServiceRunning(context: Context, NetworkChangeService: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (NetworkChangeService.name == service.service.className) {
            return true
        }
    }
    return false
}

fun imageToggleAtras(imageView: ImageView, context: Context, action: String,empleados: ArrayList<Empleado>,licencias: ArrayList<Licencia>,empleado: ArrayList<Empleado>) {
    imageView.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.black))
                true
            }
            MotionEvent.ACTION_UP -> {
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.gris_flecha_volver))
                // Llamar a performClick() para manejar el clic correctamente
                v.performClick()
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.gris_flecha_volver))
                true
            }
            else -> false
        }
    }

    // Anular performClick para manejar el clic
    imageView.setOnClickListener {
        // Aquí puedes manejar el clic en la imagen
        goAtras(context, action, empleados,licencias,empleado )
    }
}

fun goAtras(context: Context, action: String, empleados: ArrayList<Empleado>, licencias: ArrayList<Licencia>, empleado: ArrayList<Empleado>) {
    if (action == "irInicioRrHhActivity") {
        val intent = Intent(context, InicioRrHhActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    if (action == "irInicioSeguridadActivity") {
        val intent = Intent(context, InicioSeguridadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    if (action == "irEmpleadosLicenciasActivity") {
        val intent = Intent(context, EmpleadosLicenciasActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(empleados))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    if (action == "irLicenciasEmpleadoActivity") {
        val intent = Intent(context, LicenciasEmpleadoActivity::class.java)
        intent.putParcelableArrayListExtra("listaEmpleados", ArrayList(empleados))
        intent.putParcelableArrayListExtra("licenciasEmpleado", ArrayList(licencias))
        intent.putParcelableArrayListExtra("empleadoBuscado", ArrayList(empleado))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


}


fun ImageView.changeColorTemporarily(color: Int, duration: Long) {
    val originalColorFilter = this.colorFilter
    this.setColorFilter(color)

    // Restaurar el color original después de un retraso
    Handler().postDelayed({
        this.colorFilter = originalColorFilter
    }, duration)
}

fun Button.changeTextColorTemporarily(color: Int, duration: Long) {
    val originalTextColor = this.currentTextColor
    this.setTextColor(color)

    // Restaurar el color original después de un retraso
    Handler().postDelayed({
        this.setTextColor(originalTextColor)
    }, duration)
}