package com.example.myapplication.model

class HorarioModel(
    val _id: String,
    val horarioEntrada: String,
    val horarioSalida: String,
    val tipo: String
) {

    fun getFullName(): String {
        return "${horarioEntrada}-${horarioSalida} ${tipo}"
    }
}