package com.example.saludencasa.Adaptador


import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Modelo.Chats
import com.example.saludencasa.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ChatsAdapter(private val chats: List<Chats>, private val idCliente: Int) : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(chats: Chats)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.txtNombreNotifiacion)
        val imgFoto: ImageView = itemView.findViewById(R.id.imgFotoPerfilNotificacion)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFechaCreacion)
        val txtultimomensaje: TextView = itemView.findViewById(R.id.txtUltimoMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatsViewHolder(itemView).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = chats[position]
                    // Llamar al listener del clic cuando se haga clic en el elemento
                    onItemClickListener?.onItemClick(clickedItem)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val chat = chats[position]
        val formatoEntrada = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val formatoSalida = DateTimeFormatter.ofPattern("HH:mm")
        val fechaFormateada = OffsetDateTime.parse(chat.fecha_creacion, formatoEntrada)
        val fechaEnvio = fechaFormateada.format(formatoSalida)
        holder.txtFecha.text = fechaEnvio
        holder.txtultimomensaje.text = chat.ultimensaje
        if(chat.id_cliente == idCliente){
            holder.usernameTextView.text = chat.trabajador
            if (chat.fotoT != null) {
                holder.imgFoto.load(chat.fotoT)
            } else {
                holder.imgFoto.setImageResource(R.drawable.foto_predeterminada)
            }
        }else{
            holder.usernameTextView.text = chat.cliente
            if (chat.fotoC != null) {
                holder.imgFoto.load(chat.fotoC)
            } else {
                holder.imgFoto.setImageResource(R.drawable.foto_predeterminada)
            }
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}
