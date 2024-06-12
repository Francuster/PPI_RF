package com.example.myapplication.model

import java.io.Serializable

data class LicenciaResponse(
    val licenciaId: String,
    val fechaDesde: String,
    val fechaHasta: String,
    val userId: String
):Serializable

