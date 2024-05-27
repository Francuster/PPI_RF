package com.example.myapplication.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.myapplication.model.Registro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATABASE_NAME = "log3r"
const val DATABASE_VERSION = 2
const val TAG = "Connection"

/*
Llamar desde el activity deseado:
  val readableDB = Connection(applicationContext).readableDatabase
  // logic
  readableDB.close()

  O

  val writableDB = Connection(applicationContext).writableDatabase
  // logic
  writableDB.close()
*/
class Connection(val ctx: Context) : SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
  override fun onCreate(db: SQLiteDatabase?) {
    if (db == null) {
      Log.e(TAG,"DB is null in onCreate method")
      return
    }
    val CREATE_TABLE_USUARIOS="""
      CREATE TABLE usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nombre TEXT NOT NULL,
        apellido TEXT NOT NULL,
        email TEXT NOT NULL,
        legajo TEXT NOT NULL,
        password TEXT DEFAULT NULL,
        biometric_data BLOB NOT NULL,
        rol INTEGER NOT NULL -- 1=seguridad, 2=entrantes(profes, alumnos, etc.)
      );""".trimIndent()


    val CREATE_TABLE_INGRESOS=""" 
    CREATE TABLE ingresos (
      horario TEXT,
      nombre TEXT,
      apellido TEXT,
      dni TEXT,
      estado TEXT,
      tipo TEXT
    );
    """.trimIndent()

    val CREATE_TABLE_VISITANTES="""
    CREATE TABLE visitantes (
      id_ingreso INTEGER PRIMARY KEY,
      nombre_apellido TEXT NOT NULL,
      dni INTEGER NOT NULL,
      detalles TEXT NOT NULL DEFAULT '',

      FOREIGN KEY(id_ingreso) REFERENCES ingresos(id)
    );
    """.trimIndent()

    val CREATE_TABLE_ADMINS="""
    CREATE TABLE admins (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      nombre TEXT NOT NULL,
      apellido TEXT NOT NULL,
      email TEXT NOT NULL,
      password TEXT DEFAULT NULL
    );
    """.trimIndent()

    val CREATE_TABLE_LOGS="""
    CREATE TABLE logs (
      horario TEXT NOT NULL,
      nombre TEXT NOT NULL,
      apellido TEXT NOT NULL,
      dni INTEGER NOT NULL,
      estado TEXT NOT NULL,
      tipo TEXT NOT NULL
    );
    """.trimIndent()

    db.execSQL(CREATE_TABLE_USUARIOS)
    db.execSQL(CREATE_TABLE_INGRESOS)
    db.execSQL(CREATE_TABLE_VISITANTES)
    db.execSQL(CREATE_TABLE_ADMINS)
    db.execSQL(CREATE_TABLE_LOGS)

    //Prueba de insert en ingresos

      val horario = "2024-05-17 12:00:00"
      val nombre = "prueba"
      val apellido = "update logs local"
      val dni = "12345678"
      val estado = "Activo"
      val tipo = "Entrada"

      val insertQuery = """
    INSERT INTO ingresos (horario, nombre, apellido, dni, estado, tipo)
    VALUES ('$horario', '$nombre', '$apellido', '$dni', '$estado', '$tipo');
""".trimIndent()

      db.execSQL(insertQuery)


      db.execSQL("INSERT INTO usuarios VALUES (null, 'nombreGuardia1', 'apellidoGuardia1', 'guardia@gmail.com', 'legajo1234'," +
        "'12345678', 'imagenOToken', 1)")

    Log.i(TAG,"DB esta creada")
    //Toast.makeText(ctx, "DB creada onCreate", Toast.LENGTH_LONG).show()
  }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db == null) {
            Log.e(TAG, "DB is null in onUpgrade method")
            return
        }
        db.execSQL("DROP TABLE IF EXISTS visitantes")
        db.execSQL("DROP TABLE IF EXISTS ingresos")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS admins")
        db.execSQL("DROP TABLE IF EXISTS logs")

        onCreate(db)
        Toast.makeText(ctx, "DB actualizada onUpgrade oldVersion: $oldVersion, newVersion: $newVersion", Toast.LENGTH_LONG).show()
    }

  override fun onOpen(db: SQLiteDatabase?) {
    super.onOpen(db)
//    Toast.makeText(ctx, "DB abierta onOpen", Toast.LENGTH_LONG).show()
      Log.e(TAG,"DB abierta onOpen")

  }
}