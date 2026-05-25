package com.kedo.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8081"

    val apiService: KedoApiService by lazy {
        // 1. Creamos el "espía" para que imprima todo el tráfico de red
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        // 2. Añadimos el espía al motor de conexión (OkHttp)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // 3. Se lo pasamos a Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // <-- Le inyectamos el motor modificado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KedoApiService::class.java)
    }
}