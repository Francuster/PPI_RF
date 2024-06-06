package com.example.myapplication.model

data class CorteInternet(
    val horarioDesconexion: String,
    val horarioReconexion:String,
    val cantRegistros: Int,
    val periodoDeCorte: String
)
