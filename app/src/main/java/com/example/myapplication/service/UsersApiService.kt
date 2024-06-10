package com.example.myapplication.service

import com.example.myapplication.model.ImagenModel
import com.example.myapplication.model.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersApiService {

    @POST("/api/users")
    fun post(@Body userModel: UserModel): Call<ImagenModel>
    @PUT("/api/users/{id}")
    fun put(@Path("id") userId: String, @Body userModel: UserModel): Call<Void>
    @GET("/api/users")
    fun get(): Call<List<UserModel>>

    @GET("/api/users/{id}")
    fun getById(@Path("id") userId: String): Call<List<UserModel>>
    @DELETE("/api/users/{id}")
    fun delete(@Path("id") userId: String): Call<Void>
}