package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Bloqueo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BloquearService {
    @GET("Bloqueos/") // Reemplazar url
    fun getBloqueo(): Call<List<Bloqueo>>

    @POST("Bloqueos/")
    suspend fun postBloqueo(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("Bloqueos/") // Reemplazar url
    suspend fun getBloqueoSimple(): List<Bloqueo>

    @DELETE("Bloqueos/{id}/")
    suspend fun eliminarBloqueo(@Path("id") id: Int): Response<Void>
}