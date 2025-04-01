package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.EnfermedadesCliente
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface EnfermedadesClienteService {
    @GET("EnfermedadesxPaciente/") // Reemplazar url
    fun getEnfermedadesCliente(): Call<List<EnfermedadesCliente>>

    @POST("EnfermedadesxPaciente/")
    suspend fun postEnfermedadesCliente(@Body requestBody: RequestBody): Response<ResponseBody>
}