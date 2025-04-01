package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Chats
import com.example.saludencasa.Modelo.Opiniones
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface CalificacionService {
    @GET("Calificacion/") // Reemplazar url
    fun getCalificacion(): Call<List<Opiniones>>

    @POST("Calificacion/")
    suspend fun postCalificacion(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("Calificacion/") // Reemplazar url
    suspend fun getCalificacionSimple(): List<Opiniones>

    @PUT("Calificacion/{id}/")
    suspend fun putCalificacion(@Path("id") id: Int, @Body requestBody: RequestBody): Response<ResponseBody>
}