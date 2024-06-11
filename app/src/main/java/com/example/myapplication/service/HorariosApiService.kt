package com.example.myapplication.service

import com.example.myapplication.model.HorarioModel
import com.example.myapplication.model.ImagenModel
import com.example.myapplication.model.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HorariosApiService {

    @POST("/api/horarios")
    fun post(@Body horarioModel: HorarioModel): Call<HorarioModel>
    @PUT("/api/horarios/{id}")
    fun put(@Path("id") id: String, @Body horarioModel: HorarioModel): Call<Void>
    @GET("/api/horarios")
    fun get(): Call<List<HorarioModel>>

    @GET("/api/horarios/{id}")
    fun getById(@Path("id") id: String): Call<HorarioModel>
    @DELETE("/api/horarios/{id}")
    fun delete(@Path("id") id: String): Call<Void>
}