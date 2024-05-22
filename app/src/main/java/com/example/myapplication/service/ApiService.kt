package com.example.myapplication.service

import com.example.myapplication.model.EmbeddingsRequest
import com.example.myapplication.model.EmbeddingsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/login2")
    fun loginWithEmbeddings(@Body request: EmbeddingsRequest): Call<EmbeddingsResponse>

    @POST("/api/authentication2")
    fun authenticationWithEmbeddings(@Body request: EmbeddingsRequest): Call<EmbeddingsResponse>
}