package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Profesiones
import retrofit2.Call
import retrofit2.http.GET


interface ProfesionesService {
    @GET("TipoDeProfesiones/") // Reemplazar url
    fun getProfesiones(): Call<List<Profesiones>>
}