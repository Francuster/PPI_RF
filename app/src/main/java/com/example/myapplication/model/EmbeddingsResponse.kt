package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class EmbeddingsResponse (
    val data: Data,
    val message: String
) : Serializable

data class Data(
    val _id: String,
    val apellido: String,
    val dni: Int,
    val image: List<Float>,
    val label: Int,
    val lugares: List<String>,
    val nombre: String,
    val rol: String
) : Serializable
