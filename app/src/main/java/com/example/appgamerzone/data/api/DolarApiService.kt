package com.example.appgamerzone.data.api

import com.example.appgamerzone.data.model.DolarResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * API Service para obtener la cotización del dólar
 */
interface DolarApiService {

    @GET("v1/cotizaciones/usd")
    suspend fun getCotizacionUSD(): DolarResponse

    companion object {
        private const val BASE_URL = "https://cl.dolarapi.com/"

        fun create(): DolarApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(DolarApiService::class.java)
        }
    }
}

