package com.example.saludencasa.Adaptador

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Modelo.Mensajes
import com.example.saludencasa.R
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MensajesAdapter(private val mensajes: MutableList<Mensajes>, private val idCliente: Int, private val onItemClick: (Int, Int) -> Unit) : RecyclerView.Adapter<MensajesAdapter.MensajesViewHolder>() {

    var itemSelecionado: Int = 0

    class MensajesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val crdMensajeNormal: CardView = itemView.findViewById(R.id.crdMensaje)
        val txtFechaEnvio: TextView = itemView.findViewById(R.id.txtvFechaEnvio)
    }

    fun ordenarPorFecha() {
        mensajes.sortBy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(it.fecha_envio) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajesViewHolder {
        val tipoDiseño = if (viewType == 1) {
            R.layout.item_mensajes_normal
        } else {
            R.layout.item_mensajes_fecha
        }
        val itemView = LayoutInflater.from(parent.context).inflate(tipoDiseño, parent, false)
        return MensajesViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MensajesViewHolder, position: Int) {
        val mensaje = mensajes[position]
        val vistasMensajeNormal = holder.crdMensajeNormal.layoutParams as ConstraintLayout.LayoutParams
        val formatoEntrada = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val formatoSalida = DateTimeFormatter.ofPattern("HH:mm")
        val fechaFormateada = OffsetDateTime.parse(mensaje.fecha_envio, formatoEntrada)
        val fechaEnvio = fechaFormateada.format(formatoSalida)
        holder.txtFechaEnvio.text = fechaEnvio

        if(idCliente != mensaje.id_cliente){
            vistasMensajeNormal.endToEnd = ConstraintLayout.LayoutParams.UNSET
            vistasMensajeNormal.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            vistasMensajeNormal.marginEnd = 0
            vistasMensajeNormal.marginStart = 8
            vistasMensajeNormal.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            val colorEnviado = ContextCompat.getColor(holder.itemView.context, R.color.mensajeRecibido)
            holder.crdMensajeNormal.setCardBackgroundColor(colorEnviado)
        }else{
            vistasMensajeNormal.startToStart = ConstraintLayout.LayoutParams.UNSET
            vistasMensajeNormal.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            vistasMensajeNormal.marginStart = 0
            vistasMensajeNormal.marginEnd = 8
            vistasMensajeNormal.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            val colorEnviado = ContextCompat.getColor(holder.itemView.context, R.color.mensajeEnviado)
            holder.crdMensajeNormal.setCardBackgroundColor(colorEnviado)
        }

        if("fecha" == mensaje.tipo_mensaje){
            val btnAceptarFecha : Button = holder.itemView.findViewById(R.id.btnFechaAceptar)
            val btnRechazarFecha : Button = holder.itemView.findViewById(R.id.btnFechaRechazar)
            val llopciones : LinearLayout = holder.itemView.findViewById(R.id.llOpcionesFecha)
            val txtmensajeFecha: TextView = holder.itemView.findViewById(R.id.txtvMensajeDia)
            val txtmensajeHora: TextView = holder.itemView.findViewById(R.id.txtvMensajeHora)
            val txtRespuestaFecha: TextView = holder.itemView.findViewById(R.id.txtvRespuestaFecha)
            val partes = mensaje.Mensaje.split(",")
            val fecha = partes[0].trim()
            val hora = partes[1].trim()
            txtmensajeFecha.setText("Fecha: " + fecha)
            txtmensajeHora.setText("Hora: " + hora)
            txtRespuestaFecha.visibility = View.VISIBLE
            llopciones.visibility = View.GONE
            if (idCliente != mensaje.id_cliente){
                if(mensaje.estado_tipo == "enviado"){
                    txtRespuestaFecha.visibility = View.GONE
                    llopciones.visibility = View.VISIBLE
                }else if (mensaje.estado_tipo == "aceptado" || itemSelecionado == 1){
                    txtRespuestaFecha.setText("fecha de atención aceptada")
                }else{
                    txtRespuestaFecha.setText("fecha de atención rechazada")
                }
            }else{
                if(mensaje.estado_tipo == "enviado"){
                    txtRespuestaFecha.visibility = View.VISIBLE
                    llopciones.visibility = View.GONE
                }else if (mensaje.estado_tipo == "aceptado"){
                    txtRespuestaFecha.setText("fecha de atención aceptada")
                }else{
                    txtRespuestaFecha.setText("fecha de atención rechazada")
                }
            }


            btnAceptarFecha.setOnClickListener {
                onItemClick(mensaje.id_mensaje,1)
                itemSelecionado = 1
                notifyItemChanged(position)
            }
            btnRechazarFecha.setOnClickListener {
                onItemClick(mensaje.id_mensaje,2)
                itemSelecionado = 2
                notifyItemChanged(position)
            }
        }else{
            val txtmensaje: TextView = holder.itemView.findViewById(R.id.txtvMensaje)
            txtmensaje.text = mensaje.Mensaje.trim()
        }
        holder.crdMensajeNormal.layoutParams = vistasMensajeNormal

    }

    override fun getItemViewType(position: Int): Int {
        val mensaje = mensajes[position]

        return if("normal" == mensaje.tipo_mensaje){ 1 }
                else {2}
    }

    override fun getItemCount(): Int {
        return mensajes.size
    }

    fun addMensaje(mensaje: Mensajes) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }
}