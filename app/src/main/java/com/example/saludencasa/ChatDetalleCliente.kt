package com.example.saludencasa

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Adaptador.MensajesAdapter
import com.example.saludencasa.ApiServices.BloquearService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.MensajesService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Mensajes
import com.example.saludencasa.Modelo.MensajeTiempoReal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatDetalleCliente : AppCompatActivity() {
    private lateinit var recyclerViewMensaje: RecyclerView
    private lateinit var adapterMensaje: MensajesAdapter
    var motivo = ""
    var latitud = ""
    var longitud = ""
    var fechaCreacion = ""
    var fechaInicio = ""
    var fechaFin = ""
    var fechaConfirmacion = ""
    var idCita = 0
    var estadoCita = ""
    var arrayfechaInicio : MutableList<String> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    var idChat = 0
    var idCliente = 0
    var idTrabajadorChat = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detalle_cliente)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val btnAtras = findViewById<ImageButton>(R.id.btnAtras)
        btnAtras.setOnClickListener {
            super.onBackPressed()
        }

        idChat = intent.getIntExtra("IDCHAT", 0)
        idCliente = intent.getIntExtra("IDClienteChat", 0)
        idTrabajadorChat = intent.getIntExtra("IDTrabajadorChat", 0)
        val idClienteTrabajador = intent.getIntExtra("IdClienteTrabajador", 0)
        val nombreTrabajadorChat = intent.getStringExtra("NombreTrabajadorChat")
        val foto = intent.getStringExtra("FOTO")
        val imgfoto : ImageView = findViewById(R.id.imgFotoChat)
        if (foto != null && foto != "") {
            imgfoto.load(foto)
        } else {
            imgfoto.setImageResource(R.drawable.foto_predeterminada)
        }

        recyclerViewMensaje = findViewById(R.id.rcvMensajes)
        recyclerViewMensaje.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val txtNombre: TextView = findViewById(R.id.txtNombreCD)
        val txtMensajeNuevo: EditText = findViewById(R.id.txtEscribirMensaje)

        getMensajesAPI(idChat,idCliente,idTrabajadorChat)

        val btnEnviar: ImageButton = findViewById(R.id.btnEnviar)
        btnEnviar.setOnClickListener{
            postMensajesAPI(txtMensajeNuevo.text.toString().trim(),idChat,idCliente,"normal","enviado",idTrabajadorChat)
            txtMensajeNuevo.text.clear()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceCita = retrofit.create(CitasService::class.java)
        val apiServiceBloqueo = retrofit.create(BloquearService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiServiceCita.getCitaSimple()
                for (dataItem in response) {
                    if (idCliente == dataItem.id_cliente && idTrabajadorChat == dataItem.id_trabajador && dataItem.estadoid == "En proceso") {
                        idCita = dataItem.id_cita
                        motivo = dataItem.descripcion_motivo
                        fechaCreacion = dataItem.fecha_creacion
                        fechaFin = dataItem.fecha_finatencion
                        fechaInicio = dataItem.fecha_inicioatencion
                        fechaConfirmacion = dataItem.fecha_confirmacion
                        latitud = dataItem.latitud
                        longitud = dataItem.longitud
                        estadoCita = dataItem.estadoid
                        break
                    }else if(idTrabajadorChat == dataItem.id_trabajador && dataItem.estadoid == "Aceptada"){
                        arrayfechaInicio.add(dataItem.fecha_inicioatencion)
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            try {
                val response = apiServiceBloqueo.getBloqueoSimple()
                for (dataItem in response) {
                    if ((idCliente == dataItem.id_usuario_bloqueador && idClienteTrabajador == dataItem.id_usuario_bloqueado) ||
                        (idCliente == dataItem.id_usuario_bloqueado && idClienteTrabajador == dataItem.id_usuario_bloqueador) ) {
                        val llmensaje : LinearLayout = findViewById(R.id.llMensaje)
                        val txtmensajebloqueado : TextView = findViewById(R.id.txtMensajeBloqueo)
                        llmensaje.visibility = View.GONE
                        txtmensajebloqueado.visibility = View.VISIBLE
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            habilitarInteraccion()
        }

        /*val trabajador = intent.extras
        if (trabajador != null) {
            txtNombre.text = "IDCliente: " + trabajador.getInt("IDCliente").toString() + ", IDTrabajador: " + trabajador.getInt("ID").toString()
        }*/


        txtNombre.text = nombreTrabajadorChat.toString()

        val btnMenu = findViewById<ImageButton>(R.id.btnMenuChat)

        btnMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.menu_chat_cliente)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.op_contacto -> {
                        val intent = Intent(this, PerfilTrabajador_vistaCliente::class.java)
                        intent.putExtra("idCliente",idCliente)
                        intent.putExtra("idTrabajador",idTrabajadorChat)
                        intent.putExtra("idClienteTrabajador",idClienteTrabajador)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()


        }
    }

    companion object {
        fun newIntent(context: Context, idChat: Int, idClienteChat: Int, idTrabajadorChat: Int, nombreTrabajador: String,idClienteTrabajador: Int, foto: String): Intent {
            val intent = Intent(context, ChatDetalleCliente::class.java)
            intent.putExtra("IDCHAT", idChat)
            intent.putExtra("IDClienteChat", idClienteChat)
            intent.putExtra("IDTrabajadorChat", idTrabajadorChat)
            intent.putExtra("NombreTrabajadorChat", nombreTrabajador)
            intent.putExtra("IdClienteTrabajador", idClienteTrabajador)
            intent.putExtra("FOTO", foto)
            return intent
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe
    fun onNewMessagesEvent(event: MensajeTiempoReal) {
        if(idChat==event.idChat){
            val mensaje = Mensajes(1,idChat,event.nombre,OffsetDateTime.now().toString(),event.contenido,true,false,"normal","enviado")
            adapterMensaje.addMensaje(mensaje)
            recyclerViewMensaje.smoothScrollToPosition(adapterMensaje.itemCount-1)
        }
    }

    private fun getMensajesAPI(idChat: Int, idCliente: Int,idTrabajador: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceMensajes = retrofit.create(MensajesService::class.java)
        val callMensajes = serviceMensajes.getMensajes()
        val apiServiceCita = retrofit.create(CitasService::class.java)

        callMensajes.enqueue(object : Callback<List<Mensajes>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Mensajes>>, response: Response<List<Mensajes>>) {
                if (response.isSuccessful) {
                    val mensajes = response.body()
                    val mensajesNuevo = mutableListOf<Mensajes>()

                    if (mensajes != null) {
                        mensajes.forEach { itemDato ->
                            if (itemDato.id_chat == idChat) {
                                mensajesNuevo.add(itemDato)
                            }
                        }
                    }
                    if (mensajesNuevo!= null) {
                        adapterMensaje = MensajesAdapter(mensajesNuevo,idCliente){ id_mensaje, btnOpcionFecha ->
                            val mensajes = mensajesNuevo.find { id_mensaje == it.id_mensaje }
                            if (mensajes!=null){
                                when(btnOpcionFecha){
                                    //aceptar cita
                                    1 -> {
                                        if (estadoCita == "En proceso"){
                                            val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                            val offsetDateTime = OffsetDateTime.parse(fechaInicio, formato)
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
                                                    if (arrayhora.isAfter(hora) && arrayhora.isBefore(hora.plusMinutes(30))){
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
                                                val partes = mensajes.Mensaje.split(",")
                                                val fecha = partes[0].trim()
                                                val hora = partes[1].trim()
                                                val jsonObjectCita = JSONObject()
                                                jsonObjectCita.put("id_cita", idCita)
                                                jsonObjectCita.put("id_trabajador", idTrabajador)
                                                jsonObjectCita.put("id_cliente", idCliente)
                                                jsonObjectCita.put("descripcion_motivo", motivo)
                                                jsonObjectCita.put("fecha_creacion", fechaCreacion)
                                                jsonObjectCita.put("fecha_inicioatencion", obtenerFechaYHoraFormateada(fecha,hora))
                                                jsonObjectCita.put("fecha_finatencion", fechaFin)
                                                jsonObjectCita.put("fecha_confirmacion", fechaConfirmacion)
                                                jsonObjectCita.put("notificacion_trabajador", false)
                                                jsonObjectCita.put("notificacion_cliente", false)
                                                jsonObjectCita.put("notificacion_calificacion", false)
                                                jsonObjectCita.put("latitud", latitud)
                                                jsonObjectCita.put("longitud", longitud)
                                                jsonObjectCita.put("estado", 2)
                                                val jsonStringCita = jsonObjectCita.toString()
                                                val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                                                runBlocking {
                                                    try {
                                                        val responseCliente = apiServiceCita.putCita(idCita,requestBodyCita)
                                                        if (responseCliente.isSuccessful) {
                                                            Toast.makeText(applicationContext,"Se acepto la cita correctamente",Toast.LENGTH_LONG).show()
                                                            val jsonObjectMensaje = JSONObject()
                                                            jsonObjectMensaje.put("id_mensaje", mensajes.id_mensaje)
                                                            jsonObjectMensaje.put("id_cliente", mensajes.id_cliente)
                                                            jsonObjectMensaje.put("fecha_envio", mensajes.fecha_envio)
                                                            jsonObjectMensaje.put("Mensaje", mensajes.Mensaje)
                                                            jsonObjectMensaje.put("tipo_mensaje", mensajes.tipo_mensaje)
                                                            jsonObjectMensaje.put("estado_tipo", "aceptado")
                                                            jsonObjectMensaje.put("id_chat", idChat)
                                                            val jsonStringMensajes = jsonObjectMensaje.toString()
                                                            val serviceMensajes = retrofit.create(MensajesService::class.java)
                                                            val requestBodyMensajes = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringMensajes)

                                                            runBlocking {
                                                                try {
                                                                    //Chat
                                                                    val responseChat = serviceMensajes.putMensaje(mensajes.id_mensaje,requestBodyMensajes)
                                                                    if (responseChat.isSuccessful) {
                                                                        val responseBodyChat = responseChat.body()
                                                                        val responseDataChat = responseBodyChat?.string()
                                                                        getMensajesAPI(idChat,idCliente,idTrabajador)

                                                                    } else {

                                                                    }
                                                                } catch (e: Exception) {
                                                                    // Manejar el error aquí
                                                                    e.printStackTrace()
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }else {
                                                Toast.makeText(applicationContext,"Hay choque horario, modifique la fecha de atencion",Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        else{
                                            Toast.makeText(applicationContext,"No hay solicitud de este cliente",Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    2 -> {
                                        if (estadoCita == "En proceso"){
                                            val jsonObjectMensaje = JSONObject()
                                            jsonObjectMensaje.put("id_mensaje", mensajes.id_mensaje)
                                            jsonObjectMensaje.put("id_cliente", mensajes.id_cliente)
                                            jsonObjectMensaje.put("fecha_envio", mensajes.fecha_envio)
                                            jsonObjectMensaje.put("Mensaje", mensajes.Mensaje)
                                            jsonObjectMensaje.put("tipo_mensaje", mensajes.tipo_mensaje)
                                            jsonObjectMensaje.put("estado_tipo", "rechazado")
                                            jsonObjectMensaje.put("id_chat", idChat)
                                            val jsonStringMensajes = jsonObjectMensaje.toString()
                                            val serviceMensajes = retrofit.create(MensajesService::class.java)
                                            val requestBodyMensajes = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringMensajes)

                                            runBlocking {
                                                try {
                                                    //Chat
                                                     val responseChat = serviceMensajes.putMensaje(mensajes.id_mensaje,requestBodyMensajes)

                                                    if (responseChat.isSuccessful) {
                                                        val responseBodyChat = responseChat.body()
                                                        val responseDataChat = responseBodyChat?.string()
                                                        Toast.makeText(applicationContext,"Se rechazo la hora",Toast.LENGTH_LONG).show()
                                                        getMensajesAPI(idChat,idCliente,idTrabajador)

                                                    } else {

                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(applicationContext,e.message.toString(),Toast.LENGTH_LONG).show()
                                                    e.printStackTrace()
                                                }
                                            }
                                        }else{
                                            Toast.makeText(applicationContext,"No hay solicitud de este cliente",Toast.LENGTH_LONG).show()
                                        }

                                    }
                                }
                            }
                        }
                        recyclerViewMensaje.adapter = adapterMensaje
                        adapterMensaje.ordenarPorFecha()
                        recyclerViewMensaje.smoothScrollToPosition(adapterMensaje.itemCount-1)
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }
            }

            override fun onFailure(call: Call<List<Mensajes>>, t: Throwable) {
                // Manejar error en la llamada al servicio
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postMensajesAPI(mensajeNuevo: String, idChat: Int, idCliente: Int, tipoMensaje: String, estado:String, idTrabajador: Int) {
        val jsonObjectMensaje = JSONObject()
        jsonObjectMensaje.put("id_cliente", idCliente)
        jsonObjectMensaje.put("fecha_envio", OffsetDateTime.now().toString())
        jsonObjectMensaje.put("Mensaje", mensajeNuevo)
        jsonObjectMensaje.put("visto_emisor", true)
        jsonObjectMensaje.put("visto_receptor", false)
        jsonObjectMensaje.put("tipo_mensaje", tipoMensaje)
        jsonObjectMensaje.put("estado_tipo", estado)
        jsonObjectMensaje.put("id_chat", idChat)
        val jsonStringMensajes = jsonObjectMensaje.toString()

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceMensajes = retrofit.create(MensajesService::class.java)
        val requestBodyMensajes = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringMensajes)

        runBlocking {
            try {
                //Chat
                val responseChat = serviceMensajes.postMensaje(requestBodyMensajes)
                if (responseChat.isSuccessful) {
                    val responseBodyChat = responseChat.body()
                    val responseDataChat = responseBodyChat?.string()
                    if (responseDataChat != null) {
                        val gson = Gson()
                        val messageType = object : TypeToken<Mensajes>() {}.type
                        val mensajeResponse: Mensajes = gson.fromJson(responseDataChat, messageType)
                        val idMensaje = mensajeResponse.id_mensaje
                        val mensaje = Mensajes(idMensaje,idChat,idCliente,OffsetDateTime.now().toString(),mensajeNuevo,true,false,tipoMensaje,estado)
                        adapterMensaje.addMensaje(mensaje)
                        recyclerViewMensaje.smoothScrollToPosition(adapterMensaje.itemCount-1)
                    }
                    //getMensajesAPI(idChat,idCliente,idTrabajador)

                } else {

                }
            } catch (e: Exception) {
                // Manejar el error aquí
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFechaYHoraFormateada(fecha: String, hora:String): String {

        val zonaHoraria = ZoneId.systemDefault()
        val offset = zonaHoraria.rules.getOffset(Instant.now())

        val fechaParseada = OffsetDateTime.parse("$fecha$hora:00$offset", DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ssxxx"))

        return fechaParseada.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}