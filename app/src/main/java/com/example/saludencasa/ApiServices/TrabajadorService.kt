package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Trabajador
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface TrabajadorService {
    @GET("Trabajador/") // Reemplazar url
    suspend fun getTrabajadoresSimple(): List<Trabajador>

    @GET("Trabajador/") // Reemplazar url
    fun getTrabajadores(): Call<List<Trabajador>>

    @POST("Trabajador/")
    suspend fun postTrabajador(@Body requestBody: RequestBody): Response<ResponseBody>
}