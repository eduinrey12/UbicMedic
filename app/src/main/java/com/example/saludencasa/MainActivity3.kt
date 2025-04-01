package com.example.saludencasa

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.Constante.urlUbicMedic
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class MainActivity3 : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var miVariableGlobal: Int = 0
    private var miVariableGlobalTrabaja: Int = 0

    //agregando
    fun navigateToPerfilTrabajadorVistaCliente(trabajadorId: Int, trabajadorIdCliente: Int) {
        val perfilTrabajadorFragment = PerfilTrabajador_vistaClienteFragment.newInstance(trabajadorId,trabajadorIdCliente)

        supportFragmentManager.beginTransaction()
            .replace(R.id.flHome, perfilTrabajadorFragment)
            .addToBackStack(null) // Esto permite retroceder al fragmento anterior
            .commit()
    }
    //hasta aca
    fun getMiVariableGlobal(): Int {
        return miVariableGlobal
    }

    fun getMiVariableGlobalTrabaja(): Int {
        return miVariableGlobalTrabaja
    }

    fun setMiVariableGlobal(valor: Int) {
        miVariableGlobal = valor
    }

    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectedItemId: Int = R.id.nav_Home
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val bdLogin = intent.extras
        val idCliente = bdLogin?.getInt("IDCLIENTE")
        if (idCliente != null) {
            miVariableGlobal = idCliente
        }
        val idTrabajador = bdLogin?.getInt("IDTrabajador")
        if (idTrabajador != null) {
            miVariableGlobalTrabaja = idTrabajador
        }

        bottomNavigationView = findViewById(R.id.bnvMenu)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        bottomNavigationView.menu.findItem(R.id.nav_Home).setIcon(R.drawable.hogar64n)

        // Carga el fragmento inicial
        val initialFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.flHome, initialFragment).commit()

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceCita = retrofit.create(CitasService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiServiceCita.getCitaSimple()
                for (dataItem in response) {
                    val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    val fechaFin = OffsetDateTime.parse(dataItem.fecha_finatencion, formato)
                    val fechaCalificar = fechaFin.plusDays(1)

                    if ((idCliente == dataItem.id_cliente && dataItem.estadoid == "Aceptada" &&
                        !dataItem.notificacion_calificacion  && fechaCalificar < OffsetDateTime.now()) ||
                        (!dataItem.notificacion_calificacion && idCliente == dataItem.id_cliente && dataItem.estadoid == "Finalizado")) {
                        val intent = Intent(applicationContext,CalificarTrabajador::class.java)
                        intent.putExtra("idTrabajador",dataItem.id_trabajador)
                        intent.putExtra("idCliente",dataItem.id_cliente)
                        intent.putExtra("nombreTrabajador",dataItem.trabajador)
                        val foto = if (dataItem == null ) "" else dataItem.fotoT
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
                        jsonObjectCita.put("notificacion_cliente",  dataItem.notificacion_cliente)
                        jsonObjectCita.put("notificacion_calificacion", true)
                        jsonObjectCita.put("latitud", dataItem.latitud)
                        jsonObjectCita.put("longitud", dataItem.longitud)
                        jsonObjectCita.put("estado", 4)
                        val jsonStringCita = jsonObjectCita.toString()
                        val requestBodyCita = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCita)
                        runBlocking {
                            try {
                                apiServiceCita.putCita(dataItem.id_cita,requestBodyCita)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        intent.putExtra("fotoT",foto)
                        startActivity(intent)
                        /*idCita = dataItem.id_cita
                        motivo = dataItem.descripcion_motivo
                        fechaCreacion = dataItem.fecha_creacion
                        fechaFin = dataItem.fecha_finatencion
                        fechaInicio = dataItem.fecha_inicioatencion
                        fechaConfirmacion = dataItem.fecha_confirmacion
                        latitud = dataItem.latitud
                        longitud = dataItem.longitud
                        estadoCita = dataItem.estadoid*/
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
        }

    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_Home -> {
                val previousMenuItem = bottomNavigationView.menu.findItem(selectedItemId)
                item.setIcon(R.drawable.hogar64n)
                if(selectedItemId == R.id.nav_Horario){
                    previousMenuItem.setIcon(R.drawable.calendario64b)
                }else if(selectedItemId == R.id.nav_mundo){
                    previousMenuItem.setIcon(R.drawable.ubicacion64b)
                }else if(selectedItemId == R.id.nav_chat){
                    previousMenuItem.setIcon(R.drawable.charla64b)
                }else if(selectedItemId == R.id.nav_Perfil){
                    previousMenuItem.setIcon(R.drawable.perfil64b)
                }
                selectedItemId = item.itemId

                val fragment1 = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment1).commit()
            }
            R.id.nav_Horario -> {
                val previousMenuItem = bottomNavigationView.menu.findItem(selectedItemId)
                item.setIcon(R.drawable.calendario64n)
                if(selectedItemId == R.id.nav_Home){
                    previousMenuItem.setIcon(R.drawable.hogar64b)
                }else if(selectedItemId == R.id.nav_mundo){
                    previousMenuItem.setIcon(R.drawable.ubicacion64b)
                }else if(selectedItemId == R.id.nav_chat){
                    previousMenuItem.setIcon(R.drawable.charla64b)
                }else if(selectedItemId == R.id.nav_Perfil){
                    previousMenuItem.setIcon(R.drawable.perfil64b)
                }
                selectedItemId = item.itemId

                val fragment2 = HorarioFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment2).commit()
            }
            R.id.nav_chat -> {
                val previousMenuItem = bottomNavigationView.menu.findItem(selectedItemId)
                item.setIcon(R.drawable.charla64n)
                if(selectedItemId == R.id.nav_Horario){
                    previousMenuItem.setIcon(R.drawable.calendario64b)
                }else if(selectedItemId == R.id.nav_mundo){
                    previousMenuItem.setIcon(R.drawable.ubicacion64b)
                }else if(selectedItemId == R.id.nav_Home){
                    previousMenuItem.setIcon(R.drawable.hogar64b)
                }else if(selectedItemId == R.id.nav_Perfil){
                    previousMenuItem.setIcon(R.drawable.perfil64b)
                }
                selectedItemId = item.itemId

                val fragment3 = ChatFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment3).commit()
            }
            R.id.nav_mundo -> {
                val previousMenuItem = bottomNavigationView.menu.findItem(selectedItemId)
                item.setIcon(R.drawable.ubicacion64n)
                if(selectedItemId == R.id.nav_Horario){
                    previousMenuItem.setIcon(R.drawable.calendario64b)
                }else if(selectedItemId == R.id.nav_Home){
                    previousMenuItem.setIcon(R.drawable.hogar64b)
                }else if(selectedItemId == R.id.nav_chat){
                    previousMenuItem.setIcon(R.drawable.charla64b)
                }else if(selectedItemId == R.id.nav_Perfil){
                    previousMenuItem.setIcon(R.drawable.perfil64b)
                }
                selectedItemId = item.itemId

                val fragment4 = MapaFragment()
                supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment4).commit()
            }
            R.id.nav_Perfil -> {
                val previousMenuItem = bottomNavigationView.menu.findItem(selectedItemId)
                item.setIcon(R.drawable.perfil64n)
                if(selectedItemId == R.id.nav_Horario){
                    previousMenuItem.setIcon(R.drawable.calendario64b)
                }else if(selectedItemId == R.id.nav_mundo){
                    previousMenuItem.setIcon(R.drawable.ubicacion64b)
                }else if(selectedItemId == R.id.nav_chat){
                    previousMenuItem.setIcon(R.drawable.charla64b)
                }else if(selectedItemId == R.id.nav_Home){
                    previousMenuItem.setIcon(R.drawable.hogar64b)
                }
                selectedItemId = item.itemId

                val fragment5 = PerfilTrabajadorFragment()
                val fragment6 = PerfilClienteFragment()
                val bdLogin = intent.extras
                val TipoLogin = bdLogin?.getString("TIPOLOGIN").toString()
                val idLogin = bdLogin?.getInt("IDLOGIN",0)
                val bdEnviarLogin = Bundle()
                bdEnviarLogin.putString("TIPO", TipoLogin)
                bdEnviarLogin.putInt("ID", getMiVariableGlobal())
                bdEnviarLogin.putInt("IDTrabajador", getMiVariableGlobalTrabaja())
                if (idLogin != null) {
                    bdEnviarLogin.putInt("IDLOGIN", idLogin)
                }
                if (getMiVariableGlobalTrabaja()!=0){
                    fragment5.arguments = bdEnviarLogin
                    supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment5).commit()
                }else{
                    fragment6.arguments = bdEnviarLogin
                    supportFragmentManager.beginTransaction().replace(R.id.flHome, fragment6).commit()
                }

            }
        }
        return true
    }
}