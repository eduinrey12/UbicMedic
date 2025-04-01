package com.example.saludencasa

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Adaptador.EnfermedadesClienteAdapter
import com.example.saludencasa.ApiServices.BloquearService
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.EnfermedadesClienteService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.EnfermedadesCliente
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
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
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Locale

class PerfilCliente_vistaTrabajador : AppCompatActivity() {

    private lateinit var adapterEnfermedadesCliente: EnfermedadesClienteAdapter
    private lateinit var rcvEnfermedadesCliente: RecyclerView
    private lateinit var rcvCronicas: RecyclerView
    private lateinit var rcvCongenitas: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View
    var idBloqueo = 0
    var clienteNombre = "Hola"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_cliente_vista_trabajador)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        rcvEnfermedadesCliente = findViewById(R.id.rcvAlergias)
        rcvCronicas = findViewById(R.id.rcvCronicas)
        rcvCongenitas = findViewById(R.id.rcvCongenitas)
        rcvEnfermedadesCliente.layoutManager = FlexboxLayoutManager(this,
            FlexDirection.ROW,
            FlexWrap.WRAP)
        rcvCronicas.layoutManager = FlexboxLayoutManager(this,
            FlexDirection.ROW,
            FlexWrap.WRAP)
        rcvCongenitas.layoutManager = FlexboxLayoutManager(this,
            FlexDirection.ROW,
            FlexWrap.WRAP)

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceCliente = retrofit.create(ClienteService::class.java)
        val apiServiceBloqueo = retrofit.create(BloquearService::class.java)

        val idCliente = intent.getIntExtra("idCliente", 0)
        val idTrabajador = intent.getIntExtra("idTrabajador", 0)
        val trabajadorIdCliente = intent.getIntExtra("idClienteTrabajador", 0)

        val btnmenu : ImageButton = findViewById(R.id.btnMenuPerfil)
        val popupMenu = PopupMenu(this, btnmenu)
        popupMenu.inflate(R.menu.menu_perfil_trabajador_vista_cliente)

        val bloquearMenuItem = popupMenu.menu.findItem(R.id.op_bloquear)
        val desbloquearMenuItem = popupMenu.menu.findItem(R.id.op_desbloquear)
        desbloquearMenuItem.isVisible = false
        btnmenu.setOnClickListener {
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.op_reportar -> {
                        val intent = Intent(this, ReportarPerfil::class.java)
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
            try {
                val response = apiServiceCliente.getCliente()
                for (dataItem in response) {
                    val wsIdCliente = dataItem.id_cliente
                    if (wsIdCliente == idCliente) {
                        clienteNombre = dataItem.nombre + " " + dataItem.apellido
                        val txtnombre : TextView =findViewById<TextView>(R.id.txtvNombrePerfil)
                        txtnombre.setText(dataItem.nombre + " " + dataItem.apellido)
                        val txtUbicacion : TextView =findViewById<TextView>(R.id.txtvUbicacion)
                        txtUbicacion.setText(dataItem.paisdescrip.toString() + ", " + dataItem.ciudaddescrip.toString() )
                        val txtSangre : TextView =findViewById<TextView>(R.id.txtvSangre)
                        txtSangre.setText(dataItem.sangredescrip.toString())
                        val imgGenero : ImageView =findViewById(R.id.imgGenero)
                        if (dataItem.sexo==1){
                            imgGenero.setImageDrawable(resources.getDrawable(R.drawable.simbolo_masculino, null))
                        }else{
                            imgGenero.setImageDrawable(resources.getDrawable(R.drawable.simbolo_femenino, null))
                        }
                        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val fechaNacimiento = formato.parse(dataItem.fecha_nacimiento)
                        val calNac = Calendar.getInstance()
                        calNac.time = fechaNacimiento
                        val calActual = Calendar.getInstance()
                        var edad = calActual.get(Calendar.YEAR) - calNac.get(Calendar.YEAR)
                        if (calActual.get(Calendar.DAY_OF_YEAR) < calNac.get(Calendar.DAY_OF_YEAR)) {
                            edad--
                        }
                        val txtEdad : TextView =findViewById<TextView>(R.id.txtvEdad)
                        txtEdad.setText(edad.toString())

                        val imgPerfil : ImageView =findViewById(R.id.imgFotoPerfilFrag)
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
            //Bloqueo
            try {
                val response = apiServiceBloqueo.getBloqueoSimple()
                for (dataItem in response) {
                    if (idCliente == dataItem.id_usuario_bloqueador && trabajadorIdCliente == dataItem.id_usuario_bloqueado) {
                        idBloqueo = dataItem.id_bloqueo
                        bloquearMenuItem.isVisible = false
                        desbloquearMenuItem.isVisible = true
                        break
                    }else if(idCliente == dataItem.id_usuario_bloqueado && trabajadorIdCliente == dataItem.id_usuario_bloqueador){
                        val img404 : ImageView = findViewById(R.id.img404)
                        val txtv404 : TextView = findViewById(R.id.txtv404)
                        val txtv404mensaje : TextView = findViewById(R.id.txtv404Mensaje)
                        val fondoError : View = findViewById(R.id.fondoProgress)
                        val scvPerfil : ScrollView = findViewById(R.id.scvPerfilContenido)
                        img404.visibility = View.VISIBLE
                        txtv404.visibility = View.VISIBLE
                        txtv404mensaje.visibility = View.VISIBLE
                        fondoError.visibility = View.VISIBLE
                        scvPerfil.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            habilitarInteraccion()
        }

        val serviceEnfermedadesCliente = retrofit.create(EnfermedadesClienteService::class.java)
        val callEnfermedadesCliente = serviceEnfermedadesCliente.getEnfermedadesCliente()

        callEnfermedadesCliente.enqueue(object : Callback<List<EnfermedadesCliente>> {

            override fun onResponse(call: Call<List<EnfermedadesCliente>>, response: Response<List<EnfermedadesCliente>>) {
                if (response.isSuccessful) {
                    val todasenfermedadesClienteResponse = response.body()
                    val enfermedadesClienteResponse = mutableListOf<EnfermedadesCliente>()
                    val alergias = mutableListOf<EnfermedadesCliente>()
                    val cronicas = mutableListOf<EnfermedadesCliente>()
                    val congenitas = mutableListOf<EnfermedadesCliente>()

                    if (todasenfermedadesClienteResponse != null){
                        todasenfermedadesClienteResponse.forEach{ itemDato ->
                            if (itemDato.id_cliente == idCliente){
                                enfermedadesClienteResponse.add(itemDato)
                            }
                        }
                    }

                    if (enfermedadesClienteResponse != null) {
                        enfermedadesClienteResponse.forEach { itemDato ->
                            if (itemDato.idclasienfermedad == "1") {
                                alergias.add(itemDato)
                            }else if(itemDato.idclasienfermedad == "2"){
                                cronicas.add(itemDato)
                            }else{
                                congenitas.add(itemDato)
                            }
                        }
                    }

                    if (alergias != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(alergias) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = alergias.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvEnfermedadesCliente.adapter = adapterEnfermedadesCliente
                    }

                    if (cronicas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(cronicas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = cronicas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvCronicas.adapter = adapterEnfermedadesCliente
                    }

                    if (congenitas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(congenitas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = congenitas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvCongenitas.adapter = adapterEnfermedadesCliente
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }
            }

            override fun onFailure(call: Call<List<EnfermedadesCliente>>, t: Throwable) {
                // Manejar error en la llamada al servicio
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogBloquear(idClienteMio: Int, idClienteEl: Int, bloquearMenuItem: MenuItem, desbloquearMenuItem: MenuItem) {
        val dialogBloquear = Dialog(this)
        dialogBloquear.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogBloquear.setCancelable(true)
        dialogBloquear.setContentView(R.layout.dialog_bloquear_perfil)
        dialogBloquear.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogBloquear.findViewById(R.id.txtvNombrePerfil)
        txtEncabezado.setText("¿Quieres Bloquear a ${clienteNombre}?")
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
                Toast.makeText(this,"Se bloqueo este perfil", Toast.LENGTH_LONG).show()
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }
        dialogBloquear.show()
    }

    private fun showDialogDesBloquear(idBloqueo: Int, bloquearMenuItem: MenuItem, desbloquearMenuItem: MenuItem) {
        val dialogBloquear = Dialog(this)
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
        txtEncabezado.setText("¿Quieres desbloquear a ${clienteNombre}?")
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
                    Toast.makeText(applicationContext,"Se desbloqueo este perfil", Toast.LENGTH_LONG).show()
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