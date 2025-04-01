package com.example.saludencasa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.ChatsAdapter
import com.example.saludencasa.ApiServices.ChatsService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Chats
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatFragment : Fragment(R.layout.fragment_chat), ChatsAdapter.OnItemClickListener{

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var adapterChat: ChatsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        fondoProgress = view.findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val miActividad = activity as? MainActivity3
        val idCliente = miActividad?.getMiVariableGlobal()
        val idTrabajador = miActividad?.getMiVariableGlobalTrabaja()

        val titulo: TextView = view.findViewById(R.id.txtTituloChat)
        //titulo.text = valorVariableGlobal.toString()

        recyclerViewChat = view.findViewById(R.id.rcvListaNotificaciones)
        recyclerViewChat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceChatss = retrofit.create(ChatsService::class.java)
        val callChatss = serviceChatss.getChats()

        callChatss.enqueue(object : Callback<List<Chats>> {
            override fun onResponse(call: Call<List<Chats>>, response: Response<List<Chats>>) {
                if (response.isSuccessful) {
                    val chats = response.body()
                    val chatsNuevo = mutableListOf<Chats>()

                    if (chats != null) {
                        chats.forEach { itemDato ->
                            if (itemDato.id_cliente == idCliente || itemDato.id_trabajador == idTrabajador) {
                                chatsNuevo.add(itemDato)
                            }
                        }
                    }
                    if (chatsNuevo!= null) {
                        if (idCliente!=null){
                            adapterChat = ChatsAdapter(chatsNuevo,idCliente)
                            recyclerViewChat.adapter = adapterChat
                            adapterChat.setOnItemClickListener(this@ChatFragment)
                        }
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }
                habilitarInteraccion()
            }

            override fun onFailure(call: Call<List<Chats>>, t: Throwable) {
                // Manejar error en la llamada al servicio
                habilitarInteraccion()
            }
        })

    }

    override fun onItemClick(chats: Chats) {
        val miActividad = activity as? MainActivity3
        val idClienteTrabajador = miActividad?.getMiVariableGlobal()
        val idTrabajador = miActividad?.getMiVariableGlobalTrabaja()
        if(idTrabajador!=null && idClienteTrabajador != null){
            if (idTrabajador!=0){
                var idCliente = 0
                if(idClienteTrabajador == chats.id_cliente_trabajador){
                    idCliente = chats.id_cliente
                }else{
                    idCliente = chats.id_cliente_trabajador
                }
                if(idTrabajador == chats.id_trabajador){
                    val fotoC= if(chats.fotoC==null) "" else chats.fotoC
                    val intent = ChatDetalle.newIntent(requireContext(), chats.id_chat, idCliente, chats.id_trabajador, chats.cliente,idClienteTrabajador,fotoC)
                    startActivity(intent)
                }else{
                    val fotoT= if(chats.fotoT==null) "" else chats.fotoT
                    val intent = ChatDetalle.newIntent(requireContext(), chats.id_chat, idCliente, chats.id_trabajador, chats.trabajador,idClienteTrabajador,fotoT)
                    startActivity(intent)
                }

            }else{
                val fotoT= if(chats.fotoT==null) "" else chats.fotoT
                val intent = ChatDetalleCliente.newIntent(requireContext(), chats.id_chat, chats.id_cliente, chats.id_trabajador, chats.trabajador,chats.id_cliente_trabajador,fotoT)
                startActivity(intent)
            }

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