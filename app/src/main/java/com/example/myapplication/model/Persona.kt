package com.example.myapplication.model

data class Persona(
    val numeroDocumento: String, //cambiar a number seguramente
    val nombre: String,
    val apellido: String,
    val lugaresAcceso: List<String>, //lista de lugares a donde puede ir tal persona
    val imagen: ByteArray? // imagen en formato ByteArray
)