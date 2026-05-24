package com.kedo.app.data

//IMPORTS
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La IP del emulador apuntando al puerto de mi Spring Boot
    private const val BASE_URL = "http://10.0.2.2:8081"

    val apiService: KedoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KedoApiService::class.java)
    }
}