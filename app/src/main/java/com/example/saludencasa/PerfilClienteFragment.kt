package com.example.saludencasa

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Adaptador.EnfermedadesClienteAdapter
import com.example.saludencasa.Adaptador.ProfesionesAdapter
import com.example.saludencasa.Adaptador.TrabajadorAdapter
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.EnfermedadesClienteService
import com.example.saludencasa.ApiServices.ProfesionesService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.EnfermedadesCliente
import com.example.saludencasa.Modelo.Profesiones
import com.example.saludencasa.Modelo.Trabajador
import com.example.saludencasa.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class PerfilClienteFragment : Fragment(R.layout.fragment_perfil_cliente) {

    private lateinit var adapterEnfermedadesCliente: EnfermedadesClienteAdapter
    private lateinit var rcvEnfermedadesCliente: RecyclerView
    private lateinit var rcvCronicas: RecyclerView
    private lateinit var rcvCongenitas: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        fondoProgress = view.findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        rcvEnfermedadesCliente = view.findViewById(R.id.rcvAlergias)
        rcvCronicas = view.findViewById(R.id.rcvCronicas)
        rcvCongenitas = view.findViewById(R.id.rcvCongenitas)
        rcvEnfermedadesCliente.layoutManager = FlexboxLayoutManager(requireContext(),FlexDirection.ROW,FlexWrap.WRAP)
        rcvCronicas.layoutManager = FlexboxLayoutManager(requireContext(),FlexDirection.ROW,FlexWrap.WRAP)
        rcvCongenitas.layoutManager = FlexboxLayoutManager(requireContext(),FlexDirection.ROW,FlexWrap.WRAP)

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceCliente = retrofit.create(ClienteService::class.java)
        val miActividad = activity as? MainActivity3
        val idCliente = miActividad?.getMiVariableGlobal()
        val btnMenu = view.findViewById<ImageButton>(R.id.btnMenuPerfil)
        val btnEditar = view.findViewById<Button>(R.id.btnEditPerfil)
        val datoTIPO = arguments?.getString("TIPO")
        val datoIDLogin = arguments?.getInt("IDLOGIN")

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
                                Toast.makeText(requireContext(),"Enfermdad: " + enfermedadesCliente.descripcion,Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(requireContext(), "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvEnfermedadesCliente.adapter = adapterEnfermedadesCliente
                    }

                    if (cronicas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(cronicas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = cronicas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(requireContext(),"Enfermdad: " + enfermedadesCliente.descripcion,Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(requireContext(), "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvCronicas.adapter = adapterEnfermedadesCliente
                    }

                    if (congenitas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(congenitas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = congenitas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(requireContext(),"Enfermdad: " + enfermedadesCliente.descripcion,Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(requireContext(), "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
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

        lifecycleScope.launch {
            try {
                val response = apiServiceCliente.getCliente()
                for (dataItem in response) {
                    val wsIdCliente = dataItem.id_cliente
                    if (wsIdCliente == idCliente) {
                        val txtnombre : TextView =view.findViewById<TextView>(R.id.txtvNombrePerfil)
                        txtnombre.setText(dataItem.nombre + " " + dataItem.apellido)
                        val txtUbicacion : TextView =view.findViewById<TextView>(R.id.txtvUbicacion)
                        txtUbicacion.setText(dataItem.paisdescrip.toString() + ", " + dataItem.ciudaddescrip.toString() )
                        val txtSangre : TextView =view.findViewById<TextView>(R.id.txtvSangre)
                        txtSangre.setText(dataItem.sangredescrip.toString())
                        val imgGenero : ImageView =view.findViewById(R.id.imgGenero)
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
                        val txtEdad : TextView =view.findViewById<TextView>(R.id.txtvEdad)
                        txtEdad.setText(edad.toString())

                        val imgPerfil : ImageView =view.findViewById(R.id.imgFotoPerfilFrag)
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
            habilitarInteraccion()
        }

        btnEditar.setOnClickListener {
            val intent = Intent(activity, EditarPerfilTrabajador::class.java)
            val bdDatos = Bundle()
            if (idCliente != null) {
                bdDatos.putInt("IDCLIENTE", idCliente)
            }
            if (datoIDLogin != null) {
                bdDatos.putInt("IDLOGIN", datoIDLogin)
            }
            bdDatos.putString("TIPO",datoTIPO)
            intent.putExtras(bdDatos)
            startActivity(intent)
        }

        btnMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.inflate(R.menu.menu_perfil)

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.op_Credenciales -> {
                        val intent = Intent(activity, EditarCredencialesTrabajador::class.java)
                        val bdDatos = Bundle()
                        if (idCliente != null) {
                            bdDatos.putInt("IDCLIENTE", idCliente)
                        }
                        if (datoIDLogin != null) {
                            bdDatos.putInt("IDLOGIN", datoIDLogin)
                        }
                        bdDatos.putString("TIPO",datoTIPO)
                        intent.putExtras(bdDatos)
                        startActivity(intent)
                        true
                    }
                    R.id.op_enfermedades -> {
                        val intent = Intent(activity, GestionarEnfermedades::class.java)
                        val bdDatos = Bundle()
                        if (idCliente != null) {
                            bdDatos.putInt("IDCLIENTE", idCliente)
                        }
                        if (datoIDLogin != null) {
                            bdDatos.putInt("IDLOGIN", datoIDLogin)
                        }
                        bdDatos.putString("TIPO",datoTIPO)
                        intent.putExtras(bdDatos)
                        startActivity(intent)
                        true
                    }
                    R.id.op_historial -> {
                        val intent = Intent(activity, Historial::class.java)
                        val bdDatos = Bundle()
                        if (idCliente != null) {
                            bdDatos.putInt("IDCLIENTE", idCliente)
                        }
                        if (datoIDLogin != null) {
                            bdDatos.putInt("IDLOGIN", datoIDLogin)
                        }
                        bdDatos.putString("TIPO",datoTIPO)
                        intent.putExtras(bdDatos)
                        startActivity(intent)
                        true
                    }
                    R.id.op_cerrars -> {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        requireContext().startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

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