package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Citas
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface CitasService {
    @GET("Cita/") // Reemplazar url
    fun getCita(): Call<List<Citas>>

    @POST("Cita/")
    suspend fun postCita(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("Cita/") // Reemplazar url
    suspend fun getCitaSimple(): List<Citas>

    @PUT("Cita/{id}/") // Reemplazar url
    suspend fun putCita(@Path("id") id: Int, @Body requestBody: RequestBody): Response<ResponseBody>
}