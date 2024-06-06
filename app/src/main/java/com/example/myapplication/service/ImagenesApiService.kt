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
import retrofit2.http.Path
import retrofit2.http.Query

interface ImagenesApiService {

    @POST("images")
    fun postImagenes(@Body imagenModel: ImagenModel): Call<Void>
    @PUT("images/{userId}")
    fun putImagenes(@Path("userId") userId: String, @Body imagenModel: ImagenModel): Call<Void>
    @GET("/api/imagenes")
    fun getImagenes(@Query("userId") userId: String): Call<List<ImagenModel>>

    @DELETE("/api/imagenes")
    fun deleteImagenes(@Body request: ImagenModel): Call<Void>
}