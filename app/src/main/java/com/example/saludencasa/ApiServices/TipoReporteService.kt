package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.TipoReporte
import retrofit2.Call
import retrofit2.http.GET


interface TipoReporteService {
    @GET("TipoReporte/") // Reemplazar url
    fun getTipoReporte(): Call<List<TipoReporte>>
}