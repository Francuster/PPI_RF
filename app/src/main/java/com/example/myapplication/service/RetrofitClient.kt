package com.example.myapplication.service

import com.example.myapplication.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val imagenApiService: ImagenesApiService = retrofit.create(ImagenesApiService::class.java)
    val userApiService: UsersApiService = retrofit.create(UsersApiService::class.java)
    val horariosApiService: HorariosApiService = retrofit.create(HorariosApiService::class.java)
    val configuracionesApiService: ConfiguracionesApiService = retrofit.create((ConfiguracionesApiService::class.java))
}