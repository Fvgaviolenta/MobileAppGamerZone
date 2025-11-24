package com.example.appgamerzone.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de la API de DolarAPI
 * Endpoint: https://cl.dolarapi.com/v1/cotizaciones/usd
 */
data class DolarResponse(
    @SerializedName("moneda")
    val moneda: String = "",

    @SerializedName("nombre")
    val nombre: String = "",

    @SerializedName("compra")
    val compra: Double = 0.0,

    @SerializedName("venta")
    val venta: Double = 0.0,

    @SerializedName("casa")
    val casa: String = "",

    @SerializedName("fecha")
    val fecha: String = ""
)

