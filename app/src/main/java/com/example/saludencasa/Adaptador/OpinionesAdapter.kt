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
import com.example.saludencasa.Modelo.Opiniones
import com.example.saludencasa.R
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class OpinionesAdapter(val opiniones: MutableList<Opiniones>) : RecyclerView.Adapter<OpinionesAdapter.OpinionesViewHolder>() {

    var itemSelecionado: Int = 0

    //agregar un inner al class
    class OpinionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foto : ImageView = itemView.findViewById(R.id.imgFotoPerfilOpinion)
        val nombre: TextView = itemView.findViewById(R.id.txtNombreOpinion)
        val puntuacion: TextView = itemView.findViewById(R.id.txtvCalificacion)
        val comentario: TextView = itemView.findViewById(R.id.txtvComentario)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_calificaciones_opiniones, parent, false)
        return OpinionesViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OpinionesViewHolder, position: Int) {
        val opinion = opiniones[position]
        holder.nombre.text = opinion.cliente
        val estrellas = "★".repeat(opinion.puntuacion) + "☆".repeat(5 - opinion.puntuacion)
        holder.puntuacion.text = estrellas
        holder.comentario.text = opinion.comentario
        if (opinion.foto != null) {
            holder.foto.load(opinion.foto)
        } else {
            holder.foto.setImageResource(R.drawable.foto_predeterminada)
        }
    }

    override fun getItemCount() = opiniones.size

}