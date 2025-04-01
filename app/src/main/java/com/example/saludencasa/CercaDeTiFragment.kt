package com.example.saludencasa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.TrabajadoresAdapter
import com.example.saludencasa.ApiServices.TrabajadorService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Trabajador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CercaDeTiFragment : Fragment() {
    private lateinit var trabajadoresAdapter: TrabajadoresAdapter
    private lateinit var trabajadorService: TrabajadorService
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtUsuario: EditText
    var latitude2 = 0.0
    var longitude2 = 0.0

    private var trabajadoresList: List<Trabajador> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_todos_trabajadores, container, false)

        latitude2 = arguments?.getDouble("latitude1") ?: 0.0
        longitude2 = arguments?.getDouble("longitude2") ?: 0.0

        recyclerView = view.findViewById(R.id.rcvTodosLosTrabajadores)
        txtUsuario = view.findViewById(R.id.txtUsuario)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trabajadorService = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrabajadorService::class.java)
        txtUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementar
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                val filteredTrabajadores = if (searchText.isNotEmpty()) {
                    trabajadoresList.filter {
                        it.cliente.contains(searchText, ignoreCase = true)
                    }.toMutableList()
                } else {
                    trabajadoresList.toMutableList()
                }
                trabajadoresAdapter.trabajadores = filteredTrabajadores
                trabajadoresAdapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {
                // No es necesario implementar
            }
        })
        GlobalScope.launch(Dispatchers.Main) {
            //cambiar de "en proceso a aceptado"
            trabajadoresList = withContext(Dispatchers.IO) {
                trabajadorService.getTrabajadoresSimple().filter { it.estadoid == "Aceptado" }
            }
           trabajadoresAdapter = TrabajadoresAdapter(trabajadoresList.toMutableList(), latitude2, longitude2)
            recyclerView.adapter = trabajadoresAdapter
            trabajadoresAdapter.setOnItemClickListener { trabajador ->
                val trabajadorId = trabajador.id_trabajador
                val miActividad = activity as? MainActivity3
                miActividad?.navigateToPerfilTrabajadorVistaCliente(trabajadorId,trabajador.id_cliente)
            }
        }

    }
}