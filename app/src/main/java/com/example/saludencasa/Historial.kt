package com.example.saludencasa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.CitasAdapter
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.TrabajadorService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Citas
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Historial : AppCompatActivity(), CitasAdapter.OnItemClickListener  {
    private lateinit var recyclerViewCitas: RecyclerView
    private lateinit var adapterCitas: CitasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        var idCliente: Int = intent.extras!!.getInt("IDCLIENTE",0)
        var valorIDTrabajador = 0

        recyclerViewCitas = findViewById(R.id.rcvListaCitas)
        recyclerViewCitas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServiceTrabajador = retrofit.create(TrabajadorService::class.java)
        val serviceCitas = retrofit.create(CitasService::class.java)
        val callCitas = serviceCitas.getCita()

        lifecycleScope.launch {
            try {
                val response = apiServiceTrabajador.getTrabajadoresSimple()
                for (dataItem in response) {
                    val wsIdCliente = dataItem.id_cliente
                    if (wsIdCliente == idCliente) {
                        valorIDTrabajador = dataItem.id_trabajador
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            callCitas.enqueue(object : Callback<List<Citas>> {
                override fun onResponse(call: Call<List<Citas>>, response: Response<List<Citas>>) {
                    if (response.isSuccessful) {
                        val citas = response.body()
                        val citasNuevo = mutableListOf<Citas>()

                        if (citas != null) {
                            citas.forEach { itemDato ->
                                if (itemDato.id_cliente == idCliente || itemDato.id_trabajador == valorIDTrabajador) {
                                    citasNuevo.add(itemDato)
                                }
                            }
                        }
                        if (citasNuevo!= null) {
                            /*adapterCitas = CitasAdapter(citasNuevo)
                            recyclerViewCitas.adapter = adapterCitas

                            adapterCitas.setOnItemClickListener(this@Historial)*/


                        }
                    } else {
                        // Manejar error en la respuesta del servicio
                    }
                }

                override fun onFailure(call: Call<List<Citas>>, t: Throwable) {
                    // Manejar error en la llamada al servicio
                }
            })
        }





    }

    override fun onItemClick(citas: Citas) {
        // val intent = ChatDetalle.newIntent(requireContext(), chats.id_chat, chats.id_cliente)
        //startActivity(intent)
    }

}
