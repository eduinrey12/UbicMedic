package com.example.saludencasa.Modelo

data class Chats(
    val id_trabajador: Int,
    val id_chat: Int,
    val cliente: String,
    val trabajador: String,
    val id_cliente: Int,
    val estado: String,
    val fecha_creacion: String,
    val fotoC: String,
    val fotoT: String,
    val id_cliente_trabajador: Int,
    val ultimensaje: String,
)