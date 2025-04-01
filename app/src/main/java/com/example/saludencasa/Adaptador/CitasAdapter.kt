package com.example.saludencasa.Adaptador


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Modelo.Citas
import com.example.saludencasa.R
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CitasAdapter(private val citas: MutableList<Citas>, private val onItemClick: (Int,View) -> Unit) : RecyclerView.Adapter<CitasAdapter.CitasViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(citas: Citas)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun ordenarPorFecha() {
        citas.sortBy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(it.fecha_inicioatencion) }
        notifyDataSetChanged()
    }

    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.txtvNombreHorario)
        val hora: TextView = itemView.findViewById(R.id.txtvFecha)
        val horafin: TextView = itemView.findViewById(R.id.txtvhorafin)
        val persona: TextView = itemView.findViewById(R.id.txtvPersona)
        val ubicacion: TextView = itemView.findViewById(R.id.txtvUbicacion)
        val opciones: ImageButton = itemView.findViewById(R.id.btnMenuCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_horario, parent, false)
        return CitasViewHolder(itemView).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = citas[position]
                    // Llamar al listener del clic cuando se haga clic en el elemento
                    onItemClickListener?.onItemClick(clickedItem)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CitasViewHolder, position: Int) {
        val cita = citas[position]
        val vwEstado : View = holder.itemView.findViewById(R.id.vwEstado)

        holder.usernameTextView.text = cita.descripcion_motivo
        val formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val fechaIni = OffsetDateTime.parse(cita.fecha_inicioatencion, formato)
        //val fechaFin = OffsetDateTime.parse(cita.fecha_finatencion, formato)
        val horaIni = fechaIni.hour
        val minutosIni = fechaIni.minute
        val AMPMIni = if (horaIni >= 12) "PM" else "AM"
        val horaAtencion = String.format("%02d:%02d", horaIni, minutosIni)
        val horaFinAtencion = fechaIni.toLocalTime().plusMinutes(30)
        val AMPMFin = if (horaFinAtencion.hour >= 12) "PM" else "AM"
        holder.hora.text = horaAtencion + " " + AMPMIni
        holder.horafin.text = horaFinAtencion.toString() + " " + AMPMFin
        holder.persona.text = cita.trabajador
        holder.ubicacion.text = cita.latitud + ", " + cita.longitud

        if (OffsetDateTime.now() > fechaIni){
            vwEstado.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.citaPasada))
        }

        holder.opciones.setOnClickListener {
            onItemClick(cita.id_cita,it)
        }
    }

    override fun getItemCount(): Int {
        return citas.size
    }
}
