package com.example.saludencasa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.ProfesionesAdapter
import com.example.saludencasa.Adaptador.TrabajadorAdapter
import com.example.saludencasa.ApiServices.BloquearService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.ProfesionesService
import com.example.saludencasa.ApiServices.TrabajadorService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Profesiones
import com.example.saludencasa.Modelo.Trabajador
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment(R.layout.fragment_home), TrabajadorAdapter.OnItemClickListener {

    private lateinit var recyclerViewTrabajadorTop: RecyclerView
    private lateinit var recyclerViewTrabajador: RecyclerView
    private lateinit var recyclerViewProfesiones: RecyclerView
    private lateinit var profesionesAdapter: ProfesionesAdapter
    private lateinit var trabajadorAdapter: TrabajadorAdapter
    private lateinit var trabajadorAdapterTop: TrabajadorAdapter
    private var showAllProfesiones = false
    private lateinit var trabajadoresOriginal: List<Trabajador>
    private lateinit var profesionesOriginal: List<Profesiones>
    //12 de agisto
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var txtUbic: TextView
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var latitude2: Double = 0.0
    var longitude2: Double = 0.0
    //fin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        fondoProgress = view.findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        //14 de agosto
        val btnCerca: Button = view.findViewById(R.id.btnCerca)
        //hasta aca

        //12 de agosto
        txtUbic = view.findViewById(R.id.txtUbic)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
        //fin
        trabajadoresOriginal = mutableListOf()
        profesionesOriginal = mutableListOf()

        trabajadoresOriginal = mutableListOf()
        profesionesOriginal = mutableListOf()
        val txtVerTodo: TextView = view.findViewById(R.id.textView24)
        val txtTodosTrabajadores: TextView = view.findViewById(R.id.textView242)
        val txtTodosTrabajadoresTop: TextView = view.findViewById(R.id.textView2422)
        val miActividad = activity as? MainActivity3
        val idCliente = miActividad?.getMiVariableGlobal()
        val idTrabajador = miActividad?.getMiVariableGlobalTrabaja()
        recyclerViewTrabajador = view.findViewById(R.id.rcvListaRecomendados)
        recyclerViewTrabajador.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewTrabajadorTop = view.findViewById(R.id.rcvListaTop)
        recyclerViewTrabajadorTop.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewProfesiones = view.findViewById(R.id.rcvListaProfesiones)
        recyclerViewProfesiones.layoutManager = FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP)
        val btnnotificacion : FrameLayout = view.findViewById(R.id.btnNotificacion)
        btnnotificacion.setOnClickListener {
            val intent = Intent(context,Notificaciones::class.java)
            intent.putExtra("IDCLIENTE",idCliente)
            intent.putExtra("IDTRABAJADOR",idTrabajador)
            startActivity(intent)
            val crdcontador : CardView = view.findViewById(R.id.crdNotificacionHome)
            crdcontador.visibility = View.GONE
        }

        val retrofit = Retrofit.Builder().baseUrl(urlUbicMedic).addConverterFactory(GsonConverterFactory.create()).build()
        val serviceTrabajador = retrofit.create(TrabajadorService::class.java)
        val callTrabajador = serviceTrabajador.getTrabajadores()

        val serviceProfesiones = retrofit.create(ProfesionesService::class.java)
        val callProfesiones = serviceProfesiones.getProfesiones()

        txtVerTodo.setOnClickListener {
            showAllProfesiones = !showAllProfesiones
            profesionesAdapter.setShowAllProfesiones(showAllProfesiones)
            if (showAllProfesiones) {
                txtVerTodo.text = "ver menos"
            } else {
                txtVerTodo.text = "ver todo"
            }
        }

        val apiServiceCita = retrofit.create(CitasService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiServiceCita.getCitaSimple()
                val crdcontador : CardView = view.findViewById(R.id.crdNotificacionHome)
                val txtcontador : TextView = view.findViewById(R.id.contador_notificaciones)
                var contador = 0
                for (dataItem in response) {
                    if ((idCliente == dataItem.id_cliente && dataItem.estadoid == "Aceptada" && !dataItem.notificacion_cliente)
                        || (idTrabajador == dataItem.id_trabajador && !dataItem.notificacion_trabajador)) {
                        contador += 1
                    }
                }
                if (contador > 0){
                    crdcontador.visibility = View.VISIBLE
                    txtcontador.setText(contador.toString())
                }

            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
            //Profesiones
            try {
                callProfesiones.enqueue(object : Callback<List<Profesiones>> {
                    override fun onResponse(call: Call<List<Profesiones>>, response: Response<List<Profesiones>>) {
                        if (response.isSuccessful) {
                            val profesiones = response.body()
                            if (profesiones != null) {
                                profesionesAdapter = ProfesionesAdapter(profesiones) { idProfesion ->
                                    val profesion = profesiones.find { it.id_profesiones == idProfesion }
                                    if (profesion != null) {
                                        val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                        intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, profesion.id_profesiones)
                                        intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, profesion.descripcion)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(requireContext(), "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                recyclerViewProfesiones.adapter = profesionesAdapter
                            }
                        } else {
                        }

                    }
                    override fun onFailure(call: Call<List<Profesiones>>, t: Throwable) {
                    }
                })
            }catch (e:Exception){
                e.printStackTrace()
            }
            //Trabajadores
            try{
                callTrabajador.enqueue(object : Callback<List<Trabajador>> {
                    @SuppressLint("MissingPermission")
                    override fun onResponse(call: Call<List<Trabajador>>, response: Response<List<Trabajador>>) {
                        if (response.isSuccessful) {
                            val trabajadores = response.body()
                            val trabajadorNuevo = mutableListOf<Trabajador>()
                            val trabajadorNuevoTop = mutableListOf<Trabajador>()
                            if (trabajadores != null) {
                                trabajadores.forEach { itemDato ->
                                    if (itemDato.estadoid == "Aceptado" && idTrabajador !=itemDato.id_trabajador) {
                                        trabajadorNuevo.add(itemDato)
                                        trabajadorNuevoTop.add(itemDato)
                                    }
                                }
                            }
                            if (trabajadorNuevo.isNotEmpty()) {

                                trabajadorAdapter = TrabajadorAdapter(trabajadorNuevo,latitude,longitude )
                                trabajadorAdapterTop = TrabajadorAdapter(trabajadorNuevoTop,latitude,longitude )
                                recyclerViewTrabajador.adapter = trabajadorAdapter
                                recyclerViewTrabajadorTop.adapter = trabajadorAdapterTop
                                trabajadorAdapter.ordenarCalificacion()
                                trabajadorAdapterTop.ordenarAtenciones()
                                trabajadorAdapter.setOnItemClickListener(this@HomeFragment)
                                trabajadorAdapterTop.setOnItemClickListener(this@HomeFragment)
                            }
                        }
                        habilitarInteraccion()
                    }
                    override fun onFailure(call: Call<List<Trabajador>>, t: Throwable) {
                    }
                })

            }catch (e: Exception){
                e.printStackTrace()
            }

        }

        txtTodosTrabajadores.setOnClickListener {
            onVerMasClick()
        }
        txtTodosTrabajadoresTop.setOnClickListener {
            onVerMasClick()
        }
        btnCerca.setOnClickListener {
            onVerMasClick2()
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
            if (location != null) {
                updateLocationText(location)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(
                    requireContext(),
                    "La aplicación necesita permisos de ubicación para obtener la ubicación en tiempo real",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }


    override fun onItemClick(trabajador: Trabajador) {
        if (trabajador.id_trabajador == -1) {
            Toast.makeText(requireContext(), "Has seleccionado ver más", Toast.LENGTH_SHORT).show()
        } else {
            val miActividad = activity as? MainActivity3
            val idCliente = miActividad?.getMiVariableGlobal()
            val intent = Intent(activity, PerfilTrabajador_vistaCliente::class.java)
            intent.putExtra("idCliente",idCliente)
            intent.putExtra("idTrabajador",trabajador.id_trabajador)
            intent.putExtra("idClienteTrabajador",trabajador.id_cliente)
            intent.putExtra("latitude",latitude.toString())
            intent.putExtra("longitude",longitude.toString())
            startActivity(intent)
        }
    }
    override fun onVerMasClick() {
        val fragment = TodosTrabajadoresFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", latitude)
        bundle.putDouble("longitude", longitude)
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.flHome, fragment).addToBackStack(null).commit()
    }
    override fun onVerMasClick2() {
        val cercaDeTiFragment = CercaDeTiFragment()
        val bundle2 = Bundle()
        bundle2.putDouble("latitude1", latitude2)
        bundle2.putDouble("longitude2", longitude2)
        cercaDeTiFragment.arguments = bundle2
        Log.e("latitude2",  latitude2.toString())
        Log.e("latitude",  latitude.toString())
        parentFragmentManager.beginTransaction().replace(R.id.flHome, cercaDeTiFragment).addToBackStack(null).commit()
    }

    override fun getLocation(): Pair<Double, Double> {
        return Pair(latitude, longitude)
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View
    private fun deshabilitarInteraccion() {
        progressBar.visibility = View.VISIBLE
        fondoProgress.visibility = View.VISIBLE
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    private fun habilitarInteraccion() {
        progressBar.visibility = View.GONE
        fondoProgress.visibility = View.GONE
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    private fun updateLocationText(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        latitude2 = location.latitude
        longitude2 = location.longitude

        val ubicText = "Latitud: $latitude\nLongitud: $longitude"
        txtUbic.text = ubicText
    }
}
