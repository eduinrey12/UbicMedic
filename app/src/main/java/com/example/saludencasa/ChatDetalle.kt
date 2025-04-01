package com.example.saludencasa

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
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
import com.google.android.material.textfield.TextInputLayout
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ChatDetalle : AppCompatActivity() {

    private lateinit var recyclerViewMensaje: RecyclerView
    private lateinit var adapterMensaje: MensajesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View
    var idChat = 0
    var idClienteTrabajador = 0
    var idTrabajadorChat = 0
    var idClienteChat = 0
    var nombreClienteChat = "hola"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detalle)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val btnAtras = findViewById<ImageButton>(R.id.btnAtras)
        btnAtras.setOnClickListener {
            super.onBackPressed()
        }
        idChat = intent.getIntExtra("IDCHAT", 0)
        idClienteTrabajador = intent.getIntExtra("IdClienteTrabajador", 0)//idtrabajdorCliente
        idTrabajadorChat = intent.getIntExtra("IDTrabajadorChat", 0)
        idClienteChat = intent.getIntExtra("IDClienteChat", 0)
        nombreClienteChat = intent.getStringExtra("NombreClienteChat").toString()

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

        getMensajesAPI(idChat, idClienteTrabajador)

        val btnEnviar: ImageButton = findViewById(R.id.btnEnviar)
        btnEnviar.setOnClickListener{
            postMensajesAPI(txtMensajeNuevo.text.toString().trim(),idChat,idClienteTrabajador,"normal","enviado")
            txtMensajeNuevo.text.clear()
        }

        var motivo = ""
        var latitud = ""
        var longitud = ""
        var fechaCreacion = ""
        var fechaInicio = ""
        var fechaFin = ""
        var idCita = 0
        var estadoCita = ""
        var arrayfechaInicio : MutableList<String> = mutableListOf()

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
                    if (idClienteChat == dataItem.id_cliente && idTrabajadorChat == dataItem.id_trabajador && dataItem.estadoid == "En proceso") {
                        idCita = dataItem.id_cita
                        motivo = dataItem.descripcion_motivo
                        fechaCreacion = dataItem.fecha_creacion
                        fechaFin = dataItem.fecha_finatencion
                        fechaInicio = dataItem.fecha_inicioatencion
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
                    if ((idClienteTrabajador == dataItem.id_usuario_bloqueador && idClienteChat == dataItem.id_usuario_bloqueado) ||
                        (idClienteTrabajador == dataItem.id_usuario_bloqueado && idClienteChat == dataItem.id_usuario_bloqueador) ) {
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

        txtNombre.text = nombreClienteChat.toString()

        val btnMenu = findViewById<ImageButton>(R.id.btnMenuChat)

        btnMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.menu_chat)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.op_contacto -> {
                        val intent = Intent(this, PerfilCliente_vistaTrabajador::class.java)
                        intent.putExtra("idCliente",idClienteTrabajador)
                        intent.putExtra("idTrabajador",idTrabajadorChat)
                        intent.putExtra("idClienteTrabajador",idClienteChat)
                        startActivity(intent)
                        true
                    }
                    R.id.op_fecha -> {
                        showDialog(idClienteTrabajador,idChat)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()


        }

    }

    companion object {
        fun newIntent(context: Context, idChat: Int, idClienteChat: Int, idTrabajadorChat: Int ,nombreCliente: String, idClienteTrabajador: Int, foto: String): Intent {
            val intent = Intent(context, ChatDetalle::class.java)
            intent.putExtra("IDCHAT", idChat)
            intent.putExtra("IDClienteChat", idClienteChat)
            intent.putExtra("IDTrabajadorChat", idTrabajadorChat)
            intent.putExtra("NombreClienteChat", nombreCliente)
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private fun getMensajesAPI(idChat: Int, idCliente: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceMensajes = retrofit.create(MensajesService::class.java)
        val callMensajes = serviceMensajes.getMensajes()

        callMensajes.enqueue(object : Callback<List<Mensajes>> {
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
                        adapterMensaje = MensajesAdapter(mensajesNuevo,idCliente){ id_chat, btnOpcionFecha ->
                            val mensajes = mensajesNuevo.find { idChat == it.id_chat }
                            if (mensajes!=null){
                                when(btnOpcionFecha){
                                    //aceptar cita
                                    1 -> {
                                        Toast.makeText(applicationContext,"Aceptar",Toast.LENGTH_SHORT).show()
                                    }
                                    2 -> {
                                        Toast.makeText(applicationContext,"Rechazar",Toast.LENGTH_SHORT).show()
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
    private fun postMensajesAPI(mensajeNuevo: String, idChat: Int, idCliente: Int, tipoMensaje: String, estado:String) {
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
                    //getMensajesAPI(idChat, idCliente)

                } else {

                }
            } catch (e: Exception) {
                // Manejar el error aquí
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialog(idCliente: Int, idChat: Int) {
        val dialogCita = Dialog(this)
        dialogCita.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCita.setCancelable(true)
        dialogCita.setContentView(R.layout.dialog_soliciar_cita)
        dialogCita.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogCita.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val txtnombre : TextView = dialogCita.findViewById(R.id.txtvNombreDialogCita)
        val txtMotivo : TextInputLayout = dialogCita.findViewById(R.id.motivo_text_input_layout)
        val btnEnviar : Button = dialogCita.findViewById(R.id.btnEnviarCita)
        val txtfecha :  EditText = dialogCita.findViewById(R.id.txtFechaDialog)
        val txthora :  EditText = dialogCita.findViewById(R.id.txthoraDialog)
        txtMotivo.visibility = View.GONE
        txtnombre.setText("Fecha de atencion")
        val txt_i_fecha : TextInputLayout = dialogCita.findViewById(R.id.fecha_text_input_layout)
        txt_i_fecha.editText?.setText(getFechaActual2())
        txt_i_fecha.setEndIconOnClickListener {
            obtenerFecha(txt_i_fecha)
        }
        val txt_i_hora : TextInputLayout = dialogCita.findViewById(R.id.hora_text_input_layout)
        txt_i_hora.editText?.setText(getHoraActual())
        txt_i_hora.setEndIconOnClickListener {
            obtenerHora(txt_i_hora)
        }
        btnEnviar.setOnClickListener {
            postMensajesAPI(txtfecha.text.toString().trim() + ", " + txthora.text.toString().trim(),idChat,idCliente,"fecha","enviado")
            dialogCita.dismiss()
        }
        dialogCita.show()
    }

    private fun obtenerFecha(txtfecha : TextInputLayout) {
        val calendario = Calendar.getInstance()
        val año = calendario[Calendar.YEAR]
        val mes = calendario[Calendar.MONTH]
        val día = calendario[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { view, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d-%02d-%02d", year, (month + 1), dayOfMonth)
                txtfecha.editText?.setText(selectedDate)
            }, año, mes, día
        )
        datePickerDialog.show()
    }

    private fun obtenerHora(txthora : TextInputLayout) {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _: TimePicker, hourOfDay: Int, minute: Int ->
            val amPm: String = if (hourOfDay>12) "PM" else "AM"
            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            txthora.editText?.setText(selectedTime)
        }, hora, minuto, true)

        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFechaActual2(): String {
        val fechaActual = LocalDate.now()
        val fechaFormato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return fechaActual.format(fechaFormato)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHoraActual(): String {
        val horaActual = LocalTime.now()
        val horaFormato = DateTimeFormatter.ofPattern("HH:mm")
        return horaActual.format(horaFormato)
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