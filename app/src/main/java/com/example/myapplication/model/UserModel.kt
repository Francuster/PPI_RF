package com.example.myapplication.model

import java.io.Serializable

class UserModel (
    val _id: String,
    val nombre: String,
    val apellido: String,
    val dni: Int,
    val rol: String,
    val horarios: List<String>,
    val email: String
): Serializable {

    public fun getFullName(): String {
        return "${nombre} ${apellido}"
    }
}