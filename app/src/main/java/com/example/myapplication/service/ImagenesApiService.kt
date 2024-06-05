package com.example.myapplication.service

import com.example.myapplication.model.EmbeddingsRequest
import com.example.myapplication.model.EmbeddingsResponse
import com.example.myapplication.model.ImagenModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ImagenesApiService {

    @POST("/api/imagenes")
    fun postImagenes(@Body request: ImagenModel): Call<ImagenModel>

    @GET("/api/imagenes")
    fun getImagenes(@Query("userId") userId: String): Call<List<ImagenModel>>

    @PUT("/api/imagenes")
    fun putImagenes(@Body request: ImagenModel): Call<Void>

    @DELETE("/api/imagenes")
    fun deleteImagenes(@Body request: ImagenModel): Call<Void>
}