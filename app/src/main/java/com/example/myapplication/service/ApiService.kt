package com.example.myapplication.service

import com.example.myapplication.model.EmbeddingsRequest
import com.example.myapplication.model.EmbeddingsResponse
import com.example.myapplication.model.LicenciaRequest
import com.example.myapplication.model.LicenciaResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/login")
    fun loginWithEmbeddings(@Body request: EmbeddingsRequest): Call<EmbeddingsResponse>

    @POST("/api/authentication")
    fun authenticationWithEmbeddings(@Body request: EmbeddingsRequest): Call<EmbeddingsResponse>

    @POST("/api/licencias")
    fun createLicencia(@Body licenciaRequest: LicenciaRequest): Call<LicenciaResponse>

}