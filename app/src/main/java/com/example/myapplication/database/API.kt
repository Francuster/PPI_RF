package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

//import com.example.myapplication.entities.Usuario
/*
object API {
  fun ingresarEntradaUsuario(usuario: Usuario){

  }

  fun salidaUsuario(ingreso: Ingreso usuario: Usuario)
}*/
fun registrarLogs(context: Context, nombre: String, apellido: String, dni: Int, estado: String, tipo: String) {
    val databaseConnection = Connection(context)
    val db = databaseConnection.writableDatabase
    val fechaActual = obtenerFechaActualISO()
    try {
        // Registrar el visitante en la tabla de Logs
        val queryLogs = "INSERT INTO logs (horario, nombre, apellido, dni, estado, tipo) VALUES (?, ?, ?, ?, ?, ?)"
        db.execSQL(queryLogs, arrayOf(fechaActual, nombre, apellido, dni, estado, tipo))
    } catch (e: Exception) {
        // Manejar cualquier excepción
        Log.e(TAG, "Error al registrar Log", e)
    } finally {
        // Cerrar la conexión a la base de datos
        Log.i(TAG, "Entrada de usuario insertada correctamente en la tabla LOGS con horario: $fechaActual")
        db.close()
    }
}

fun obtenerFechaActualISO(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}

/**fun obtenerFechaActualISO(): String {
    // Crear un objeto Calendar para obtener la fecha y hora actual
    val calendar = Calendar.getInstance()

    // Crear un formato de fecha ISO 8601
    val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    // Obtener la fecha actual en formato ISO 8601
    val fechaActualISO = formatoISO.format(calendar.time)

    // Retornar la fecha en formato ISO
    return fechaActualISO
}**/
fun insertIntoIngresos(context: Context, usuarioID: Int?, fechaHoraEntrada: String, entradaSalidaOnline: Int) {
    val databaseConnection = Connection(context)
    val db: SQLiteDatabase = databaseConnection.writableDatabase

    try {
        // Crea un objeto ContentValues para almacenar los datos que se insertarán
        val values = ContentValues().apply {
            put("id_usuario", usuarioID)
            put("fecha_hora_entrada", fechaHoraEntrada)
            put("entrada_online", entradaSalidaOnline)
            // Si quieres, puedes agregar los valores para las columnas opcionales (fecha_hora_salida y salida_online)
        }

        // Inserta los datos en la tabla `ingresos`
        val newRowId = db.insert("ingresos", null, values)

        if (newRowId == -1L) {
            // Manejar el error de inserción
            Log.e(TAG, "Error al insertar la entrada de usuario en la tabla ingresos.")
        } else {
            Log.i(TAG, "Entrada de usuario insertada correctamente en la tabla ingresos con ID: $newRowId")

        }
    } catch (e: Exception) {
        // Manejar la excepción si algo sale mal
        Log.e(TAG, "Error al insertar la entrada de usuario en la tabla ingresos.", e)
    } finally {
        // Cerrar la base de datos
        db.close()
    }
}

fun verificarBaseDatos(context: Context) {
    val databaseConnection = Connection(context)
    val db = databaseConnection.readableDatabase

    try {
        // Consulta la tabla 'usuarios' para verificar si tiene registros
        val cursorUsuarios = db.query("usuarios", null, null, null, null, null, null)
        if (cursorUsuarios.moveToFirst()) {
            Log.i(TAG, "Tabla 'usuarios' cargada correctamente.")
        } else {
            Log.e(TAG, "Tabla 'usuarios' está vacía o no se cargó correctamente.")
        }
        cursorUsuarios.close()

        // Consulta la tabla 'ingresos' para verificar si tiene registros
        val cursorIngresos = db.query("ingresos", null, null, null, null, null, null)
        if (cursorIngresos.moveToFirst()) {
            Log.i(TAG, "Tabla 'ingresos' cargada correctamente.")
        } else {
            Log.e(TAG, "Tabla 'ingresos' está vacía o no se cargó correctamente.")
        }
        cursorIngresos.close()

        // Puedes realizar consultas similares para otras tablas según sea necesario

    } catch (e: Exception) {
        Log.e(TAG, "Error al verificar la base de datos.", e)
    } finally {
        db.close()
    }
}

fun obtenerIdUsuarioPorLegajo(context: Context, legajo: String): Int? {
    // Crear una instancia de la conexión a la base de datos
    val databaseConnection = Connection(context)
    val db = databaseConnection.readableDatabase

    // Declarar una variable para almacenar el ID del usuario
    var idUsuario: Int? = null

    try {
        // Consulta SQL para buscar el id_usuario con el legajo proporcionado
        val cursor = db.query(
            "usuarios", // Nombre de la tabla
            arrayOf("id"), // Columnas que queremos seleccionar
            "legajo = ?", // Cláusula WHERE
            arrayOf(legajo), // Parámetros de la consulta
            null, // GROUP BY
            null, // HAVING
            null  // ORDER BY
        )

        // Si se encuentra un registro, obtener el id_usuario
        if (cursor.moveToFirst()) {
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        // Cerrar el cursor
        cursor.close()
    } catch (e: Exception) {
        // Manejar cualquier excepción que pueda ocurrir
        Log.e(TAG, "Error al obtener id_usuario por legajo: $legajo", e)
    } finally {
        // Cerrar la conexión a la base de datos
        db.close()
    }

    // Devolver el id_usuario (o null si no se encontró)
    return idUsuario
}

