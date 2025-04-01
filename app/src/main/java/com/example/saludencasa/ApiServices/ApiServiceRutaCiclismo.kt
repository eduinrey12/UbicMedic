package com.example.saludencasa.ApiServices

import com.example.saludencasa.Modelo.RouteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServiceRutaCiclismo {
    @GET("/v2/directions/cycling-regular")
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ): Response<RouteResponse>
}