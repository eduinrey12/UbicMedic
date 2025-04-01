package com.example.saludencasa.Modelo

data class Mensajes(
    val id_mensaje: Int,
    val id_chat: Int,
    val id_cliente: Int,
    val fecha_envio: String,
    val Mensaje: String,
    val visto_emisor: Boolean,
    val visto_receptor: Boolean,
    val tipo_mensaje: String,
    val estado_tipo: String
)
