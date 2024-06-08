package com.example.myapplication.model

data class LicenciaRequest(
    val userId: String,
    val fechaDesde: String,
    val fechaHasta: String
){
}