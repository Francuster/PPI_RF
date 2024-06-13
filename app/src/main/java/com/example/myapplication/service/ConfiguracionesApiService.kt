package com.example.myapplication.service

import com.example.myapplication.model.ConfiguracionModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ConfiguracionesApiService {

    @POST("/api/config")
    fun post(@Body configuracionModel: ConfiguracionModel): Call<ConfiguracionModel>
    @PUT("/api/config")
    fun put(@Body configuracionModel: ConfiguracionModel): Call<Void>
    @GET("/api/config/{nombre}")
    fun getByName(@Path("nombre") nombre: String): Call<ConfiguracionModel>
    @DELETE("/api/config")
    fun delete(@Body configuracionModel: ConfiguracionModel): Call<Void>
}