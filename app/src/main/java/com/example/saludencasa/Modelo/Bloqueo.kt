package com.example.saludencasa.Modelo

data class Bloqueo(
    val id_bloqueo: Int,
    val id_usuario_bloqueador: Int,
    val id_usuario_bloqueado: Int,
    val fecha_bloqueo: String
)