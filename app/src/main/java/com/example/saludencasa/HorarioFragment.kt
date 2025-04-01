package com.example.saludencasa

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.GridView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.AgendaAdapter
import com.example.saludencasa.Adaptador.CitasAdapter
import com.example.saludencasa.ApiServices.BloquearService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.TrabajadorService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Agenda
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
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class HorarioFragment : Fragment(R.layout.fragment_horario), CitasAdapter.OnItemClickListener {
    private lateinit var recyclerViewCitas: RecyclerView
    private lateinit var adapterCitas: CitasAdapter
    private lateinit var recyclerViewAgenda: RecyclerView
    private lateinit var adapterAgenda: AgendaAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressBar)
        fondoProgress = view.findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()
        recyclerViewAgenda = view.findViewById(R.id.recyclerView)
        recyclerViewAgenda.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCitas = view.findViewById(R.id.rcvListaCitas)
        recyclerViewCitas.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val fechaActual = LocalDate.now()
        var year = fechaActual.year
        var month = ((fechaActual.monthValue) - 1 )
        var yearA = fechaActual.year
        var monthA = ((fechaActual.monthValue) - 1 )
        val dia = fechaActual.dayOfMonth
        val symbols = DateFormatSymbols(Locale("es", "ES"))
        val nombresMeses = symbols.months
        val txtfechaActual : TextView = view.findViewById(R.id.txtvfecha)
        txtfechaActual.setText(nombresMeses[month] + " " + year.toString())
        val btnMesSiguiente : ImageButton = view.findViewById(R.id.btnMesDespues)
        val btnMesAnterior : ImageButton = view.findViewById(R.id.btnMesAntes)
        btnMesSiguiente.setOnClickListener {
            if (month > 10){
                month = 0
                year += 1
            }else{
                month += 1
            }
            val verificar =if (month == monthA && year == yearA) true else false
            val diasMes = getDaysInMonth(year, month)
            val ListDias = generateDaysData(year, month, diasMes)
            cargarDiasAgenda(ListDias,dia,month,year,verificar)
            txtfechaActual.setText(nombresMeses[month] + " " + year.toString())
        }
        btnMesAnterior.setOnClickListener {
            if (month < 1){
                month = 11
                year -= 1
            }else{
                month -= 1
            }
            val verificar =if (month == monthA && year == yearA) true else false
            val diasMes = getDaysInMonth(year, month)
            val ListDias = generateDaysData(year, month, diasMes)
            cargarDiasAgenda(ListDias,dia,month,year,verificar)
            txtfechaActual.setText(nombresMeses[month] + " " + year.toString())
        }
        val diasMes = getDaysInMonth(yearA, monthA)
        val ListDias = generateDaysData(yearA, monthA, diasMes)
        cargarDiasAgenda(ListDias,dia,monthA,yearA, true)
        cargarAgenda(fechaActual.toString())
    }

    private fun cargarDiasAgenda(ListDias: List<Agenda>, dia:Int, month: Int, year: Int, verificar: Boolean){

        adapterAgenda = AgendaAdapter(verificar,dia,ListDias) { dia ->
            val AgendaDia = ListDias.find { it.dayOfMonth == dia }
            if (AgendaDia != null) {
                val fechaselecionada = year.toString() + "-" + String.format("%02d-%02d", month+1, dia)
                deshabilitarInteraccion()
                cargarAgenda(fechaselecionada)
            }
        }

        recyclerViewAgenda.adapter = adapterAgenda
        if(verificar){
            recyclerViewAgenda.post {
                val layoutManager = recyclerViewAgenda.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(dia-1,0)
            }
        }

    }
    private fun cargarAgenda(fechaSelecionada: String) {
        val miActividad = activity as? MainActivity3
        val valorVariableGlobal = miActividad?.getMiVariableGlobal()
        var valorIDTrabajador : Int = 0
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceTrabajador = retrofit.create(TrabajadorService::class.java)
        lifecycleScope.launch {
            try {
                val response = apiServiceTrabajador.getTrabajadoresSimple()
                for (dataItem in response) {
                    val wsIdCliente = dataItem.id_cliente
                    if (wsIdCliente == valorVariableGlobal) {
                        valorIDTrabajador = dataItem.id_trabajador
                        break
                    }
                }
                val serviceCitas = retrofit.create(CitasService::class.java)
                val callCitas = serviceCitas.getCita()
                callCitas.enqueue(object : Callback<List<Citas>> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(call: Call<List<Citas>>, response: Response<List<Citas>>) {
                        if (response.isSuccessful) {
                            val citas = response.body()
                            val citasNuevo = mutableListOf<Citas>()
                            if (citas != null) {
                                citas.forEach { itemDato ->
                                    val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                    val fechaIni = OffsetDateTime.parse(itemDato.fecha_inicioatencion, formato)
                                    val fechaSolo = fechaIni.toLocalDate().toString()
                                    if ((itemDato.id_cliente == valorVariableGlobal || itemDato.id_trabajador == valorIDTrabajador)
                                        && (itemDato.estado == 2 || itemDato.estado == 4) && fechaSolo == fechaSelecionada) {
                                        citasNuevo.add(itemDato)
                                    }
                                }
                            }
                            if (citasNuevo!= null) {
                                adapterCitas = CitasAdapter(citasNuevo){ idCita, it ->
                                    val cita = citasNuevo.find { it.id_cita == idCita }
                                    if (cita!=null){
                                        if(cita.id_trabajador == valorIDTrabajador){
                                            val popupMenu = PopupMenu(requireContext(), it)
                                            popupMenu.inflate(R.menu.menu_cita)
                                            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                                                when (menuItem.itemId) {
                                                    R.id.op_contacto -> {
                                                        true
                                                    }
                                                    R.id.op_mapa -> {
                                                        val latitud = cita.latitud.toDouble()
                                                        val longitud = cita.longitud.toDouble()
                                                        val nombre = cita.cliente
                                                        val foto = cita.fotoC
                                                        val args = Bundle()
                                                        args.putDouble("latitud", latitud)
                                                        args.putDouble("longitud", longitud)
                                                        args.putString("nombre", nombre)
                                                        args.putString("foto", foto)
                                                        val rutaClientesFragment = RutaClientes()
                                                        rutaClientesFragment.arguments = args
                                                        val fragmentManager = requireActivity().supportFragmentManager
                                                        fragmentManager.beginTransaction()
                                                            .replace(this@HorarioFragment.id, rutaClientesFragment)
                                                            .addToBackStack(null)
                                                            .commit()
                                                        //Toast.makeText(requireContext(),"latitud: "+cita.latitud + ", longitud: " + cita.longitud, Toast.LENGTH_LONG).show()
                                                        true
                                                    }
                                                    R.id.op_finalizar -> {
                                                        val jsonObjectCita = JSONObject()
                                                        jsonObjectCita.put("id_cita", cita.id_cita)
                                                        jsonObjectCita.put("id_trabajador", cita.id_trabajador)
                                                        jsonObjectCita.put("id_cliente", cita.id_cliente)
                                                        jsonObjectCita.put("descripcion_motivo", cita.descripcion_motivo)
                                                        jsonObjectCita.put("fecha_creacion", cita.fecha_creacion)
                                                        jsonObjectCita.put("fecha_inicioatencion", cita.fecha_inicioatencion)
                                                        jsonObjectCita.put("fecha_finatencion", cita.fecha_finatencion)
                                                        jsonObjectCita.put("fecha_confirmacion", cita.fecha_confirmacion)
                                                        jsonObjectCita.put("notificacion_trabajador", cita.notificacion_trabajador)
                                                        jsonObjectCita.put("notificacion_cliente", cita.notificacion_cliente)
                                                        jsonObjectCita.put("notificacion_calificacion", cita.notificacion_calificacion)
                                                        jsonObjectCita.put("latitud", cita.latitud)
                                                        jsonObjectCita.put("longitud", cita.longitud)
                                                        jsonObjectCita.put("estado", 4)
                                                        showDialogFinalizar(cita.id_cita,cita.cliente,jsonObjectCita)
                                                        true
                                                    }
                                                    else -> false
                                                }
                                            }
                                            popupMenu.show()
                                        }

                                    }
                                }
                                recyclerViewCitas.adapter = adapterCitas

                                adapterCitas.setOnItemClickListener(this@HorarioFragment)
                                adapterCitas.ordenarPorFecha()

                                habilitarInteraccion()
                            }
                        } else {
                            // Manejar error en la respuesta del servicio
                        }
                    }

                    override fun onFailure(call: Call<List<Citas>>, t: Throwable) {
                        // Manejar error en la llamada al servicio
                    }
                })
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }

        }
    }

    private fun showDialogFinalizar(idCita: Int, clienteNombre: String ,jsonObjectCita: JSONObject) {
        val dialogFinalizar = Dialog(requireContext())
        dialogFinalizar.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFinalizar.setCancelable(true)
        dialogFinalizar.setContentView(R.layout.dialog_cita_finalizar)
        dialogFinalizar.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogFinalizar.findViewById(R.id.txtvNombrePerfil)
        txtEncabezado.setText("¿Quieres finalizar la cita de ${clienteNombre}?")
        val btnFinalizar : Button = dialogFinalizar.findViewById(R.id.btnFinalizarSi)
        val btnCancelar : Button = dialogFinalizar.findViewById(R.id.btnFinalizarNo)

        btnCancelar.setOnClickListener {
            dialogFinalizar.dismiss()
        }

        btnFinalizar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiServiceCita = retrofit.create(CitasService::class.java)
            try{
                val jsonStringCita = jsonObjectCita.toString()
                val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                runBlocking {
                    try {
                        //Chat
                        val responseBloqueo = apiServiceCita.putCita(idCita,requestBodyCita)
                        if (responseBloqueo.isSuccessful) {
                            Toast.makeText(requireContext(),"Cita finalizada", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                dialogFinalizar.dismiss()

            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }
        dialogFinalizar.show()
    }


    override fun onItemClick(citas: Citas) {
       // val intent = ChatDetalle.newIntent(requireContext(), chats.id_chat, chats.id_cliente)
        //startActivity(intent)
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun generateDaysData(year: Int, month: Int, daysInMonth: Int): List<Agenda> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        val daysData = mutableListOf<Agenda>()

        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (day in 1..daysInMonth) {
            val dayOfWeek = dateFormat.format(calendar.time)
            daysData.add(Agenda(day, dayOfWeek))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return daysData
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
