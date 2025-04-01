package com.example.saludencasa.Adaptador

import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Modelo.Citas
import com.example.saludencasa.R
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificacionesAdapter(private val idCliente: Int,val notificaciones: MutableList<Citas>, private val onItemClick: (Int, Int) -> Unit) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionesViewHolder>() {

    var itemSelecionado: Int = 0

    fun ordenarPorFecha() {
        notificaciones.sortByDescending { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(it.fecha_creacion) }
        notifyDataSetChanged()
    }

    //agregar un inner al class
    class NotificacionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreNotifiacion)
        val fecha: TextView = itemView.findViewById(R.id.txtFechaCreacion)
        val fechaAtencion: TextView = itemView.findViewById(R.id.txtFechaAtencion)
        val horaAtencion: TextView = itemView.findViewById(R.id.txtHoraAtencion)
        val lugar: TextView = itemView.findViewById(R.id.txtUbicacion)
        val foto : ImageView = itemView.findViewById(R.id.imgFotoPerfilNotificacion)
        val llopciones : LinearLayout = itemView.findViewById(R.id.llOpciones)
        val btnAceptar : Button = itemView.findViewById(R.id.btnAceptar)
        val btnCancelar : Button = itemView.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion_citas, parent, false)
        return NotificacionesViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: NotificacionesViewHolder, position: Int) {
        val notificacion = notificaciones[position]
        val formatoEntrada = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val formatoSalida = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fecha = OffsetDateTime.parse(notificacion.fecha_finatencion, formatoEntrada)
        val fechaFormateada = fecha.format(formatoSalida)
        val fechaAtencion = OffsetDateTime.parse(notificacion.fecha_inicioatencion, formatoEntrada)
        val fechaA = fechaAtencion.toLocalDate()
        val horaA = fechaAtencion.toLocalTime()
        holder.fecha.text = fechaFormateada
        holder.fechaAtencion.text = "Fecha: " + fechaA.toString()
        holder.horaAtencion.text = "Hora: " + horaA.toString()

        if(notificacion.id_cliente == idCliente){
            //Cambiar el texto para que el nombre este en negrita
            val textoCompleto = SpannableString(notificacion.trabajador + " acepto tu solicitud de cita")
            val startIndex = 0
            val endIndex = notificacion.trabajador.length
            textoCompleto.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            holder.nombre.text = textoCompleto
            holder.llopciones.visibility = View.GONE
            if (notificacion.fotoT != null) {
                holder.foto.load(notificacion.fotoT)
            } else {
                holder.foto.setImageResource(R.drawable.foto_predeterminada)
            }
        }else{//Trabajador
            //Cambiar el texto para que el nombre este en negrita
            var textoCompleto: SpannableString
            if((notificacion.estadoid == "En proceso" && itemSelecionado == 0) || itemSelecionado == 4) {
                textoCompleto= SpannableString(notificacion.cliente + " ha solicitado una cita")
            }else if (notificacion.estadoid == "Aceptada" ||  itemSelecionado == 1){
                textoCompleto=SpannableString("Has aceptado la solicitud de cita de " + notificacion.cliente)
                holder.llopciones.visibility = View.GONE
            }else {
                textoCompleto=SpannableString("Has cancelado la solicitud de cita de " + notificacion.cliente)
                holder.llopciones.visibility = View.GONE
            }
            val startIndex = textoCompleto.indexOf(notificacion.cliente)
            if (startIndex != -1) {
                val endIndex = startIndex + notificacion.cliente.length
                textoCompleto.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            holder.nombre.text = textoCompleto
            if (notificacion.fotoC != null) {
                holder.foto.load(notificacion.fotoC)
            } else {
                holder.foto.setImageResource(R.drawable.foto_predeterminada)
            }
        }
        holder.itemView.setOnClickListener {
            onItemClick(notificacion.id_cita,3)
        }
        holder.btnAceptar.setOnClickListener {
            onItemClick(notificacion.id_cita,1)
            itemSelecionado = 1
            notifyItemChanged(position)
        }
        holder.btnCancelar.setOnClickListener {
            onItemClick(notificacion.id_cita,2)
            itemSelecionado = 2
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = notificaciones.size

}