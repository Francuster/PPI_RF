package com.example.myapplication.model

import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
//esta clase deberia borrarse
data class Persona(
    val numeroDocumento: String, //cambiar a number seguramente
    val nombre: String,
    val apellido: String,
    val lugaresAcceso: List<String>, //lista de lugares a donde puede ir tal persona
    val imagen: Bitmap, // imagen en formato ByteArray
    val rol: List<String>, //lista de roles de persona
)