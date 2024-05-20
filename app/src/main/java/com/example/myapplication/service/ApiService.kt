package com.example.myapplication.service

import com.example.myapplication.model.EmbeddingsRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/login2")
    fun sendFloats(@Body request: EmbeddingsRequest): Call<Void>
}