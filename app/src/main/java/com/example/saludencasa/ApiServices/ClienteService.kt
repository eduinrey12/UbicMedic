package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Clientes
import com.example.saludencasa.Modelo.ClientesPUT
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ClienteService {
    @GET("Cliente/") // Reemplazar url
    suspend fun getCliente(): List<Clientes>

    @POST("Cliente/")
    suspend fun postCliente(@Body requestBody: RequestBody): Response<ResponseBody>

    @PUT("Cliente/{id}/")
    suspend fun putCliente(@Path("id") id: Int,@Body requestBody: RequestBody): Response<ResponseBody>
}