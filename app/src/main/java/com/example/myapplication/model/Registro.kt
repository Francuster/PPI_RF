package com.example.myapplication.model

data class Registro(
    var horario:String,  //Tal vez cambiar a Val, y setearle la fecha y hora actual siempre
    var nombre: String,
    var apellido: String,
    var dni: String,
    var estado: String,
    var tipo: String
)
