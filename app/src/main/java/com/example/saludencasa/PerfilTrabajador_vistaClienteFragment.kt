package com.example.saludencasa

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.saludencasa.Constante.urlUbicMedic
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import coil.load
import com.example.saludencasa.ApiServices.BloquearService
import com.example.saludencasa.ApiServices.ChatsService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.MensajesService
import com.example.saludencasa.ApiServices.TrabajadorService
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PerfilTrabajador_vistaClienteFragment : Fragment(R.layout.fragment_perfil_trabajador_vista_cliente) {

    var motivo = ""
    var latitud = ""
    var longitud = ""
    var fechaCreacion = ""
    var fechaInicio = ""
    var fechaFin = ""
    var fechaConfirmacion = ""
    var idCita = 0
    var idBloqueo = 0
    var trabajadorNombre = "Hola"

    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_perfil_trabajador_vista_cliente, container, false)

        progressBar = rootView.findViewById(R.id.progressBar)
        fondoProgress = rootView.findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val miActividad = activity as? MainActivity3
        val idCliente = miActividad?.getMiVariableGlobal()
        val trabajadorId = arguments?.getInt("trabajador_id",0)
        val trabajadorIdCliente = arguments?.getInt("trabajador_id_cliente",0)
        var estadoCita = ""



        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServiceTrabajador = retrofit.create(TrabajadorService::class.java)
        val apiServiceCita = retrofit.create(CitasService::class.java)
        val apiServiceBloqueo = retrofit.create(BloquearService::class.java)

        val btnSolicitar = rootView.findViewById<Button>(R.id.btnSolicitarCita)
        val btnCancelar = rootView.findViewById<Button>(R.id.btnCancelarCita)
        btnCancelar.visibility = View.GONE
        btnCancelar.setOnClickListener {
            val jsonObjectCita = JSONObject()
            jsonObjectCita.put("id_cita", idCita)
            jsonObjectCita.put("id_trabajador", trabajadorId)
            jsonObjectCita.put("id_cliente", idCliente)
            jsonObjectCita.put("descripcion_motivo", motivo)
            jsonObjectCita.put("fecha_creacion", fechaCreacion)
            jsonObjectCita.put("fecha_inicioatencion", fechaInicio)
            jsonObjectCita.put("fecha_finatencion", fechaFin)
            jsonObjectCita.put("fecha_confirmacion", fechaConfirmacion)
            jsonObjectCita.put("notificacion_trabajador", false)
            jsonObjectCita.put("notificacion_cliente", false)
            jsonObjectCita.put("notificacion_calificacion", false)
            jsonObjectCita.put("latitud", latitud)
            jsonObjectCita.put("longitud", longitud)
            jsonObjectCita.put("estado", 3)
            val jsonStringCita = jsonObjectCita.toString()
            val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
            runBlocking {
                try {
                    val responseCliente = apiServiceCita.putCita(idCita,requestBodyCita)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(requireContext(),"Se cancelo la cita",Toast.LENGTH_SHORT).show()
                        btnSolicitar.visibility=View.VISIBLE
                        btnCancelar.visibility=View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnSolicitar.setOnClickListener {
            if (trabajadorId != null) {
                if (idCliente != null) {
                    showDialog(idCliente,trabajadorId,btnSolicitar,btnCancelar)
                }
            }
        }

        val btnmenu : ImageButton = rootView.findViewById(R.id.btnMenuPerfil)
        val popupMenu = PopupMenu(requireContext(), btnmenu)
        popupMenu.inflate(R.menu.menu_perfil_trabajador_vista_cliente)

        val bloquearMenuItem = popupMenu.menu.findItem(R.id.op_bloquear)
        val desbloquearMenuItem = popupMenu.menu.findItem(R.id.op_desbloquear)
        desbloquearMenuItem.isVisible = false
        btnmenu.setOnClickListener {
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.op_reportar -> {
                        val intent = Intent(activity, ReportarPerfil::class.java)
                        intent.putExtra("idCliente",idCliente)
                        intent.putExtra("idClienteTrabajador",trabajadorIdCliente)
                        startActivity(intent)
                        true
                    }
                    R.id.op_bloquear -> {
                        if (trabajadorIdCliente != null && idCliente != null) {
                            showDialogBloquear(
                                idCliente,
                                trabajadorIdCliente,
                                bloquearMenuItem,
                                desbloquearMenuItem
                            )
                        }
                        true
                    }
                    R.id.op_desbloquear -> {
                        if (trabajadorIdCliente != null && idCliente != null) {
                            showDialogDesBloquear(idBloqueo, bloquearMenuItem, desbloquearMenuItem)
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        lifecycleScope.launch {
            //Trabajador
            try {
                val response = apiServiceTrabajador.getTrabajadoresSimple()
                for (dataItem in response) {
                    val wsIdTrabajador = dataItem.id_trabajador
                    if (wsIdTrabajador == trabajadorId) {
                        val txtnombre: TextView = rootView.findViewById<TextView>(R.id.txtvNombrePerfil)
                        txtnombre.setText(dataItem.cliente)
                        trabajadorNombre = dataItem.cliente

                        //val txtcali: TextView = findViewById<TextView>(R.id.txtcalificacion)
                        //txtcali.setText(dataItem.puntuaciones)
                        //val txtaten: TextView = findViewById<TextView>(R.id.txtatencion)
                        //txtaten.setText(dataItem.atenciones)
                        //trabajadorNombre = dataItem.cliente

                        val imgPerfil : ImageView =rootView.findViewById(R.id.imgFotoPerfilFrag)
                        var imagen: String = dataItem.foto
                        if (dataItem.foto != null) {
                            imgPerfil.load(imagen)
                        } else {
                            imgPerfil.setImageResource(R.drawable.foto_predeterminada)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            //cita
            try {
                val response = apiServiceCita.getCitaSimple()
                for (dataItem in response) {
                    if (idCliente == dataItem.id_cliente && trabajadorId == dataItem.id_trabajador && dataItem.estadoid == "En proceso") {
                        idCita = dataItem.id_cita
                        motivo = dataItem.descripcion_motivo
                        fechaCreacion = dataItem.fecha_creacion
                        fechaFin = dataItem.fecha_finatencion
                        fechaInicio = dataItem.fecha_inicioatencion
                        fechaConfirmacion = dataItem.fecha_confirmacion
                        latitud = dataItem.latitud
                        longitud = dataItem.longitud
                        estadoCita = dataItem.estadoid
                        btnSolicitar.visibility=View.GONE
                        btnCancelar.visibility=View.VISIBLE
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            //Bloqueo
            try {
                val response = apiServiceBloqueo.getBloqueoSimple()
                for (dataItem in response) {
                    if (idCliente == dataItem.id_usuario_bloqueador && trabajadorIdCliente == dataItem.id_usuario_bloqueado) {
                        idBloqueo = dataItem.id_bloqueo
                        bloquearMenuItem.isVisible = false
                        desbloquearMenuItem.isVisible = true
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            habilitarInteraccion()
        }

        return rootView
    }
    companion object {
        // Método estático para crear una nueva instancia de DetalleTrabajadorFragment
        fun newInstance(Id: Int, IdCliente: Int): PerfilTrabajador_vistaClienteFragment {
            val fragment = PerfilTrabajador_vistaClienteFragment()
            val args = Bundle()
            args.putInt("trabajador_id", Id)
            args.putInt("trabajador_id_cliente", IdCliente)
            fragment.arguments = args
            return fragment
        }
    }

    private fun obtenerFecha(txtfecha : TextInputLayout) {
        val calendario = Calendar.getInstance()
        val año = calendario[Calendar.YEAR]
        val mes = calendario[Calendar.MONTH]
        val día = calendario[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(requireContext(),
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

        val timePickerDialog = TimePickerDialog(requireContext(), { _: TimePicker, hourOfDay: Int, minute: Int ->
            val amPm: String = if (hourOfDay>12) "PM" else "AM"
            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            txthora.editText?.setText(selectedTime)
        }, hora, minuto, true)

        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFechaYHoraFormateada(fecha: String, hora:String): String {

        val zonaHoraria = ZoneId.systemDefault()
        val offset = zonaHoraria.rules.getOffset(Instant.now())

        val fechaParseada = OffsetDateTime.parse("$fecha$hora:00$offset", DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ssxxx"))

        return fechaParseada.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFechaActual(): String {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFechaHoraActual(): String {
        val fechaActual = OffsetDateTime.now()
        val fechaFormato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        return fechaActual.format(fechaFormato)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialog(idCliente: Int, idTrabajador: Int, btnSolicitar : Button, btnCancelar : Button) {
        val dialogCita = Dialog(requireContext())
        dialogCita.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCita.setCancelable(true)
        dialogCita.setContentView(R.layout.dialog_soliciar_cita)
        dialogCita.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtMotivo : EditText = dialogCita.findViewById(R.id.txtMotivoDialog)
        val btnEnviar : Button = dialogCita.findViewById(R.id.btnEnviarCita)
        val txtfecha :  EditText = dialogCita.findViewById(R.id.txtFechaDialog)
        val txthora :  EditText = dialogCita.findViewById(R.id.txthoraDialog)
        val txt_i_fecha :  TextInputLayout = dialogCita.findViewById(R.id.fecha_text_input_layout)
        val txt_i_motivo : TextInputLayout = dialogCita.findViewById(R.id.motivo_text_input_layout)
        txt_i_fecha.editText?.setText(getFechaActual())
        txt_i_fecha.setEndIconOnClickListener {
            obtenerFecha(txt_i_fecha)
        }
        val txt_i_hora :  TextInputLayout = dialogCita.findViewById(R.id.hora_text_input_layout)
        txt_i_hora.editText?.setText(getHoraActual())
        txt_i_hora.setEndIconOnClickListener {
            obtenerHora(txt_i_hora)
        }

        var idChat: Int = 0
        var estadoChat: String = ""
        var chatCreado : Boolean = false
        var fechaChat : String = ""
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServiceChat = retrofit.create(ChatsService::class.java)
        lifecycleScope.launch {
            try {
                val response = apiServiceChat.getChatSimple()
                for (dataItem in response) {
                    if (idCliente == dataItem.id_cliente && idTrabajador == dataItem.id_trabajador) {
                        idChat = dataItem.id_chat
                        estadoChat = dataItem.estado
                        chatCreado = true
                        break
                    }
                }
                } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
        }
        btnEnviar.setOnClickListener {
            if (txtMotivo.text.toString().trim().isEmpty()){
                txt_i_motivo.error="El campo no puede estar vacio"
                txt_i_motivo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
            }else{
                val jsonObjectChat = JSONObject()
                val jsonObjectMensaje = JSONObject()
                val jsonObjectCita = JSONObject()

                if (chatCreado){
                    jsonObjectChat.put("id_chat", idChat)
                    jsonObjectMensaje.put("id_chat", idChat)
                }
                jsonObjectChat.put("fecha_creacion", getFechaActual())
                jsonObjectChat.put("id_cliente", idCliente)
                jsonObjectChat.put("id_trabajador", idTrabajador)
                jsonObjectChat.put("ultimensaje", "Me gustaria tener una cita medica con usted")
                jsonObjectChat.put("estado", "proceso")

                jsonObjectMensaje.put("id_cliente", idCliente)
                jsonObjectMensaje.put("fecha_envio", getFechaActual())
                jsonObjectMensaje.put("Mensaje", "Me gustaria tener una cita medica con usted")
                jsonObjectMensaje.put("tipo_mensaje", "normal")
                jsonObjectMensaje.put("visto_emisor", true)
                jsonObjectMensaje.put("visto_receptor", false)
                jsonObjectMensaje.put("estado_tipo", "enviado")

                motivo = txtMotivo.text.toString()
                fechaCreacion = getFechaHoraActual()
                fechaInicio = obtenerFechaYHoraFormateada(txtfecha.text.toString(),txthora.text.toString())
                fechaFin = getFechaHoraActual()
                fechaConfirmacion = getFechaHoraActual()

                jsonObjectCita.put("id_trabajador", idTrabajador)
                jsonObjectCita.put("id_cliente", idCliente)
                jsonObjectCita.put("descripcion_motivo", motivo)
                jsonObjectCita.put("fecha_creacion", fechaCreacion)
                jsonObjectCita.put("fecha_inicioatencion", fechaInicio)
                jsonObjectCita.put("fecha_finatencion", fechaFin)
                jsonObjectCita.put("fecha_confirmacion", fechaConfirmacion)
                jsonObjectCita.put("notificacion_trabajador", false)
                jsonObjectCita.put("notificacion_cliente", false)
                jsonObjectCita.put("notificacion_calificacion", false)
                jsonObjectCita.put("latitud", "-79.466468")
                jsonObjectCita.put("longitud", "-79.466468")
                jsonObjectCita.put("estado", 1)

                val jsonStringChat = jsonObjectChat.toString()
                val requestBodyChat = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringChat)

                runBlocking {
                    try {
                        //Chat
                        val responseChat = if(chatCreado){
                            apiServiceChat.putChat(idChat,requestBodyChat)
                        }else{
                            apiServiceChat.postChat(requestBodyChat)
                        }
                        if (responseChat.isSuccessful) {
                            val responseBodyChat = responseChat.body()
                            val responseDataChat = responseBodyChat?.string()
                            if (responseBodyChat != null) {
                                //Mensaje
                                val jsonObject = JSONObject(responseDataChat)
                                val nuevaId = jsonObject.getInt("id_chat")
                                jsonObjectMensaje.put("id_chat", nuevaId)
                                val jsonStringMensaje = jsonObjectMensaje.toString()
                                val apiServiceMensaje = retrofit.create(MensajesService::class.java)
                                val requestBodyMensaje = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringMensaje)
                                val responseMensaje = apiServiceMensaje.postMensaje(requestBodyMensaje)
                                if (responseMensaje.isSuccessful) {
                                    val responseBodyMensaje = responseMensaje.body()
                                    if (responseBodyMensaje != null) {
                                        //Cita
                                        val jsonStringCita = jsonObjectCita.toString()
                                        val apiServiceCita = retrofit.create(CitasService::class.java)
                                        val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                                        val responseCita = apiServiceCita.postCita(requestBodyCita)
                                        if (responseCita.isSuccessful) {
                                            val responseBodyCita = responseCita.body()
                                            val responseDataCita = responseBodyCita?.string()
                                            if (responseDataCita != null) {
                                                //Mensaje
                                                val jsonObject = JSONObject(responseDataCita)
                                                idCita = jsonObject.getInt("id_cita")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            //Mensaje
                        }
                    } catch (e: Exception) {
                        // Manejar el error aquí
                        e.printStackTrace()
                    }
                }

                Toast.makeText(requireContext(),"Se realizo la cita correctamente",Toast.LENGTH_LONG).show()
                btnSolicitar.visibility = View.GONE
                btnCancelar.visibility = View.VISIBLE
                dialogCita.dismiss()
            }

        }
        dialogCita.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogBloquear(idClienteMio: Int, idClienteEl: Int, bloquearMenuItem: MenuItem, desbloquearMenuItem: MenuItem) {
        val dialogBloquear = Dialog(requireContext())
        dialogBloquear.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogBloquear.setCancelable(true)
        dialogBloquear.setContentView(R.layout.dialog_bloquear_perfil)
        dialogBloquear.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogBloquear.findViewById(R.id.txtvNombrePerfil)
        txtEncabezado.setText("¿Quieres Bloquear a ${trabajadorNombre}?")
        val btnEnviar : Button = dialogBloquear.findViewById(R.id.btnBloquear)

        btnEnviar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiServiceBloqueo = retrofit.create(BloquearService::class.java)
            try{
                val jsonObjectBloqueo = JSONObject()
                jsonObjectBloqueo.put("id_usuario_bloqueador", idClienteMio)
                jsonObjectBloqueo.put("id_usuario_bloqueado", idClienteEl)
                jsonObjectBloqueo.put("fecha_bloqueo", OffsetDateTime.now())
                val jsonStringBloqueo = jsonObjectBloqueo.toString()
                val requestBodyBloqueo = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringBloqueo)
                runBlocking {
                    try {
                        //Chat
                        val responseBloqueo = apiServiceBloqueo.postBloqueo(requestBodyBloqueo)
                        if (responseBloqueo.isSuccessful) {
                            val responseBodyBloqueo = responseBloqueo.body()
                            val responseDataBloqueo = responseBodyBloqueo?.string()
                            if (responseBodyBloqueo!=null){
                                val jsonObject = JSONObject(responseDataBloqueo)
                                idBloqueo = jsonObject.getInt("id_bloqueo")
                            }
                        } else {

                        }
                    } catch (e: Exception) {
                        // Manejar el error aquí
                        e.printStackTrace()
                    }
                }
                dialogBloquear.dismiss()
                bloquearMenuItem.isVisible = false
                desbloquearMenuItem.isVisible = true
                Toast.makeText(requireContext(),"Se bloqueo este perfil", Toast.LENGTH_LONG).show()
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }
        dialogBloquear.show()
    }

    private fun showDialogDesBloquear(idBloqueo: Int, bloquearMenuItem: MenuItem, desbloquearMenuItem: MenuItem) {
        val dialogBloquear = Dialog(requireContext())
        dialogBloquear.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogBloquear.setCancelable(true)
        dialogBloquear.setContentView(R.layout.dialog_bloquear_perfil)
        dialogBloquear.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogBloquear.findViewById(R.id.txtvNombrePerfil)
        val imgMensaje : ImageView = dialogBloquear.findViewById(R.id.imgMensaje)
        val txtMensaje : TextView = dialogBloquear.findViewById(R.id.txtAvisoMensaje)
        val txtNotificacion : TextView = dialogBloquear.findViewById(R.id.txtAvisoNotificacion)
        val txtBloquear : TextView = dialogBloquear.findViewById(R.id.txtAvisoBloquear)
        val btnEnviar : Button = dialogBloquear.findViewById(R.id.btnBloquear)
        txtEncabezado.setText("¿Quieres desbloquear a ${trabajadorNombre}?")
        imgMensaje.setImageResource(R.drawable.desbloquear_mensaje)
        txtMensaje.setText("Podra enviarte mensajes y encontrar tu perfil.")
        txtNotificacion.setText("No se notificará a esta persona que la desbloqueaste.")
        txtBloquear.setText("Puedes bloquear a esta persona cuando quieras en las opciones")
        btnEnviar.setText("Desbloquear")
        btnEnviar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiServiceBloqueo = retrofit.create(BloquearService::class.java)
            lifecycleScope.launch{
                try{
                    apiServiceBloqueo.eliminarBloqueo(idBloqueo)
                    dialogBloquear.dismiss()
                    bloquearMenuItem.isVisible = true
                    desbloquearMenuItem.isVisible = false
                    Toast.makeText(requireContext(),"Se desbloqueo este perfil", Toast.LENGTH_LONG).show()
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }

        }
        dialogBloquear.show()
    }

    private fun deshabilitarInteraccion() {
        progressBar.visibility = View.VISIBLE
        fondoProgress.visibility = View.VISIBLE
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun habilitarInteraccion() {
        progressBar.visibility = View.GONE
        fondoProgress.visibility = View.GONE
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}