package com.example.saludencasa.Modelo

data class Login(
    val id_login: Int,
    val clienteL: String,
    val id_cliente: Int,
    val usuario: String,
    val contrasenia: String,
    val tipo_login: String,
    val estado: Boolean
)