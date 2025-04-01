package com.example.saludencasa

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.ChatsAdapter
import com.example.saludencasa.Adaptador.NotificacionesAdapter
import com.example.saludencasa.ApiServices.ChatsService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Chats
import com.example.saludencasa.Modelo.Citas
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class Notificaciones : AppCompatActivity() {

    private lateinit var rcvNotificaciones: RecyclerView
    private lateinit var adapterNotificacion: NotificacionesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val idCliente = intent.getIntExtra("IDCLIENTE", 0)
        val idTrabajador = intent.getIntExtra("IDTRABAJADOR", 0)
        var arrayfechaInicio : MutableList<String> = mutableListOf()


        rcvNotificaciones = findViewById(R.id.rcvListaNotificaciones)
        rcvNotificaciones.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceNotificaciones = retrofit.create(CitasService::class.java)
        val callNotificaciones = serviceNotificaciones.getCita()
        val apiServiceCita = retrofit.create(CitasService::class.java)

        callNotificaciones.enqueue(object : Callback<List<Citas>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Citas>>, response: Response<List<Citas>>) {
                if (response.isSuccessful) {
                    val notificaciones = response.body()
                    val notificacionesNuevo = mutableListOf<Citas>()

                    if (notificaciones != null) {
                        notificaciones.forEach { itemDato ->
                            if ((itemDato.id_trabajador == idTrabajador) ||
                                    itemDato.id_cliente == idCliente && (itemDato.estadoid == "Aceptada" || itemDato.estadoid == "Finalizado")) {
                                notificacionesNuevo.add(itemDato)
                            }
                        }
                    }
                    if (notificacionesNuevo!= null) {
                        if (idCliente!=null){
                            adapterNotificacion = NotificacionesAdapter(idCliente,notificacionesNuevo){ idNotificacion, btnOpciones ->
                                val notificacion = notificacionesNuevo.find { it.id_cita == idNotificacion }
                                if (notificacion!=null){
                                    when(btnOpciones){
                                        //aceptar cita
                                        1 -> {
                                            val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                            val offsetDateTime = OffsetDateTime.parse(notificacion.fecha_inicioatencion, formato)
                                            val fecha = offsetDateTime.toLocalDate()
                                            val hora = offsetDateTime.toLocalTime()
                                            val minutos = offsetDateTime.minute
                                            var citaSinChoqueHorario = false
                                            if (arrayfechaInicio.isEmpty()){
                                                citaSinChoqueHorario = true
                                            }
                                            for (dato in arrayfechaInicio) {
                                                val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                                val offsetDateTime = OffsetDateTime.parse(dato, formato)
                                                val arrayfecha = offsetDateTime.toLocalDate()
                                                val arrayhora = offsetDateTime.toLocalTime()
                                                if(fecha == arrayfecha){
                                                    if ((hora.isAfter(arrayhora) && hora.isBefore(arrayhora.plusMinutes(30)))
                                                        || hora.plusMinutes(30).isAfter(arrayhora) && hora.plusMinutes(30).isBefore(arrayhora.plusMinutes(30))){
                                                        citaSinChoqueHorario = false
                                                        break
                                                    }else{
                                                        citaSinChoqueHorario = true
                                                    }

                                                }else{
                                                    citaSinChoqueHorario = true
                                                }
                                            }
                                            if(citaSinChoqueHorario){
                                                val jsonObjectCita = JSONObject()
                                                jsonObjectCita.put("id_cita", notificacion.id_cita)
                                                jsonObjectCita.put("id_trabajador", notificacion.id_trabajador)
                                                jsonObjectCita.put("id_cliente", notificacion.id_cliente)
                                                jsonObjectCita.put("descripcion_motivo", notificacion.descripcion_motivo)
                                                jsonObjectCita.put("fecha_creacion", notificacion.fecha_creacion)
                                                jsonObjectCita.put("fecha_inicioatencion", notificacion.fecha_inicioatencion)
                                                jsonObjectCita.put("fecha_finatencion", notificacion.fecha_finatencion)
                                                jsonObjectCita.put("fecha_confirmacion", OffsetDateTime.now())
                                                jsonObjectCita.put("notificacion_trabajador", true)
                                                jsonObjectCita.put("notificacion_cliente", notificacion.notificacion_cliente)
                                                jsonObjectCita.put("notificacion_calificacion", notificacion.notificacion_calificacion)
                                                jsonObjectCita.put("latitud", notificacion.latitud)
                                                jsonObjectCita.put("longitud", notificacion.longitud)
                                                jsonObjectCita.put("estado", 2)
                                                val jsonStringCita = jsonObjectCita.toString()
                                                val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                                                runBlocking {
                                                    try {
                                                        val responseCita = apiServiceCita.putCita(notificacion.id_cita,requestBodyCita)
                                                        if (responseCita.isSuccessful) {
                                                            Toast.makeText(applicationContext,"Se acepto la cita correctamente",Toast.LENGTH_LONG).show()
                                                            arrayfechaInicio.add(notificacion.fecha_inicioatencion)
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }else {
                                                Toast.makeText(applicationContext,"Hay choque horario, modifique la fecha de atencion",Toast.LENGTH_LONG).show()
                                                adapterNotificacion.itemSelecionado = 4
                                            }
                                        }
                                        //cancelar cita
                                        2 -> {
                                            val jsonObjectCita = JSONObject()
                                            jsonObjectCita.put("id_cita", notificacion.id_cita)
                                            jsonObjectCita.put("id_trabajador", notificacion.id_trabajador)
                                            jsonObjectCita.put("id_cliente", notificacion.id_cliente)
                                            jsonObjectCita.put("descripcion_motivo", notificacion.descripcion_motivo)
                                            jsonObjectCita.put("fecha_creacion", notificacion.fecha_creacion)
                                            jsonObjectCita.put("fecha_inicioatencion", notificacion.fecha_inicioatencion)
                                            jsonObjectCita.put("fecha_finatencion", notificacion.fecha_finatencion)
                                            jsonObjectCita.put("fecha_confirmacion", OffsetDateTime.now())
                                            jsonObjectCita.put("notificacion_trabajador", true)
                                            jsonObjectCita.put("notificacion_cliente", notificacion.notificacion_cliente)
                                            jsonObjectCita.put("notificacion_calificacion", notificacion.notificacion_calificacion)
                                            jsonObjectCita.put("latitud", notificacion.latitud)
                                            jsonObjectCita.put("longitud", notificacion.longitud)
                                            jsonObjectCita.put("estado", 3)
                                            val jsonStringCita = jsonObjectCita.toString()
                                            val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                                            runBlocking {
                                                try {
                                                    val responseCliente = apiServiceCita.putCita(notificacion.id_cita,requestBodyCita)
                                                    if (responseCliente.isSuccessful) {
                                                        Toast.makeText(applicationContext,"Se cancelo la cita",Toast.LENGTH_LONG).show()
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                        //ver perfil
                                        3 -> {
                                            if(idCliente != notificacion.id_cliente){
                                                val intent = Intent(applicationContext, PerfilCliente_vistaTrabajador::class.java)
                                                intent.putExtra("idCliente",notificacion.id_cliente_trabajador)
                                                intent.putExtra("idTrabajador",notificacion.id_trabajador)
                                                intent.putExtra("idClienteTrabajador",notificacion.id_cliente)
                                                startActivity(intent)
                                            }else{
                                                val intent = Intent(applicationContext, PerfilTrabajador_vistaCliente::class.java)
                                                intent.putExtra("idCliente",notificacion.id_cliente)
                                                intent.putExtra("idTrabajador",notificacion.id_trabajador)
                                                intent.putExtra("idClienteTrabajador",notificacion.id_cliente_trabajador)
                                                startActivity(intent)
                                            }
                                        }
                                    }

                                }
                            }
                            rcvNotificaciones.adapter = adapterNotificacion
                            adapterNotificacion.ordenarPorFecha()
                        }
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }

            }

            override fun onFailure(call: Call<List<Citas>>, t: Throwable) {
                // Manejar error en la llamada al servicio
                habilitarInteraccion()
            }
        })

        lifecycleScope.launch {
            try {
                val response = apiServiceCita.getCitaSimple()
                for (dataItem in response) {
                    if(idTrabajador == dataItem.id_trabajador && dataItem.estadoid == "Aceptada"){
                        arrayfechaInicio.add(dataItem.fecha_inicioatencion)
                    }
                    if(dataItem.id_cliente == idCliente && dataItem.estadoid == "Aceptada" && !dataItem.notificacion_cliente){
                        val jsonObjectCita = JSONObject()
                        jsonObjectCita.put("id_cita", dataItem.id_cita)
                        jsonObjectCita.put("id_trabajador", dataItem.id_trabajador)
                        jsonObjectCita.put("id_cliente", dataItem.id_cliente)
                        jsonObjectCita.put("descripcion_motivo", dataItem.descripcion_motivo)
                        jsonObjectCita.put("fecha_creacion", dataItem.fecha_creacion)
                        jsonObjectCita.put("fecha_inicioatencion", dataItem.fecha_inicioatencion)
                        jsonObjectCita.put("fecha_finatencion", dataItem.fecha_finatencion)
                        jsonObjectCita.put("fecha_confirmacion", dataItem.fecha_confirmacion)
                        jsonObjectCita.put("notificacion_trabajador", dataItem.notificacion_trabajador)
                        jsonObjectCita.put("notificacion_cliente", true)
                        jsonObjectCita.put("notificacion_calificacion", dataItem.notificacion_calificacion)
                        jsonObjectCita.put("latitud", dataItem.latitud)
                        jsonObjectCita.put("longitud", dataItem.longitud)
                        jsonObjectCita.put("estado", dataItem.estado)
                        val jsonStringCita = jsonObjectCita.toString()
                        val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                        runBlocking {
                            try {
                                apiServiceCita.putCita(dataItem.id_cita,requestBodyCita)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }else if (dataItem.id_trabajador == idTrabajador && !dataItem.notificacion_trabajador){
                        val jsonObjectCita = JSONObject()
                        jsonObjectCita.put("id_cita", dataItem.id_cita)
                        jsonObjectCita.put("id_trabajador", dataItem.id_trabajador)
                        jsonObjectCita.put("id_cliente", dataItem.id_cliente)
                        jsonObjectCita.put("descripcion_motivo", dataItem.descripcion_motivo)
                        jsonObjectCita.put("fecha_creacion", dataItem.fecha_creacion)
                        jsonObjectCita.put("fecha_inicioatencion", dataItem.fecha_inicioatencion)
                        jsonObjectCita.put("fecha_finatencion", dataItem.fecha_finatencion)
                        jsonObjectCita.put("fecha_confirmacion", dataItem.fecha_confirmacion)
                        jsonObjectCita.put("notificacion_trabajador", true)
                        jsonObjectCita.put("notificacion_cliente", dataItem.notificacion_cliente)
                        jsonObjectCita.put("notificacion_calificacion", dataItem.notificacion_calificacion)
                        jsonObjectCita.put("latitud", dataItem.latitud)
                        jsonObjectCita.put("longitud", dataItem.longitud)
                        jsonObjectCita.put("estado", dataItem.estado)
                        val jsonStringCita = jsonObjectCita.toString()
                        val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                        runBlocking {
                            try {
                                apiServiceCita.putCita(dataItem.id_cita,requestBodyCita)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            habilitarInteraccion()
        }

    }

    private fun deshabilitarInteraccion() {
        progressBar.visibility = View.VISIBLE
        fondoProgress.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun habilitarInteraccion() {
        progressBar.visibility = View.GONE
        fondoProgress.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}